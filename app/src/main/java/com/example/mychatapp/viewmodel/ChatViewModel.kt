package com.example.mychatapp.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.common.viewmodel.BaseViewModel
import com.example.database.bean.UserFriBean

class ChatViewModel(mApplication: Application) : BaseViewModel(mApplication) {
    val receiver = MutableLiveData<UserFriBean>()
}