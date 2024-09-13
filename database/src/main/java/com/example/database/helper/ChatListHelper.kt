package com.example.database.helper

import android.content.Context
import com.example.common.common.Constants
import com.example.common.util.UserStatusUtil
import com.example.database.UserDatabase
import com.example.database.bean.ChatBean
import java.util.Date

object ChatListHelper {
    // 加载之前的10条消息
    fun loadHistory10Msg(context: Context, chatBean: ChatBean): MutableList<ChatBean> {
        val friend = if (chatBean.owner == chatBean.sender) chatBean.receiver else chatBean.sender

        val res = UserDatabase.getInstance(context).getChatDao()
            .loadHistory10Msg(chatBean.owner, chatBean.sendTime, friend)

        res.reverse()
        return res
    }

    // 加载和某人最近2天的10条聊天数据
    fun loadRecentMsg(context: Context, friend: String): MutableList<ChatBean> {
        val date = Date().time - Constants.ONE_DAY * 2
        val key = UserStatusUtil.getCurLoginUser()
        val res = UserDatabase.getInstance(context).getChatDao().loadRecentMsg(key, date, friend)
        res.reverse()
        return res
    }

    // 保存聊天数据
    fun saveChats(
        context: Context,
        mutableList: MutableList<ChatBean>
    ) {
        UserDatabase.getInstance(context).getChatDao().insertChats(mutableList)
    }

    fun saveOneChat(
        context: Context,
        chatBean: ChatBean,
    ) {
        UserDatabase.getInstance(context).getChatDao().insertChat(chatBean)
    }

    fun loadNewestMsg(context: Context, key: String) {
        val chatBean = UserDatabase.getInstance(context).getChatDao().loadNewestMsg(key)
        MainUserSelectHelper.updateMainUser(context, chatBean)
    }

    fun loadRelativeMsgWithSb(
        context: Context,
        owner: String,
        who: String,
        keyword: String
    ): MutableList<ChatBean> {

        return UserDatabase.getInstance(context).getChatDao()
            .loadRelativeMsgWithSb(owner, who, keyword)
    }
}