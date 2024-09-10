package com.example.mychatapp.util

import com.example.common.util.UserStatusUtil
import com.example.database.bean.UserBean

object UserUtil {

    fun setLoginStatus(userBean: UserBean, isLogin: Boolean) {
        UserStatusUtil.setIsSignIn(isLogin)

        // todo
        UserStatusUtil.setCurLoginUser(userBean.email)
        UserStatusUtil.setUsername(userBean.username!!)
        UserStatusUtil.setUserPhone(userBean.phone!!)
        UserStatusUtil.setLoginToken(userBean.token!!)
        UserStatusUtil.setUserAvatar(userBean.avatar!!)
        UserStatusUtil.setUserId(userBean.id!!)
    }
}