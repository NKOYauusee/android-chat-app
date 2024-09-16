package com.example.mychatapp.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.common.viewmodel.BaseViewModel

class UserViewModel(mApplication: Application) : BaseViewModel(mApplication) {
    val isClickBatchManage = MutableLiveData<Boolean>(false)
    val hasFriend = MutableLiveData<Boolean>(false)
}