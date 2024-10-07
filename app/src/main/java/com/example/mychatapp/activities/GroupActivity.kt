package com.example.mychatapp.activities

import android.os.Bundle
import android.util.Log
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.UserStatusUtil
import com.example.common.viewmodel.BaseViewModel
import com.example.database.bean.GroupBean
import com.example.mychatapp.R
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.ActivityGroupBinding
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GroupActivity : BaseActivity<ActivityGroupBinding, BaseViewModel>() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initListener()
    }

    private fun initListener() {
        dataBinding.imgBack.setOnClickListener {
            finish()
        }

        dataBinding.btCreate.setOnClickListener {
            if (dataBinding.createName.text.toString().isEmpty()) {
                MyToast(this@GroupActivity).show("群名不能为空")
                return@setOnClickListener
            }


            val groupInfo: GroupBean = GroupBean()
            groupInfo.info = dataBinding.createInfo.text.toString()
            groupInfo.creator = UserStatusUtil.getCurLoginUser()
            groupInfo.name = dataBinding.createName.text.toString()
            groupInfo.avatar = ""
            Log.d("xht", "group: ${Gson().toJson(groupInfo)}")
            ApiServiceHelper.service().createGroup(groupInfo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MyObservable<ResBean<String>>() {
                    override fun success(res: ResBean<String>) {
                        if (res.code == 200) {
                            MyToast(this@GroupActivity).show("创建成功")
                            finish()
                        }

                        MyToast(this@GroupActivity).show("创建失败")
                    }

                    override fun failed(e: Throwable) {
                        MyToast(this@GroupActivity).show("网络不通畅，请稍后再试")
                    }
                })
        }
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_group)
    }
}