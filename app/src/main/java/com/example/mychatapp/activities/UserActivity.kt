package com.example.mychatapp.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.Constants
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.UserFriBean
import com.example.database.helper.MainUserSelectHelper
import com.example.database.helper.UserFriendHelper
import com.example.mychatapp.BR
import com.example.mychatapp.R
import com.example.mychatapp.adapter.UserFriendListAdapter
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.ActivityUserBinding
import com.example.database.enums.FriendStatusEnum
import com.example.mychatapp.listener.UserListener
import com.example.mychatapp.viewmodel.UserViewModel
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserActivity : BaseActivity<ActivityUserBinding, UserViewModel>(), UserListener {
    private var userFriList: MyObservable<ResBean<List<UserFriBean>>>? = null
    private var userAdapter: UserFriendListAdapter? = null
    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initListener()
    }

    // TODO
    override fun onResume() {
        super.onResume()
        //loadLocalInfo()
    }

    // TODO
    fun loadLocalInfo() {
        lifecycleScope.launch(Dispatchers.Main) {
            val dataList = withContext(Dispatchers.IO) {
                UserFriendHelper.selectFriends(this@UserActivity, UserStatusUtil.getCurLoginUser())
            }

            //userAdapter = UserAdapter(dataList, this@UserActivity)
            userAdapter = UserFriendListAdapter(dataList, this@UserActivity)
            dataBinding.userRecycleView.adapter = userAdapter
            loading(false)
        }
    }

    private fun initData() {
        //dataBinding.textErrorMessage.visibility = View.INVISIBLE
        userFriList = object : MyObservable<ResBean<List<UserFriBean>>>() {
            override fun success(res: ResBean<List<UserFriBean>>) {
                Log.d("xht", "success: ${gson.toJson(res)}")
                val dataList = res.data

                if (!dataList.isNullOrEmpty()) {
                    //viewModel.userList.postValue(res.data)
                    loading(false)
                    //userAdapter = UserAdapter(dataList, this@UserActivity)
                    userAdapter = UserFriendListAdapter(dataList.toMutableList(), this@UserActivity)

                    dataBinding.userRecycleView.adapter = userAdapter

                    lifecycleScope.launch(Dispatchers.IO) {
                        UserFriendHelper.insertFriends(this@UserActivity, dataList.toMutableList())
                    }
                }
            }

            override fun failed(e: Throwable) {
                //loadLocalInfo()
            }
        }

        LogUtil.info("请求好友列表")
        ApiServiceHelper.service()
            .getUserFriend(UserStatusUtil.getCurLoginUser())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(userFriList!!)
    }

    private fun initListener() {
        dataBinding.imgBack.setOnClickListener {
            //overridePendingTransition(R.anim.enter_animation, R.anim.exit_fade_out_ani)
            finish()
        }

        dataBinding.imgBatchManger.setOnClickListener {
            isClickBatchManage = !isClickBatchManage
            viewModel.isClickBatchManage.postValue(isClickBatchManage)
            showBatchDelete(isClickBatchManage)
        }

    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_user, BR.vm)
    }

    //加载
    fun loading(isLoading: Boolean = true) {
        if (isLoading) {
            dataBinding.userProgress.visibility = View.VISIBLE
            dataBinding.userRecycleView.visibility = View.INVISIBLE
        } else {
            dataBinding.userProgress.visibility = View.INVISIBLE
            dataBinding.userRecycleView.visibility = View.VISIBLE
            dataBinding.textErrorMessage.visibility = View.INVISIBLE
        }
    }

    // 选择好友聊天
    override fun onUserClicked(friend: UserFriBean) {
        if (isClickBatchManage) {
            showBatchDelete(false)
            isClickBatchManage = false
            return
        }
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(Constants.CHAT_FRIEND, friend)
        switchActivity(intent, R.anim.enter_animation, R.anim.exit_fade_out_ani)
    }

    private var dialog: Dialog? = null

    // 拉黑好友回调
    override fun blackListFriend(friend: UserFriBean, callback: (() -> Unit)) {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this)
            .setTitle("确定拉黑该用户?")
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                friend.status = FriendStatusEnum.BLACKLIST.statusCode
                ApiServiceHelper.service()
                    .setFriendStatus(friend)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : MyObservable<ResBean<Nothing>>() {
                        override fun success(res: ResBean<Nothing>) {
                            if (res.code != 200) {
                                MyToast(mContext).show("操作失败")
                                return
                            }
                            MyToast(mContext).show("拉黑成功")
                            callback()

                            lifecycleScope.launch(Dispatchers.IO) {
                                MainUserSelectHelper.deleteMainHasChatSow(
                                    this@UserActivity,
                                    friend.owner,
                                    friend.email
                                )
                            }
                        }

                        override fun failed(e: Throwable) {
                            MyToast(mContext).show("操作失败，请稍后再试")
                            Log.d(TAG, "failed blackListFriend: ", e)
                        }

                    })
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .create()


        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
        supportActionBar?.hide()
    }

    // 删除好友回调
    override fun deleteFriend(friend: UserFriBean, callback: (() -> Unit)) {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this)
            .setTitle("确定删除该用户?")
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                //friend.status = FriendStatusEnum.BLACKLIST.statusCode
                ApiServiceHelper.service()
                    .deleteFri(UserStatusUtil.getCurLoginUser(), friend.email)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : MyObservable<ResBean<Nothing>>() {
                        override fun success(res: ResBean<Nothing>) {
                            if (res.code != 200) {
                                MyToast(mContext).show("操作失败")
                                return
                            }
                            MyToast(mContext).show("删除成功")
                            callback()

                            lifecycleScope.launch(Dispatchers.IO) {
                                MainUserSelectHelper.deleteMainHasChatSow(
                                    this@UserActivity,
                                    friend.owner,
                                    friend.email
                                )
                            }

                            // TODO 是否保留该用户的聊天记录
                        }

                        override fun failed(e: Throwable) {
                            MyToast(mContext).show("操作失败，请稍后再试")
                            Log.d(TAG, "failed blackListFriend: ", e)
                        }

                    })
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
        supportActionBar?.hide()
    }

    override fun preventLongClick(callback: (prevent: Boolean) -> Unit) {
        callback(isClickBatchManage)
    }

    // 好友操作栏
    private var isClickBatchManage = false

    // 显示 隐藏多选框
    private fun showBatchDelete(isShow: Boolean = true) {
//        if (isShow != View.VISIBLE && isShow != View.GONE)
//            return


        for (i in 0 until (userAdapter?.returnFriendListSize() ?: 0)) {
            dataBinding.userRecycleView.getChildAt(i)
                .findViewById<CheckBox>(R.id.batch_delete).apply {
                    if (isShow) {
                        translationX = -400f
                        alpha = 0f
                        animate()
                            .alpha(1f).translationX(0f).setDuration(300)
                            .setInterpolator(DecelerateInterpolator()).start()

                        visibility = View.VISIBLE
                    } else {
                        visibility = View.GONE
                    }
                }

            dataBinding.userRecycleView.getChildAt(i)
                .findViewById<LinearLayout>(R.id.friendActionWrapper).visibility = View.INVISIBLE
        }

        if (isShow) {
            dataBinding.imgBatchManger.setBackgroundResource(R.drawable.background_icon_click)
            dataBinding.bottomActionBar.apply {
                visibility = View.VISIBLE
                translationY = 500f
                alpha = 0f
                animate().translationY(0f)
                    .alpha(1f).setDuration(500)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        } else {
            dataBinding.imgBatchManger.setBackgroundResource(R.drawable.background_icon)
            dataBinding.bottomActionBar.apply {
                visibility = View.GONE
                translationY = 0f
                alpha = 0f
                animate().translationY(500f)
                    .alpha(1f).setDuration(500)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isClickBatchManage) {
                isClickBatchManage = false
                showBatchDelete(false)
                return true
            }

//            for (i in 0 until (userAdapter?.returnFriendListSize() ?: 0)) {
//                dataBinding.userRecycleView.getChildAt(i)
//                    .findViewById<LinearLayout>(R.id.friendActionWrapper).visibility = View.GONE
//            }
        }
        return super.onKeyDown(keyCode, event)
    }

}