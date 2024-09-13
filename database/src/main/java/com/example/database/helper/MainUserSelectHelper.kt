package com.example.database.helper

import android.content.Context
import com.example.common.util.UserStatusUtil
import com.example.database.UserDatabase
import com.example.database.bean.ChatBean
import com.example.database.bean.HasChatBean
import com.example.database.bean.MessageType

object MainUserSelectHelper {
    fun load(context: Context, user: String): MutableList<HasChatBean> {
        //res.reverse()
        return UserDatabase.getInstance(context).getMainChatDao().selectAll(user)
    }

    fun insert(context: Context, hasChatBean: HasChatBean) {
        UserDatabase.getInstance(context).getMainChatDao().insert(hasChatBean)
    }

    fun delete(context: Context, hasChatBean: HasChatBean) {
        UserDatabase.getInstance(context).getMainChatDao().delete(hasChatBean)
    }

    fun deleteMainHasChatSow(context: Context, owner: String, who: String) {
        UserDatabase.getInstance(context).getMainChatDao().deleteMainChatShow(owner, who)
    }

    fun updateMsgAndTime(context: Context, msg: String, date: Long, email: String) {
        UserDatabase.getInstance(context).getMainChatDao().update(msg, date, email)
    }

    fun updateMainUser(
        context: Context,
        chatBean: ChatBean

    ) {
        val hasChatBean = HasChatBean()

        hasChatBean.user = UserStatusUtil.getCurLoginUser()
        hasChatBean.newMsg = MessageType.getDescFromType(chatBean.type) ?: chatBean.message
        hasChatBean.sendTime = chatBean.sendTime

        //对方邮箱
        hasChatBean.email =
            if (chatBean.sender == hasChatBean.user) chatBean.receiver else chatBean.sender
        hasChatBean.nickname =
            if (chatBean.sender == hasChatBean.user) chatBean.receiverName else chatBean.senderName
        // TODO 头像根据 email
        hasChatBean.avatar = hasChatBean.email
        hasChatBean.isRead = chatBean.sender == UserStatusUtil.getCurLoginUser()
        //LogUtil.info("待插入 -> ${gson.toJson(hasChatBean)}")
        insert(context, hasChatBean)
    }
}