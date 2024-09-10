package com.example.mychatapp.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.common.viewmodel.BaseViewModel

class FriendApplyViewModel(mApplication: Application) : BaseViewModel(mApplication) {
    val hasApplyData = MutableLiveData<Boolean>(true)
}