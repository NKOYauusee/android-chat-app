package com.example.mychatapp.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.common.common.Constants
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.UserStatusUtil
import com.example.database.bean.ChatBean
import com.example.database.bean.UserFriBean
import com.example.database.helper.ChatListHelper
import com.example.mychatapp.R
import com.example.mychatapp.adapter.ChatAdapter
import com.example.mychatapp.databinding.ActivityChatBinding
import com.example.mychatapp.viewmodel.ChatViewModel
import com.example.mychatapp.websocket.WebSocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : BaseActivity<ActivityChatBinding, ChatViewModel>() {
    private var chatListAdapter: ChatAdapter? = null
    //private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initListener()

        loadReceiver()
    }

    override fun onResume() {
        super.onResume()
        loadMsg()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_chat)
    }

    // 加载对方用户基本信息
    private fun loadReceiver() {
        val curChatUser = intent.getSerializableExtra(Constants.CHAT_FRIEND) as UserFriBean?

        //LogUtil.info("loadReceiver " + gson.toJson(curChatUser))
        viewModel.receiver.postValue(curChatUser)
        dataBinding.chatName.text = curChatUser?.username
    }

    private fun initListener() {
        // 返回
        dataBinding.imgBack.setOnClickListener {
//            chatListAdapter?.saveChatBeanList {
//                GlobalScope.launch(Dispatchers.IO) {
//                    ChatListHelper.saveChats(this@ChatActivity, it)
//                }
//            }
            finish()
        }


        // 发送消息
        dataBinding.layoutSendMsg.setOnClickListener {
            val msg = dataBinding.inputMsg.text.toString()
            if (msg.isEmpty())
                return@setOnClickListener

            val chatBean = ChatBean()
            chatBean.message = msg

            chatBean.receiver = viewModel.receiver.value?.email
            chatBean.receiverName = viewModel.receiver.value?.username

            chatBean.sender = UserStatusUtil.getCurLoginUser()
            chatBean.senderName = UserStatusUtil.getUsername()
            //聊天记录所有者
            chatBean.owner = UserStatusUtil.getCurLoginUser()

            lifecycleScope.launch(Dispatchers.IO) {
                WebSocketManager.instance.sendMsg(chatBean)
            }
            dataBinding.inputMsg.setText("")
        }
    }

    //加载最近10条消息
    private fun loadMsg() {
        lifecycleScope.launch(Dispatchers.Main) {
            var list = withContext(Dispatchers.IO) {
                viewModel.receiver.value?.email?.let {
                    ChatListHelper.loadRecentMsg(
                        this@ChatActivity,
                        it
                    )
                }
            }

            loading(false)

            if (list == null) list = mutableListOf()
            //val list = mutableListOf<ChatBean>();
            chatListAdapter = ChatAdapter(list, null)
            dataBinding.chatRecycleView.adapter = chatListAdapter

            chatListAdapter?.getLastIdx()
                ?.let { dataBinding.chatRecycleView.scrollToPosition(it) }
        }

        //消息接收
        WebSocketManager.instance.loadMsg(this) {
            lifecycleScope.launch(Dispatchers.Main) {
                //viewModel.historyMsg.value?.add(chat)
                chatListAdapter!!.addNewMsg(it) { i ->
                    dataBinding.chatRecycleView.scrollToPosition(i.size - 1)
                }
            }
        }

        //val mManager: LinearLayoutManager = LinearLayoutManager(mContext)
        //mManager.stackFromEnd = true
        //dataBinding.chatRecycleView.layoutManager = mManager

        dataBinding.chatRecycleView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                lifecycleScope.launch(Dispatchers.Main) {
                    chatListAdapter?.getLastIdx()
                        ?.let { dataBinding.chatRecycleView.scrollToPosition(it) }
                }
            }
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            dataBinding.chatProgressBar.visibility = View.VISIBLE
        } else {
            dataBinding.chatProgressBar.visibility = View.INVISIBLE
        }
    }
}