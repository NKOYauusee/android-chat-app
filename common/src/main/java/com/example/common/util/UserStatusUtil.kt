package com.example.common.util

import com.example.common.common.Constants

object UserStatusUtil {

    fun setIsSignIn(isLogin: Boolean) {
        SpUtil.setParam(Constants.IS_SIGN_IN, isLogin)
    }

    fun getIsSignIn(): Boolean {
        return SpUtil.getParam(Constants.IS_SIGN_IN, false)
    }

    fun setCurLoginUser(email: String) {
        SpUtil.setParam(Constants.CURRENT_USER, email)
    }

    fun getCurLoginUser(): String {
        return SpUtil.getParam(Constants.CURRENT_USER, "")
    }

    fun setLoginToken(token: String) {
        SpUtil.setParam(Constants.LOGIN_TOKEN, token)
    }

    fun getLoginToken(): String {
        return SpUtil.getParam(Constants.LOGIN_TOKEN, "")
    }

    fun setUsername(username: String) {
        SpUtil.setParam(Constants.USERNAME, username)
    }

    fun getUsername(): String {
        return SpUtil.getParam(Constants.USERNAME, "")
    }

    fun setUserPhone(phone: String) {
        SpUtil.setParam(Constants.USER_PHONE, phone)
    }

    fun getUserPhone(): String {
        return SpUtil.getParam(Constants.USER_PHONE, "")
    }

    fun setUserAvatar(avatar: String) {
        SpUtil.setParam(Constants.USER_AVATAR, avatar)
    }

    fun getUserAvatar(): String {
        return SpUtil.getParam(Constants.USER_AVATAR, "")
    }

    fun setUserId(id: Int) {
        SpUtil.setParam(Constants.USER_ID, -1)
    }

    fun getUserId(): Int {
        return SpUtil.getParam(Constants.USER_ID, -1)
    }


}