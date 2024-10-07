package com.example.database.helper

import android.content.Context
import com.example.common.common.Constants
import com.example.common.util.GroupUtil
import com.example.common.util.UserStatusUtil
import com.example.database.UserDatabase
import com.example.database.bean.ChatBean
import java.util.Date

object ChatListHelper {
    // 加载之前的10条消息
    fun loadHistory10Msg(context: Context, chatBean: ChatBean): MutableList<ChatBean> {
        val res = if (GroupUtil.isGroup(chatBean.receiver)) {
            UserDatabase.getInstance(context).getChatDao()
                .loadHistory10MsgGroup(chatBean.owner, chatBean.sendTime, chatBean.receiver)
        } else {
            val friend =
                if (chatBean.owner == chatBean.sender) chatBean.receiver else chatBean.sender
            UserDatabase.getInstance(context).getChatDao()
                .loadHistory10Msg(chatBean.owner, chatBean.sendTime, friend)
        }

        res.reverse()
        return res
    }

    // 加载和某人最近2天的10条聊天数据
    fun loadRecentMsg(context: Context, friend: String): MutableList<ChatBean> {
        val date = Date().time - Constants.ONE_DAY * 2
        val key = UserStatusUtil.getCurLoginUser()

        val res = if (GroupUtil.isGroup(friend)) {
            UserDatabase.getInstance(context).getChatDao()
                .loadRecentMsgGroup(key, date, friend)
        } else {
            UserDatabase.getInstance(context).getChatDao().loadRecentMsg(key, date, friend)
        }


        res.reverse()
        return res
    }


    fun loadSpecificMsg(context: Context, friend: String, startDate: Long): MutableList<ChatBean> {
        val key = UserStatusUtil.getCurLoginUser()

        return if (GroupUtil.isGroup(friend)) {
            UserDatabase.getInstance(context).getChatDao()
                .loadSpecificMsgGroup(key, startDate, friend)
        } else {
            UserDatabase.getInstance(context).getChatDao()
                .loadSpecificMsg(key, startDate, friend)
        }
    }


    fun loadNewMsg(context: Context, friend: String, startDate: Long): MutableList<ChatBean> {
        val key = UserStatusUtil.getCurLoginUser()
        return if (GroupUtil.isGroup(friend)) {
            UserDatabase.getInstance(context).getChatDao().loadNewMsgGroup(key, startDate, friend)
        } else {
            UserDatabase.getInstance(context).getChatDao().loadNewMsg(key, startDate, friend)
        }
    }

    // 保存聊天数据
    fun saveChats(
        context: Context, mutableList: MutableList<ChatBean>
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

    // 搜索聊天记录
    fun loadRelativeMsgWithSb(
        context: Context, owner: String, who: String, keyword: String
    ): MutableList<ChatBean> {

        return UserDatabase.getInstance(context).getChatDao()
            .loadRelativeMsgWithSb(owner, who, keyword)
    }

    fun deleteOneMsg(context: Context, chatBean: ChatBean) {
        UserDatabase.getInstance(context).getChatDao().deleteOneMsg(chatBean)
    }
}