package com.example.mychatapp.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.api.bean.HttpUrl
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.Constants
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.ChatBean
import com.example.database.bean.UserFriBean
import com.example.database.enums.MessageType
import com.example.database.helper.ChatListHelper
import com.example.database.helper.MainUserSelectHelper
import com.example.mychatapp.BR
import com.example.mychatapp.R
import com.example.mychatapp.adapter.ChatAdapter
import com.example.mychatapp.databinding.ActivityChatBinding
import com.example.mychatapp.listener.ChatMsgListener
import com.example.mychatapp.util.SelectMediaHelper
import com.example.mychatapp.viewmodel.ChatViewModel
import com.example.mychatapp.websocket.WebSocketManager
import com.google.gson.Gson
import com.luck.picture.lib.entity.LocalMedia
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class ChatActivity : BaseActivity<ActivityChatBinding, ChatViewModel>(), ChatMsgListener {
    private var chatListAdapter: ChatAdapter? = null
    //private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadReceiver()

        initListener()
        loadMsg()
        //ZoomMediaLoader.getInstance().init(ImagePreviewLoader())
    }

    override fun onResume() {
        super.onResume()
        listener()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_chat, BR.vm)
    }

    // 加载对方用户基本信息
    private fun loadReceiver() {
        val curChatUser = intent.getSerializableExtra(Constants.CHAT_FRIEND) as UserFriBean?

        //LogUtil.info("loadReceiver " + gson.toJson(curChatUser))
        viewModel.receiver.postValue(curChatUser)

        dataBinding.chatName.text = curChatUser?.username

        val chat = intent.getSerializableExtra(Constants.CHAT_START) as? ChatBean
        LogUtil.info("消息起始位置" + Gson().toJson(chat))
    }

    private fun initListener() {
        // 返回
        dataBinding.imgBack.setOnClickListener {
            finish()
        }

        dataBinding.inputMsg.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) changeMoreWrapperStyle(true)
        }

        // 发送消息
        dataBinding.layoutSendMsg.setOnClickListener {
            val msg = dataBinding.inputMsg.text.toString()
            if (msg.isEmpty()) return@setOnClickListener


            lifecycleScope.launch(Dispatchers.IO) {
                WebSocketManager.instance.sendMsg(generateChat(msg))
//                MainUserSelectHelper.insertProfile(
//                    this@ChatActivity,
//                    viewModel.receiver.value!!
//                )
            }
            dataBinding.inputMsg.setText("")
        }

        dataBinding.layoutMore.setOnClickListener {
            changeMoreWrapperStyle()
        }

        dataBinding.selectMedia.setOnClickListener {

            SelectMediaHelper.selectMedia(this, 5) {
                for (img in it) {
                    val file =
                        if (img.isCut && it.size == 1) File(img.cutPath) else File(img.realPath)
                    if (file.exists()) {
                        uploadImg(file, img)
                    } else {
                        LogUtil.info("选择的文件不存在")
                    }
                }
            }
        }

        //val mManager: LinearLayoutManager = LinearLayoutManager(mContext)
        //mManager.stackFromEnd = true
        //dataBinding.chatRecycleView.layoutManager = mManager
        dataBinding.chatRecycleView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                lifecycleScope.launch(Dispatchers.Main) {
                    chatListAdapter?.scrollToLastIdx {
                        dataBinding.chatRecycleView.scrollToPosition(it)
                    }

                }
            }
        }
    }

    private fun listener() {
        // 下滑顶部/底部监听
        dataBinding.chatRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastItemPos = layoutManager.itemCount - 1
                if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    // 到达顶部，加载旧数据
                    lifecycleScope.launch(Dispatchers.Main) {
                        val topChat = chatListAdapter?.returnTopChat() ?: return@launch
                        //LogUtil.info("顶部消息 -> ${Gson().toJson(topChat)}")
                        val list = withContext(Dispatchers.IO) {
                            ChatListHelper.loadHistory10Msg(this@ChatActivity, topChat)
                        }
                        //LogUtil.info("历史消息 -> ${Gson().toJson(list)}")
                        chatListAdapter?.loadMore(list)
                    }
                } else if (layoutManager.findLastVisibleItemPosition() == lastItemPos) {

                }
            }
        })

        //消息接收
        WebSocketManager.instance.receiveMessage(this) {
            lifecycleScope.launch(Dispatchers.Main) {
                //viewModel.historyMsg.value?.add(chat)
                chatListAdapter!!.addNewMsg(it) { i ->
                    dataBinding.chatRecycleView.scrollToPosition(i.size - 1)
                }
//                withContext(Dispatchers.IO) {
//                    MainUserSelectHelper.insertProfile(
//                        this@ChatActivity,
//                        viewModel.receiver.value!!
//                    )
//                }
            }
        }
    }

    private fun uploadImg(file: File, data: LocalMedia) {
        //LogUtil.info("开始上传")
        val fileSize = ApiServiceHelper.getRequestBody(data.size, "text/plain")
        val fileName = ApiServiceHelper.getRequestBody(data.fileName, "text/plain")
        val fileType = ApiServiceHelper.getRequestBody(data.mimeType, "text/plain")
        val userId = ApiServiceHelper.getRequestBody(UserStatusUtil.getUserId(), "text/plain")
        val fileData = ApiServiceHelper.getRequestBodyPart(file, data.mimeType)

        LogUtil.info("文件类型: ${data.mimeType}")


        ApiServiceHelper.service().uploadFile(
            fileData,
            userId,
            fileName,
            fileType,
            fileSize
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MyObservable<ResBean<String>>() {

                override fun success(res: ResBean<String>) {
                    //LogUtil.info(Gson().toJson(res))
                    // res.data -> 2/20240912/20240912_W7ZYt5OnmdCY.jpg
                    WebSocketManager.instance.sendMsg(
                        generateChat(HttpUrl.IMG_URL + res.data, MessageType.IMAGE.type)
                    )
                }

                override fun failed(e: Throwable) {
                    Log.e("xht", "failed: ", e)
                }
            })
    }

    //加载特定位置的起的10条数据 (默认最近10条消息)
    private fun loadMsg(startIdx: Int = 0) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (startIdx == 0) loadNewestMsg()
            //else loadMoreMsg(startIdx)
        }
    }

    private suspend fun loadNewestMsg() {
        var list = withContext(Dispatchers.IO) {
            viewModel.receiver.value?.email?.let {
                ChatListHelper.loadRecentMsg(
                    this@ChatActivity, it
                )
            }
        }

        loading(false)

        if (list == null) list = mutableListOf()
        chatListAdapter = ChatAdapter(mutableListOf(), viewModel.receiver.value?.avatar, this)

        dataBinding.chatRecycleView.adapter = chatListAdapter

        chatListAdapter!!.setChatList(list) {
            dataBinding.chatRecycleView.scrollToPosition(it.size - 1)
        }
        // 强制滚动到底部
        delay(500)
        chatListAdapter?.scrollToLastIdx {
            dataBinding.chatRecycleView.scrollToPosition(it)
        }
    }

    //
    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            dataBinding.chatProgressBar.visibility = View.VISIBLE
        } else {
            dataBinding.chatProgressBar.visibility = View.INVISIBLE
        }
    }

    //
    private fun changeMoreWrapperStyle(forceClose: Boolean = false) {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(300)
            if (forceClose) {
                dataBinding.layoutMoreWrapper.visibility = View.GONE
                return@launch
            }

            if (dataBinding.layoutMoreWrapper.visibility == View.VISIBLE) {
                dataBinding.layoutMoreWrapper.visibility = View.GONE
            } else {
                dataBinding.layoutMoreWrapper.visibility = View.VISIBLE
            }
        }
    }

    //
    private fun generateChat(msg: String, type: Int = 0): ChatBean {
        val chatBean = ChatBean()
        chatBean.receiver = viewModel.receiver.value?.email ?: ""
        chatBean.receiverName = viewModel.receiver.value?.username

        chatBean.sender = UserStatusUtil.getCurLoginUser()
        chatBean.senderName = UserStatusUtil.getUsername()
        //聊天记录所有者
        chatBean.owner = UserStatusUtil.getCurLoginUser()
        chatBean.type = type

        val msgType = MessageType.getDescFromType(type)
        if (msgType == null) chatBean.message = msg
        else chatBean.message = "$msgType:$msg"

        return chatBean
    }

    override fun preview() {

    }
}