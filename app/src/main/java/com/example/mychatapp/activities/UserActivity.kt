package com.example.mychatapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.CheckBox
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
import com.example.database.helper.UserFriendHelper
import com.example.mychatapp.BR
import com.example.mychatapp.R
import com.example.mychatapp.adapter.NewUserAdapter
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
    private var userAdapter: NewUserAdapter? = null
    val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        //loadLocalInfo()
    }

    fun loadLocalInfo() {
        lifecycleScope.launch(Dispatchers.Main) {
            val dataList = withContext(Dispatchers.IO) {
                UserFriendHelper.selectFriends(this@UserActivity, UserStatusUtil.getCurLoginUser())
            }

            //userAdapter = UserAdapter(dataList, this@UserActivity)
            userAdapter = NewUserAdapter(dataList, this@UserActivity)
            dataBinding.userRecycleView.adapter = userAdapter
            loading(false)
        }
    }

    private fun init() {
        //dataBinding.textErrorMessage.visibility = View.INVISIBLE
        userFriList = object : MyObservable<ResBean<List<UserFriBean>>>() {
            override fun success(res: ResBean<List<UserFriBean>>) {
                Log.d("xht", "success: ${gson.toJson(res)}")
                val dataList = res.data

                if (!dataList.isNullOrEmpty()) {
                    //viewModel.userList.postValue(res.data)
                    loading(false)
                    //userAdapter = UserAdapter(dataList, this@UserActivity)
                    userAdapter = NewUserAdapter(dataList, this@UserActivity)

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
            setBatchBtnStatus()
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
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(Constants.CHAT_FRIEND, friend)
        switchActivity(intent, R.anim.enter_animation, R.anim.exit_fade_out_ani)
    }

    // 好友操作栏
    private var isClickBatchManage = false
    private fun setBatchBtnStatus() {
        isClickBatchManage = !isClickBatchManage
        // TODO
        viewModel.isClickBatchManage.postValue(isClickBatchManage)

        if (isClickBatchManage) {
            showBatchDelete()
        } else {
            showBatchDelete(View.GONE)
        }

    }

    private fun showBatchDelete(isShow: Int = View.VISIBLE) {
        if (isShow != View.VISIBLE && isShow != View.GONE)
            return


        for (i in 0 until (userAdapter?.returnFriendListSize() ?: 0)) {
            dataBinding.userRecycleView.getChildAt(i)
                .findViewById<CheckBox>(R.id.batch_delete).apply {
                    if (isShow == View.VISIBLE) {
                        translationX = -400f
                        alpha = 0f
                        animate()
                            .alpha(1f).translationX(0f).setDuration(300)
                            .setInterpolator(DecelerateInterpolator()).start()
                    }
                    visibility = isShow
                }
        }

        if (isShow == View.VISIBLE) {
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
                showBatchDelete(View.GONE)

                return true
            }
        }

        return super.onKeyDown(keyCode, event)
    }


}