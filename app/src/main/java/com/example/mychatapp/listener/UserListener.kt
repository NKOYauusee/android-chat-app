package com.example.mychatapp.listener

import android.widget.CheckBox
import com.example.database.bean.UserFriBean
import com.example.mychatapp.viewmodel.UserViewModel


interface UserListener {
    fun onUserClicked(friend: UserFriBean)
}