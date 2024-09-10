package com.example.mychatapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.api.bean.HttpUrl
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.Constants
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.ui.BaseFragment
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.ChatBean
import com.example.database.bean.HasChatBean
import com.example.database.bean.UserBean
import com.example.database.bean.UserFriBean
import com.example.database.helper.ChatListHelper
import com.example.database.helper.MainUserSelectHelper
import com.example.mychatapp.activities.ChatActivity
import com.example.mychatapp.activities.SignInActivity
import com.example.mychatapp.activities.UserActivity
import com.example.mychatapp.adapter.MainChatAdapter
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.ActivityMainBinding
import com.example.mychatapp.fragments.FriendApplyStatusFragment
import com.example.mychatapp.listener.MainChatListener
import com.example.mychatapp.util.UserUtil
import com.example.mychatapp.viewmodel.MainViewModel
import com.example.mychatapp.websocket.WebSocketManager
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Stack

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(), MainChatListener {
    private val gson = Gson()

    private val mStack = Stack<BaseFragment<*, *>>()

    // 已发起聊天的用户
    private var chatList = MainChatAdapter(mutableListOf(), this)

    // 设备未在线时 没接收到的数据（近两天）
    private var offlineMsg: MyObservable<ResBean<List<ChatBean>>>? = null

    // 轮询请求 好友处理的状态
    private var friendApplyJob: Job? = null

    // 请求结果回调
    private var friendApplyRes: MyObservable<ResBean<Int>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding.mainDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        if (UserStatusUtil.getIsSignIn() && UserStatusUtil.getCurLoginUser().isNotBlank()) {
            MyToast(mContext).show("登录成功")
        }


        initFragment()
        initListener()

        setProfileContent()
        WebSocketManager.instance.connect(HttpUrl.WS_URL, UserStatusUtil.getLoginToken())
        loadUnReceiveMsg()
    }

    override fun onResume() {
        super.onResume()
        loadUser()
        updateStatus()
        initFriendApplyRes()
    }

    override fun onDestroy() {
        super.onDestroy()
        friendApplyJob?.cancel()
        WebSocketManager.instance.stopConn()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_main, BR.vm)
    }

    private fun initListener() {
        dataBinding.imageProfile.setOnClickListener {
            openOrCloseDrawer()
        }
        // 跳转选择好友界面
        dataBinding.fabNewChat.setOnClickListener {
            switchActivity(
                this,
                UserActivity::class.java,
                R.anim.enter_animation,
                R.anim.exit_fade_out_ani
            )

            hideAllShownFragment()
        }
        // 退出弹窗
        dataBinding.btnLogout.setOnClickListener {
            showDialog(
                context = this,
                positiveText = "确认",
                negativeText = "取消",
                title = "退出登录"
            )
        }

        dataBinding.imageFriendNotify.setOnClickListener {
            //LogUtil.info("好友申请界面")
            changeFragment(0)
        }

        dataBinding.imageFriendSearch.setOnClickListener {
            prepareToSearch()
        }
    }

    // 侧边栏界面的开/关
    private fun openOrCloseDrawer() {
        val layout = dataBinding.navView.layoutParams
        layout?.let {
            it.width = getResources().displayMetrics.widthPixels
            dataBinding.navView.layoutParams = it
        }

        if (!dataBinding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            dataBinding.mainDrawerLayout.openDrawer(GravityCompat.START)
            dataBinding.mainDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        } else {
            dataBinding.mainDrawerLayout.closeDrawer(GravityCompat.START)
            dataBinding.mainDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    // 接收聊天并加载已聊天的用户
    private fun loadUser() {
        //主页消息提示
        WebSocketManager.instance.loadMsg(this@MainActivity) {
            //更新数据库消息显示内容
            lifecycleScope.launch(Dispatchers.IO) {
                val email =
                    if (it.sender == UserStatusUtil.getCurLoginUser()) it.receiver else it.sender

                MainUserSelectHelper.updateMsgAndTime(
                    this@MainActivity,
                    it.message!!,
                    it.sendTime,
                    email!!
                )
            }

            lifecycleScope.launch(Dispatchers.Main) {
                Log.d("xht", "Main 消息提示")
                val hasChatBean = HasChatBean()
                hasChatBean.user = UserStatusUtil.getCurLoginUser()
                //对方email
                hasChatBean.email =
                    if (it.sender == hasChatBean.user) it.receiver!! else it.sender!!
                hasChatBean.nickname =
                    if (it.sender == hasChatBean.user) it.receiverName else it.senderName

                hasChatBean.sendTime = it.sendTime
                hasChatBean.newMsg = it.message
                hasChatBean.avatar = "" // TODO
                hasChatBean.isRead = false
                LogUtil.info("待更新 -> ${gson.toJson(hasChatBean)}")
                chatList.updateMsg(hasChatBean) {
                    chatList.notifyItemChanged(it, true)
                }

                delay(500)
                updateStatus()
            }
        }
    }

    // 显示已发起聊天的用户
    fun updateStatus() {
        // 主页数据加载
        lifecycleScope.launch(Dispatchers.Main) {
            val list = withContext(Dispatchers.IO) {
                MainUserSelectHelper.load(this@MainActivity, UserStatusUtil.getCurLoginUser())
            }
            LogUtil.info(gson.toJson(list))
            chatList.setUserList(list)
            //chatList = MainChatAdapter(list, this@MainActivity)
            dataBinding.chatUsersRecyclerview.adapter = chatList
        }
    }

    // adapter 点击回调
    override fun onClicked(hasChat: HasChatBean) {
        lifecycleScope.launch(Dispatchers.IO) {
            MainUserSelectHelper.insert(this@MainActivity, hasChat)
        }

        val intent = Intent(this, ChatActivity::class.java)
        val friend = UserFriBean()
        friend.email = hasChat.email
        friend.avatar = hasChat.avatar
        friend.username = hasChat.nickname
        intent.putExtra(Constants.CHAT_FRIEND, friend)
        switchActivity(intent, R.anim.enter_animation, R.anim.exit_fade_out_ani)
    }

    //返回键监听
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (dataBinding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                openOrCloseDrawer()
                return true
            }
            return hideAllShownFragment()
        }
        return super.onKeyDown(keyCode, event)
    }

    //接受离线消息
    private fun loadUnReceiveMsg() {
        offlineMsg = object : MyObservable<ResBean<List<ChatBean>>>() {
            override fun success(res: ResBean<List<ChatBean>>) {
                Log.d("xht", "offlineMsg ${gson.toJson(res.data)}")

                if (res.data.isNullOrEmpty())
                    return

                lifecycleScope.launch(Dispatchers.IO) {
                    val set = mutableSetOf<String>()

                    for (c in res.data!!) {
                        set.add(c.owner)
                    }

                    ChatListHelper.saveChats(
                        this@MainActivity,
                        res.data!!.toMutableList()
                    )

                    set.forEach {
                        ChatListHelper.loadNewestMsg(this@MainActivity, it)
                    }
                }

                updateStatus()
            }

            override fun failed(e: Throwable) {
            }
        }

        val key = UserStatusUtil.getCurLoginUser()
        //LogUtil.info("key -> $key")
        //offlineJob?.cancel()
        //offlineJob =
        ApiServiceHelper.service()
            .loadUnReceiveMsg(key)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(offlineMsg!!)
    }

    // 好友处理结果回调
    private fun initFriendApplyRes() {
        friendApplyRes = object : MyObservable<ResBean<Int>>() {
            override fun success(res: ResBean<Int>) {
                //Log.d("xht", "success: ${gson.toJson(res)}")
                if (res.code == 200) {
                    // TODO 好友申请状态显示
                    if (res.data!! > 0) {
                        viewModel.hasFriendApply.postValue(true)
                    } else {
                        viewModel.hasFriendApply.postValue(false)
                    }
                }
            }

            override fun failed(e: Throwable) {
                Log.e("xht", "failed req friend apply status", e)
            }
        }

        startReqFriendApplyStatusPoll()
    }

    // 好友申请 轮询
    private fun startReqFriendApplyStatusPoll() {
        friendApplyJob?.cancel()
        friendApplyJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                ApiServiceHelper.service()
                    .getHasApplyList(UserStatusUtil.getCurLoginUser())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(friendApplyRes!!)

                delay(3000)
            }
        }
    }

    private fun setProfileContent() {
        val header = dataBinding.navView.getHeaderView(0)
        val username = header.findViewById<TextView>(R.id.username)
        val useEmail = header.findViewById<TextView>(R.id.email)

        username.text = UserStatusUtil.getUsername()
        useEmail.text = UserStatusUtil.getCurLoginUser()
    }

    // 确认 退出登录的操作
    override fun onPositiveButtonClick() {
        super.onPositiveButtonClick()

        val user = UserBean()
        user.id = -1
        user.avatar = ""
        user.username = ""
        user.email = ""
        user.phone = ""
        user.token = ""
        UserUtil.setLoginStatus(user, false)

        switchActivity(
            this, SignInActivity::class.java, R.anim.enter_animation,
            R.anim.exit_animation, true
        )
    }


    private fun initFragment() {
        // 0 好友申请处理页
        mStack.add(FriendApplyStatusFragment())
    }

    private var curFragmentId = -1

    @SuppressLint("DetachAndAttachSameFragment")
    private fun changeFragment(position: Int) {
        if (position >= mStack.size || position < 0)
            return

        val manager = supportFragmentManager.beginTransaction()

        manager.setCustomAnimations(
            R.anim.bottom_slide_in,
            R.anim.bottom_slide_out,
        )

        val name = mStack[position]::class.java.name

        // 如果 Fragment 已经添加并且显示中，则隐藏它
        if (mStack[position].isAdded && mStack[position].isVisible) {
            //manager.hide(mStack[position])
            manager.detach(mStack[position])

            dataBinding.chatUsersRecyclerview.apply {
                visibility = View.VISIBLE
                translationY = 500f
                alpha = 0f
                animate().translationY(0f)
                    .alpha(1f).setDuration(500)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        } else {
            // 如果 Fragment 尚未添加，则添加并显示
            if (!mStack[position].isAdded) {
                manager.add(R.id.fragment_container, mStack[position], name)
            }

            manager.attach(mStack[position])
            //manager.show(mStack[position])
            dataBinding.chatUsersRecyclerview.apply {
                visibility = View.INVISIBLE
                translationY = 0f
                alpha = 1f
                animate().translationY(500f)
                    .alpha(0f).setDuration(500)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        }

        //manager.show(mStack[position])
        manager.commit()
        if (curFragmentId != position && curFragmentId > 0) {
            manager.hide(mStack[curFragmentId])
        }
        curFragmentId = position
    }

    private fun hideAllShownFragment(): Boolean {
        var isBack = true
        val manager = supportFragmentManager.beginTransaction()

        manager.setCustomAnimations(
            R.anim.bottom_slide_in,
            R.anim.bottom_slide_out,
        )

        for (f in mStack) {
            if (f.isVisible) {
                manager.detach(f).commit()
                isBack = false
            }
        }

        if (!isBack) {
            dataBinding.chatUsersRecyclerview.apply {
                visibility = View.VISIBLE
                translationY = 500f
                alpha = 0f
                animate().translationY(0f)
                    .alpha(1f).setDuration(500)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        }
        return isBack
    }

    private var searchIsClicked = false
    private fun prepareToSearch() {
        searchIsClicked = !searchIsClicked
        viewModel.searchIsClicked.postValue(searchIsClicked)
        if (searchIsClicked) {
            dataBinding.imageFriendSearch.setImageResource(R.drawable.ic_search_click)
            dataBinding.searchAction.visibility = View.VISIBLE
            val layoutParams = dataBinding.searchWrapper.layoutParams
            layoutParams.width = dataBinding.imageFriendSearch.width * 5

            dataBinding.searchWrapper.layoutParams = layoutParams
        } else {
            dataBinding.imageFriendSearch.setImageResource(R.drawable.ic_search)
            dataBinding.searchAction.visibility = View.INVISIBLE
            val layoutParams = dataBinding.searchWrapper.layoutParams
            layoutParams.width = dataBinding.imageFriendSearch.width

            dataBinding.searchWrapper.layoutParams = layoutParams
        }
    }
}