package com.example.common.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

open class BaseViewModel(
    application: Application
) : AndroidViewModel(application) {

    val mApplication = application
}