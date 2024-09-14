package com.example.mychatapp.activities

import android.content.Intent
import android.os.Bundle
import com.example.common.common.Constants
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.LogUtil
import com.example.common.viewmodel.BaseViewModel
import com.example.database.bean.ChatBean
import com.example.database.bean.UserFriBean
import com.example.mychatapp.R
import com.example.mychatapp.adapter.search.ChatListSearchAdapter
import com.example.mychatapp.databinding.ActivitySearchChatBinding
import com.example.mychatapp.listener.SearchChatListener

class SearchChatActivity : BaseActivity<ActivitySearchChatBinding, BaseViewModel>(),
    SearchChatListener {

    private var chatListSearchAdapter: ChatListSearchAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadBasicData()

        initListener()
    }

    private fun initListener(){
        dataBinding.imgBack.setOnClickListener{
            finish()
        }
    }

    private fun loadBasicData() {
        val userFriBean = intent.getSerializableExtra("who") as UserFriBean
        val chatList = intent.getSerializableExtra("chatList") as ArrayList<ChatBean>
        val searchItem = intent.getSerializableExtra("searchItem") as String

        chatListSearchAdapter = ChatListSearchAdapter(searchItem, userFriBean, chatList, this)

        dataBinding.chatListRecycleView.adapter = chatListSearchAdapter
        dataBinding.whichOne.text = userFriBean.username
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_search_chat)
    }

    override fun toChatActivityWithData(userFriBean: UserFriBean, chat: ChatBean) {
        val intent = Intent(this, ChatActivity::class.java)

        intent.putExtra(Constants.CHAT_FRIEND, userFriBean)
        intent.putExtra(Constants.CHAT_START, chat)

        switchActivity(
            intent,
            R.anim.enter_animation,
            R.anim.exit_fade_out_ani,
            true
        )
    }

}