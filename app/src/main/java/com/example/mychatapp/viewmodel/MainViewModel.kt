package com.example.mychatapp.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.common.viewmodel.BaseViewModel

class MainViewModel(mApplication: Application) : BaseViewModel(mApplication) {
    // 是否有好友申请 处理结果 TODO （2天后应自动删除）
    val hasFriendApply = MutableLiveData<Boolean>(false)
    val searchIsClicked = MutableLiveData<Boolean>(false)

    val searchContent = MutableLiveData<String>()
    val searchHasRes = MutableLiveData<Boolean>(false)


    val username = MutableLiveData<String>("")
    val userEmail = MutableLiveData<String>("")
}