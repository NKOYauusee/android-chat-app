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
import com.example.common.util.UserStatusUtil
import com.example.database.bean.UserFriBean
import com.example.database.enums.FriendStatusEnum
import com.example.database.helper.MainUserSelectHelper
import com.example.database.helper.UserFriendHelper
import com.example.mychatapp.BR
import com.example.mychatapp.R
import com.example.mychatapp.adapter.UserFriendListAdapter
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.ActivityUserBinding
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
        //initData()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        loadLocalInfo()
    }

    private fun loadLocalInfo() {
        lifecycleScope.launch(Dispatchers.Main) {
            val dataList = withContext(Dispatchers.IO) {
                UserFriendHelper.selectNoBlockedFriends(
                    this@UserActivity,
                    UserStatusUtil.getCurLoginUser()
                )
            }

            //userAdapter = UserAdapter(dataList, this@UserActivity)
            userAdapter = UserFriendListAdapter(dataList, this@UserActivity)
            dataBinding.userRecycleView.adapter = userAdapter

            viewModel.hasFriend.postValue(dataList.isNotEmpty())
        }
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

        dataBinding.deleteFriendBtn.setOnClickListener {
            val size = dataBinding.userRecycleView.childCount
            val selectList = mutableListOf<Int>()
            val selectFriendList = mutableListOf<UserFriBean>()
            for (i in size - 1 downTo 0) {
                val isSelected = dataBinding.userRecycleView.getChildAt(i)
                    .findViewById<CheckBox>(R.id.batch_delete)

                if (isSelected.isChecked) {
                    selectList.add(i)
                    userAdapter?.let { it1 ->
                        selectFriendList.add(it1.getEmailFromPos(i))
                    }
                }
            }

            dialogBatchDelete(selectFriendList, selectList)
        }
    }

    // 批量删除
    private fun dialogBatchDelete(
        list: MutableList<UserFriBean>,
        posList: MutableList<Int>,
    ) {
        if (list.size == 0) return

        dialog?.dismiss()
        dialog = AlertDialog.Builder(this).setTitle(getString(R.string.info_confirm_deleting_users))
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                ApiServiceHelper.service().batchDeleteFri(
                    list
                ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : MyObservable<ResBean<Nothing>>() {
                        override fun success(res: ResBean<Nothing>) {
                            if (res.code != 200) {
                                MyToast(mContext).show(getString(R.string.info_act_fail))
                                return
                            }

                            userAdapter?.batchRemove(posList)
                            showBatchDelete(false)

                            lifecycleScope.launch(Dispatchers.IO) {
                                UserFriendHelper.batchDeleteFriend(this@UserActivity, list)
                            }
                        }

                        override fun failed(e: Throwable) {
                            MyToast(mContext).show(getString(R.string.info_act_fail))
                            Log.d(TAG, "failed blackListFriend: ", e)
                        }

                    })
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.create()


        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
        supportActionBar?.hide()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_user, BR.vm)
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
        dialog = AlertDialog.Builder(this).setTitle(getString(R.string.info_confirm_block_user))
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                friend.status = FriendStatusEnum.BLACKLIST.statusCode

                ApiServiceHelper.service().setFriendStatus(friend).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : MyObservable<ResBean<Nothing>>() {
                        override fun success(res: ResBean<Nothing>) {
                            if (res.code != 200) {
                                MyToast(mContext).show(getString(R.string.info_act_fail))
                                return
                            }
                            MyToast(mContext).show(getString(R.string.info_blocked_successfully))
                            callback()

                            lifecycleScope.launch(Dispatchers.IO) {
                                UserFriendHelper.blacklistFriend(this@UserActivity, friend)

                                MainUserSelectHelper.deleteMainHasChatSow(
                                    this@UserActivity, friend.owner, friend.email
                                )
                            }
                        }

                        override fun failed(e: Throwable) {
                            MyToast(mContext).show(getString(R.string.info_act_fail))
                            Log.d(TAG, "failed blackListFriend: ", e)
                        }

                    })
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.create()


        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
        supportActionBar?.hide()
    }

    // 删除好友回调
    override fun deleteFriend(friend: UserFriBean, callback: (() -> Unit)) {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this).setTitle(getString(R.string.info_confirm_delete_user))
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                //friend.status = FriendStatusEnum.BLACKLIST.statusCode
                ApiServiceHelper.service().deleteFri(UserStatusUtil.getCurLoginUser(), friend.email)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : MyObservable<ResBean<Nothing>>() {
                        override fun success(res: ResBean<Nothing>) {
                            if (res.code != 200) {
                                MyToast(mContext).show(getString(R.string.info_act_fail))
                                return
                            }
                            MyToast(mContext).show(getString(R.string.info_delete_successfully))
                            callback()

                            lifecycleScope.launch(Dispatchers.IO) {
                                MainUserSelectHelper.deleteMainHasChatSow(
                                    this@UserActivity, friend.owner, friend.email
                                )

                                UserFriendHelper.deleteFriend(this@UserActivity, friend)

                                // 删除与该用户的所有的聊天记录
                            }

                            // TODO 是否保留该用户的聊天记录
                        }

                        override fun failed(e: Throwable) {
                            MyToast(mContext).show(getString(R.string.info_act_fail))
                            Log.d(TAG, "failed blackListFriend: ", e)
                        }

                    })
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.create()
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
            dataBinding.userRecycleView.getChildAt(i).findViewById<CheckBox>(R.id.batch_delete)
                .apply {
                    if (isShow) {
                        translationX = -400f
                        alpha = 0f
                        animate().alpha(1f).translationX(0f).setDuration(300)
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
                animate().translationY(0f).alpha(1f).setDuration(500)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        } else {
            dataBinding.imgBatchManger.setBackgroundResource(R.drawable.background_icon)
            dataBinding.bottomActionBar.apply {
                visibility = View.GONE
                translationY = 0f
                alpha = 0f
                animate().translationY(500f).alpha(1f).setDuration(500)
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