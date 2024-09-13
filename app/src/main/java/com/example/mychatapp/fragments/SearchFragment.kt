package com.example.mychatapp.fragments

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.common.common.Constants
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.ui.BaseFragment
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.common.viewmodel.BaseViewModel
import com.example.database.bean.ChatBean
import com.example.database.bean.UserFriBean
import com.example.database.helper.ChatListHelper
import com.example.database.helper.UserFriendHelper
import com.example.mychatapp.R
import com.example.mychatapp.activities.ChatActivity
import com.example.mychatapp.adapter.search.SearchChatAdapter
import com.example.mychatapp.adapter.search.SearchFriendAdapter
import com.example.mychatapp.databinding.FragmentSearchBinding
import com.example.mychatapp.listener.UserListener
import com.example.mychatapp.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : BaseFragment<FragmentSearchBinding, BaseViewModel>() {
    private var chatList = mutableListOf<MutableList<ChatBean>>()
    private var chatAdapter: SearchChatAdapter? = null
    private var friendList = mutableListOf<UserFriBean>()
    private var friendAdapter: SearchFriendAdapter? = null
    //var groupList = mutableListOf<ChatBean>()

    private var searchJob: Job? = null


    override fun show() {
        // 观察 ViewModel 中 searchContent 的变化
        ViewModelProvider(requireActivity())[MainViewModel::class.java].searchContent.observe(
            viewLifecycleOwner
        ) { query ->
            Log.d("xht", "搜索内容: $query")
            if (!query.isNullOrEmpty())
                updateSearchContent(query)
        }
    }

    override fun hide() {
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_search)
    }

    private fun init() {
        chatList = mutableListOf()
        chatAdapter = null
        friendList = mutableListOf()
        friendAdapter = null
    }

    private fun updateSearchContent(searchContent: String) {
        LogUtil.info(searchContent)
        init()
        searchJob?.cancel()
        // 在主线程启动协程
        searchJob = lifecycleScope.launch {
            getRelativeFriend(searchContent)

            val res = getFriendList()
            //LogUtil.info("当前好友数量 ${res.size}")
            getRelativeRes(searchContent, res)
        }
    }

    // 查询可能匹配的好友
    private suspend fun getRelativeFriend(searchContent: String) {
        //if (context is BaseActivity<*, *>)
        context as BaseActivity<*, *>

        lifecycleScope.launch(Dispatchers.Main) {
            // 在 IO 线程执行数据库操作
            val res = withContext(Dispatchers.IO) {
                context?.let {
                    UserFriendHelper.selectFriendsByWords(
                        it,
                        UserStatusUtil.getCurLoginUser(),
                        searchContent
                    )
                }
            }


            if (!res.isNullOrEmpty()) {
                // 在主线程进行 UI 操作
                friendList.addAll(res)
                friendAdapter = SearchFriendAdapter(
                    searchContent,
                    res,
                    object : UserListener {
                        override fun onUserClicked(friend: UserFriBean) {
                            // TODO
                            val intent = Intent(requireActivity(), ChatActivity::class.java)
                            intent.putExtra(Constants.CHAT_FRIEND, friend)

                            clearSearchContent()

                            switchActivity(
                                intent,
                                R.anim.enter_animation,
                                R.anim.exit_fade_out_ani,
                                hideSelf = true
                            )
                        }

                        override fun blackListFriend(
                            friend: UserFriBean,
                            callback: () -> Unit
                        ) {
                        }

                        override fun deleteFriend(friend: UserFriBean, callback: () -> Unit) {
                        }

                        override fun preventLongClick(callback: (prevent: Boolean) -> Unit) {
                        }

                    }
                )

                dataBinding.searchFriendRecycleView.adapter = friendAdapter

                dataBinding.searchFriendWrapper.visibility = View.VISIBLE

            } else {
                dataBinding.searchFriendWrapper.visibility = View.GONE
                //HasSearchRes(false)
            }
        }
    }

    private suspend fun getFriendList(): MutableList<UserFriBean> {
        //if (context is BaseActivity<*, *>)
        context as BaseActivity<*, *>
        // 在 IO 线程执行数据库操作
        val res = withContext(Dispatchers.IO) {
            context?.let {
                UserFriendHelper.selectFriends(
                    it,
                    UserStatusUtil.getCurLoginUser(),
                )
            }
        }

        return res ?: mutableListOf()
    }


    // 查询可能的聊天记录
    private fun getRelativeRes(content: String, list: MutableList<UserFriBean>) {
        if (list.size == 0) {
            dataBinding.searchChatWrapper.visibility = View.GONE
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            val friends = mutableListOf<UserFriBean>()
            for (fri in list) {
                val friChat = withContext(Dispatchers.IO) {
                    context?.let {
                        ChatListHelper.loadRelativeMsgWithSb(
                            it,
                            UserStatusUtil.getCurLoginUser(),
                            fri.email,
                            content
                        )
                    }
                }

                if (!friChat.isNullOrEmpty()) {
                    chatList.add(friChat)
                    friends.add(fri)
                }

//                LogUtil.info("friends: ${Gson().toJson(friends)}")
//                LogUtil.info("chats: ${Gson().toJson(chatList)}")
            }

            if (friends.isEmpty()) {
                dataBinding.searchChatWrapper.visibility = View.GONE
                chatList = mutableListOf()
                return@launch
            }
            // TODO 装载 adapter
            chatAdapter = SearchChatAdapter(content, friends, chatList)
            dataBinding.searchChatRecycleView.adapter = chatAdapter
            dataBinding.searchChatWrapper.visibility = View.VISIBLE
        }
    }

    private fun clearSearchContent() {
        ViewModelProvider(requireActivity())[MainViewModel::class.java].searchContent.postValue("")
    }


//    companion object {
//        val instance by lazy {
//            SearchFragment()
//        }
//    }

}