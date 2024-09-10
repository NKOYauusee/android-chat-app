package com.example.mychatapp.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.common.viewmodel.BaseViewModel
import com.example.database.bean.UserFriBean

class UserViewModel(mApplication: Application) : BaseViewModel(mApplication) {
    val userList = MutableLiveData<List<UserFriBean>>()
    val isClickBatchManage = MutableLiveData<Boolean>(false)
}