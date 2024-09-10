package com.example.api.service

import com.example.api.bean.ResBean
import com.example.database.bean.ChatBean
import com.example.database.bean.FriendApply
import com.example.database.bean.UserBean
import com.example.database.bean.UserFriBean
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    // 免登录 token验证是否有效
    @FormUrlEncoded
    @POST("user/verifyToken")
    fun verifyToken(
        @Field("email") email: String,
        @Field("token") token: String,
    ): Observable<ResBean<String>>

    // 登录
    @FormUrlEncoded
    @POST("user/login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("code") code: String
    ): Observable<ResBean<UserBean>>

    // 注册
    @FormUrlEncoded
    @POST("user/register")
    fun register(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("code") code: String
    ): Observable<ResBean<String>>

    // 获取用户的好友列表
    @FormUrlEncoded
    @POST("fri/getFriends")
    fun getUserFriend(
        @Field("email") email: String,
    ): Observable<ResBean<List<UserFriBean>>>

    // 获取用户离线时未接收的聊天消息
    @FormUrlEncoded
    @POST("user/loadOfflineMsg")
    fun loadUnReceiveMsg(
        @Field("key") key: String,
    ): Observable<ResBean<List<ChatBean>>>

    // 获取用户相关的好友申请数据
    @FormUrlEncoded
    @POST("fri/getApplyList")
    fun getFriendApplyStatus(@Field("email") email: String): Observable<ResBean<List<FriendApply>>>

    // 判断用户是否有相关的好友申请数据
    @FormUrlEncoded
    @POST("fri/getHasApplyList")
    fun getHasApplyList(@Field("email") email: String): Observable<ResBean<Int>>

    // 处理对方的好友申请（添加好友 拒绝..）
    @POST("fri/setApplyStatus")
    fun setApplyStatus(@Body friendApply: FriendApply): Observable<ResBean<Nothing>>

    // 删除好友

}