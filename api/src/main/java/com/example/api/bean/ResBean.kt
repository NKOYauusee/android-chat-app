package com.example.api.bean

class ResBean<T> {
    var code: Int = 0;
    var status: String = ""
    var info: String? = null
    var data: T? = null
}