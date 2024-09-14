package com.example.api.service

import com.example.api.bean.ResBean
import com.example.database.bean.ChatBean
import com.example.database.bean.FriendApply
import com.example.database.bean.UserBean
import com.example.database.bean.UserFriBean
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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
    ): Observable<ResBean<UserBean>>

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
    @FormUrlEncoded
    @POST("fri/delFriend")
    fun deleteFri(
        @Field("email") email: String, @Field("friend") friend: String
    ): Observable<ResBean<Nothing>>

    // 批量删除好友
    @POST("fri/batchDelFriends")
    fun batchDeleteFri(
        @Body friendList: List<UserFriBean>,
    ): Observable<ResBean<Nothing>>

    @POST("fri/setFriStatus")
    fun setFriendStatus(@Body userFriBean: UserFriBean): Observable<ResBean<Nothing>>


    //文件上传
    @Multipart
    @POST("media/upload")
    fun uploadFile(
        @Part fileData: MultipartBody.Part,
        @Part("userId") userId: RequestBody,
        @Part("fileName") fileName: RequestBody,
        @Part("fileType") fileType: RequestBody,
        @Part("fileSize") fileSize: RequestBody
    ): Observable<ResBean<String>>


    // 注册 带头像上传
    @Multipart
    @POST("user/registerWithProfile")
    fun registerWithProfile(
        @Part fileData: MultipartBody.Part,
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("code") code: RequestBody
    ): Observable<ResBean<UserBean>>


    // 头像上传
    @Multipart
    @POST("user/uploadProfile")
    fun uploadProfile(
        @Part fileData: MultipartBody.Part,
        @Part("userId") userId: RequestBody,
        @Part("email") email: RequestBody,
    ): Observable<ResBean<String>>

    // 搜索结果
    @FormUrlEncoded
    @POST("user/search")
    fun searchRes(
        @Field("searchContent") searchContent: String
    ): Observable<ResBean<MutableList<UserBean>>>

    // 好友申请
    @POST("fri/addFriend")
    fun addFriendApply(@Body apply: FriendApply): Observable<ResBean<Nothing>>
}