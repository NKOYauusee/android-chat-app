package com.example.database.helper

import android.content.Context
import com.example.common.util.GroupUtil
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.UserDatabase
import com.example.database.bean.ChatBean
import com.example.database.bean.HasChatBean
import com.example.database.bean.MessageType
import com.example.database.bean.UserFriBean

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
        UserDatabase.getInstance(context).getMainChatDao()
            .update(UserStatusUtil.getCurLoginUser(), msg, date, email)
    }

    fun updateMainUser(
        context: Context,
        chatBean: ChatBean
    ) {
        val hasChatBean = HasChatBean()

        hasChatBean.user = UserStatusUtil.getCurLoginUser()
        hasChatBean.newMsg = MessageType.getDescFromType(chatBean.msgType) ?: chatBean.message
        hasChatBean.sendTime = chatBean.sendTime

        //对方邮箱
        if (!GroupUtil.isGroup(chatBean.receiver)) {
            hasChatBean.email =
                if (chatBean.sender == hasChatBean.user) chatBean.receiver else chatBean.sender

            hasChatBean.nickname =
                if (chatBean.sender == hasChatBean.user) chatBean.receiverName else chatBean.senderName
        } else {
            hasChatBean.email = chatBean.receiver
            hasChatBean.nickname = chatBean.receiverName
        }


        LogUtil.info("nko -> ${hasChatBean.email}")

        hasChatBean.isRead = chatBean.sender == UserStatusUtil.getCurLoginUser()

        hasChatBean.avatar = UserFriendHelper.selectFriendAvatar(
            context,
            UserStatusUtil.getCurLoginUser(),
            hasChatBean.email
        )

        //LogUtil.info("待插入 -> ${hasChatBean.isRead}")
        insert(context, hasChatBean)
    }

    fun insertProfile(context: Context, friend: UserFriBean) {
        friend.avatar?.let {
            UserDatabase.getInstance(context).getMainChatDao()
                .insertProfile(UserStatusUtil.getCurLoginUser(), friend.email, it)
        }
    }

}