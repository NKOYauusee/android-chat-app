package com.example.mychatapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.Constants
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.ui.BaseFragment
import com.example.common.util.LogUtil
import com.example.common.util.PermissionUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.ChatBean
import com.example.database.bean.HasChatBean
import com.example.database.bean.UserBean
import com.example.database.bean.UserFriBean
import com.example.database.helper.ChatListHelper
import com.example.database.helper.MainUserSelectHelper
import com.example.database.helper.UserFriendHelper
import com.example.mychatapp.activities.ChatActivity
import com.example.mychatapp.activities.SignInActivity
import com.example.mychatapp.activities.UserActivity
import com.example.mychatapp.adapter.MainChatAdapter
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.ActivityMainBinding
import com.example.mychatapp.fragments.FriendApplyStatusFragment
import com.example.mychatapp.fragments.SearchFragment
import com.example.mychatapp.listener.MainChatListener
import com.example.mychatapp.util.HttpHelper
import com.example.mychatapp.util.SelectMediaHelper
import com.example.mychatapp.util.UserUtil
import com.example.mychatapp.viewmodel.MainViewModel
import com.example.mychatapp.websocket.WebSocketManager
import com.google.gson.Gson
import com.makeramen.roundedimageview.RoundedImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Stack

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(), MainChatListener {
    private val gson = Gson()

    //
    private val mStack = Stack<BaseFragment<*, *>>()

    private var searchIsClicked = false

    // 已发起聊天的用户
    private var chatList = MainChatAdapter(mutableListOf(), this)

    // 设备未在线时 没接收到的数据（近两天）
    private var offlineMsg: MyObservable<ResBean<List<ChatBean>>>? = null

    // 轮询请求 好友处理的状态
    private var friendApplyJob: Job? = null


    // 请求结果回调
    private var friendApplyRes: MyObservable<ResBean<Int>>? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (UserStatusUtil.getIsSignIn() && UserStatusUtil.getCurLoginUser().isNotBlank()) {
            MyToast(mContext).show("登录成功")
        }

        if (!PermissionUtil.hasPermissions(this)) {
            // 请求权限
            PermissionUtil.requestPermissions(this)
        }

        dataBinding.mainDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        initFragment()
        initListener()

        setProfileContent()
        //
        WebSocketManager.instance.connect(
            HttpHelper.getWebsocketUrl(),
            UserStatusUtil.getLoginToken()
        )
        reqOfflineMsg()
    }

    override fun onResume() {
        super.onResume()
        loadUser()
        getFriendList()
        initFriendApplyRes()
        updateStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        friendApplyJob?.cancel()
        friendJob?.cancel()
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
            //prepareToSearch()

            showSearchFragment(false)
            if (!searchIsClicked) {
                searchIsClicked = true
            }

            prepareToSearch()

            switchActivity(
                this,
                UserActivity::class.java,
                R.anim.enter_animation,
                R.anim.exit_fade_out_ani
            )

            hideAllShownFragment()
            dataBinding.imageFriendNotifyIcon.setImageResource(R.drawable.ic_msg)
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

            if (mStack[0].isVisible) {
                dataBinding.imageFriendNotifyIcon.setImageResource(R.drawable.ic_msg)
            } else {
                dataBinding.imageFriendNotifyIcon.setImageResource(R.drawable.ic_msg_click)
            }
        }

        dataBinding.imageSearch.setOnClickListener {
            prepareToSearch()
        }

        dataBinding.searchAction.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    //showSearchFragment(false)
                    viewModel.searchContent.postValue("")
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    viewModel.searchContent.postValue("")
                    showSearchFragment(false)
                    return
                }

                showSearchFragment(true)
                viewModel.searchContent.postValue(s.toString())
            }
        })


        viewModel.searchContent.observe(this) {
            if (it.isNullOrEmpty()) {
                showSearchFragment(false)
            }
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
        WebSocketManager.instance.receiveMessage(this@MainActivity) {
            //更新数据库消息显示内容
            lifecycleScope.launch(Dispatchers.IO) {
                val email =
                    if (it.sender == UserStatusUtil.getCurLoginUser()) it.receiver else it.sender

                MainUserSelectHelper.updateMsgAndTime(
                    this@MainActivity,
                    it.message,
                    it.sendTime,
                    email
                )
            }

            lifecycleScope.launch(Dispatchers.Main) {
                Log.d("xht", "Main 消息提示")
                val hasChatBean = HasChatBean()
                hasChatBean.user = UserStatusUtil.getCurLoginUser()
                //对方email
                hasChatBean.email =
                    if (it.sender == hasChatBean.user) it.receiver else it.sender
                hasChatBean.nickname =
                    if (it.sender == hasChatBean.user) it.receiverName else it.senderName

                hasChatBean.sendTime = it.sendTime
                hasChatBean.newMsg = it.message
                hasChatBean.avatar = withContext(Dispatchers.IO) {
                    UserFriendHelper.selectFriendAvatar(
                        this@MainActivity,
                        UserStatusUtil.getCurLoginUser(),
                        hasChatBean.email
                    )
                }

                LogUtil.info(gson.toJson(hasChatBean.avatar))

                hasChatBean.isRead = false
                //LogUtil.info("待更新 -> ${gson.toJson(hasChatBean)}")

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
            LogUtil.info("当前已发起列表" + gson.toJson(list))
            chatList.setUserList(list)
            //chatList = MainChatAdapter(list, this@MainActivity)
            dataBinding.chatUsersRecyclerview.adapter = chatList
        }
    }

    // adapter 点击回调 跳转聊天界面 已读
    override fun onClicked(hasChat: HasChatBean) {
        LogUtil.info("已读 ${gson.toJson(hasChat)}")
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

    private var dialog: Dialog? = null

    override fun deleteMsg(target: HasChatBean, callback: () -> Unit) {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this)
            .setTitle("确定删除?")
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                //friend.status = FriendStatusEnum.BLACKLIST.statusCode
                callback()
                lifecycleScope.launch(Dispatchers.IO) {
                    MainUserSelectHelper.delete(this@MainActivity, target)
                }
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
        supportActionBar?.hide()
    }

    //返回键监听
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (dataBinding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                openOrCloseDrawer()
                return true
            }


            return if (hideAllShownFragment())
                super.onKeyDown(keyCode, event)
            else {
                true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    // 接收离线时未收到的消息
    private fun reqOfflineMsg() {
        offlineMsg = object : MyObservable<ResBean<List<ChatBean>>>() {
            override fun success(res: ResBean<List<ChatBean>>) {
                if (res.data.isNullOrEmpty())
                    return

                lifecycleScope.launch(Dispatchers.IO) {
                    val set = mutableSetOf<String>()

                    for (c in res.data!!) {
                        set.add(c.owner)
                    }

                    LogUtil.info("加载离线消息")
                    LogUtil.info("离线消息 ${gson.toJson(res.data)}")

                    ChatListHelper.saveChats(
                        this@MainActivity,
                        res.data!!.toMutableList()
                    )
                    // 更新最新消息
                    set.forEach {
                        ChatListHelper.loadNewestMsg(this@MainActivity, it)
                    }
                }
                // TODO 离线消息只加载一次 否则红点会重复出现
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
        LogUtil.info("好友处理 轮询请求开始")
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
                friendApplyJob?.cancel()
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

    // 设置侧边个人显示页
    private fun setProfileContent() {
        val header = dataBinding.navView.getHeaderView(0)

        val username = header.findViewById<TextView>(R.id.username)
        val useEmail = header.findViewById<TextView>(R.id.email)
        val profile = header.findViewById<RoundedImageView>(R.id.drawer_profile)


        username.text = UserStatusUtil.getUsername()
        useEmail.text = UserStatusUtil.getCurLoginUser()

        //LogUtil.info("头像路径 -> ${HttpUrl.IMG_URL + UserStatusUtil.getUserAvatar()}")

        Glide.with(this)
            .load(HttpHelper.getFileUrl(UserStatusUtil.getUserAvatar()))
            .placeholder(R.drawable.default_profile)
            .into(profile)

        val changeProfile = header.findViewById<RoundedImageView>(R.id.changeProfile)

        changeProfile.setOnClickListener {
            SelectMediaHelper.selectMedia(this) {
                val file = File(it[0].cutPath)
                uploadProfile(file)
            }
        }
    }

    // 头像上传
    private fun uploadProfile(
        file: File,
    ) {
        val fileData = ApiServiceHelper.getRequestBodyPart(file, "image/*")
        val userId = ApiServiceHelper.getRequestBody(UserStatusUtil.getUserId(), "text/plain")
        val email = ApiServiceHelper.getRequestBody(UserStatusUtil.getCurLoginUser(), "text/plain")

        ApiServiceHelper.service().uploadProfile(
            fileData = fileData,
            userId = userId,
            email = email,
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MyObservable<ResBean<String>>() {
                override fun success(res: ResBean<String>) {
                    if (res.code == 200 && !res.data.isNullOrEmpty()) {
                        MyToast(this@MainActivity).show("上传成功")
                        UserStatusUtil.setUserAvatar(res.data!!)

                        val header = dataBinding.navView.getHeaderView(0)
                        val profile = header.findViewById<RoundedImageView>(R.id.drawer_profile)

                        Glide.with(this@MainActivity)
                            .load(HttpHelper.getFileUrl(UserStatusUtil.getUserAvatar()))
                            .placeholder(R.drawable.default_profile)
                            .into(profile)
                        return
                    } else
                        MyToast(this@MainActivity).show("上传失败")
                }

                override fun failed(e: Throwable) {
                    Log.d(TAG, "failed: ", e)
                    MyToast(this@MainActivity).show("上传失败")
                }

            })
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
        mStack.add(SearchFragment())
    }

    private var curFragmentId = -1

    // 显示 隐藏 fragment
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
            manager.hide(mStack[position])

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

            manager.show(mStack[position])
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

    @SuppressLint("DetachAndAttachSameFragment")
    private fun showSearchFragment(isShow: Boolean) {
        val manager = supportFragmentManager.beginTransaction()

        manager.setCustomAnimations(
            R.anim.bottom_slide_in,
            R.anim.bottom_slide_out,
        )

        manager.hide(mStack[0])
        dataBinding.imageFriendNotifyIcon.setImageResource(R.drawable.ic_msg)

        if (isShow && !mStack[1].isVisible) {
            //LogUtil.info("visible")
            dataBinding.chatUsersRecyclerview.apply {
                visibility = View.INVISIBLE
                translationY = 0f
                alpha = 1f
                animate().translationY(500f)
                    .alpha(0f).setDuration(500)
                    .setInterpolator(DecelerateInterpolator()).start()
            }

            val name = mStack[1]::class.java.name

            if (!mStack[1].isVisible) {
                if (!mStack[1].isAdded) {
                    manager.add(R.id.fragment_container, mStack[1], name)
                }

                manager.show(mStack[1])
            }
        } else if (!isShow) {
            //LogUtil.info("invisible")
            manager.hide(mStack[1])

            if (mStack[1].isVisible) {
                dataBinding.chatUsersRecyclerview.apply {
                    visibility = View.VISIBLE
                    translationY = 500f
                    alpha = 0f
                    animate().translationY(0f)
                        .alpha(1f).setDuration(500)
                        .setInterpolator(DecelerateInterpolator()).start()
                }
            } else {
                dataBinding.chatUsersRecyclerview.apply {
                    visibility = View.VISIBLE
                }
            }

        }

        manager.commit()
        curFragmentId = 1
    }

    private fun hideAllShownFragment(): Boolean {
        dataBinding.imageFriendNotifyIcon.setImageResource(R.drawable.ic_msg)

        if (!searchIsClicked) {
            searchIsClicked = true
        }
        prepareToSearch()


        var isBack = true
        val manager = supportFragmentManager.beginTransaction()

        manager.setCustomAnimations(
            R.anim.bottom_slide_in,
            R.anim.bottom_slide_out,
        )

        for (f in mStack) {
            if (f.isVisible) {
                manager.hide(f).commit()
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

    private fun prepareToSearch() {
        searchIsClicked = !searchIsClicked
        viewModel.searchIsClicked.postValue(searchIsClicked)
        if (searchIsClicked) {
            dataBinding.imageSearch.setImageResource(R.drawable.ic_search_click)
            dataBinding.searchAction.visibility = View.VISIBLE
            val layoutParams = dataBinding.searchWrapper.layoutParams
            layoutParams.width = dataBinding.imageSearch.width * 5
            dataBinding.searchWrapper.layoutParams = layoutParams

            lifecycleScope.launch {
                delay(200)
                dataBinding.searchAction.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(dataBinding.searchAction, InputMethodManager.SHOW_IMPLICIT)
            }

            if (dataBinding.searchAction.text.isNotEmpty()) {
                showSearchFragment(true)
                viewModel.searchContent.postValue(dataBinding.searchAction.text.toString())
            }
            // TODO
        } else {
            dataBinding.imageSearch.setImageResource(R.drawable.ic_search)
            dataBinding.searchAction.visibility = View.GONE
            val layoutParams = dataBinding.searchWrapper.layoutParams
            layoutParams.width = dataBinding.imageSearch.width

            dataBinding.searchWrapper.layoutParams = layoutParams

            showSearchFragment(false)

            lifecycleScope.launch {
                delay(200)
                val inputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(dataBinding.searchAction.windowToken, 0)
            }
        }
    }

    private var friendJob: Job? = null

    private fun getFriendList() {
        friendJob?.cancel()

        friendJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                ApiServiceHelper.service()
                    .getUserFriend(UserStatusUtil.getCurLoginUser())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : MyObservable<ResBean<List<UserFriBean>>>() {
                        override fun success(res: ResBean<List<UserFriBean>>) {
                            //Log.d("xht", "success: ${gson.toJson(res)}")
                            val dataList = res.data
                            if (!dataList.isNullOrEmpty()) {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    UserFriendHelper.insertFriends(
                                        this@MainActivity,
                                        dataList.toMutableList()
                                    )

                                    for (friend in dataList) {
                                        val temp = UserFriBean()
                                        temp.email = friend.email
                                        temp.avatar = friend.avatar
                                        MainUserSelectHelper.insertProfile(
                                            this@MainActivity,
                                            temp
                                        )
                                    }

                                    //updateStatus()
                                }
                            }
                        }

                        override fun failed(e: Throwable) {
                            //loadLocalInfo()
                        }
                    })

                delay(5000)
            }
        }
    }
}