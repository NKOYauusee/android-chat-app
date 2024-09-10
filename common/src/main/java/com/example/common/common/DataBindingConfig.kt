package com.example.common.common

import android.util.SparseArray

class DataBindingConfig(val layout: Int, val vmVariableId: Int = -1) {
    val bindingParams = SparseArray<Any>()

    fun addBindingParam(variableId: Int, obj: Any): DataBindingConfig {
        if (bindingParams[variableId] == null) {
            bindingParams.put(variableId, obj)
        }
        return this
    }
}