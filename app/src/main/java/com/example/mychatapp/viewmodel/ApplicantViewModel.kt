package com.example.mychatapp.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.common.viewmodel.BaseViewModel

class ApplicantViewModel(application: Application) : BaseViewModel(application) {
    val hasResult = MutableLiveData<Boolean>(true)
}