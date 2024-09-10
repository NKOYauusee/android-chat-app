package com.example.mychatapp.activities

import android.os.Bundle
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.LogUtil
import com.example.common.util.ToastUtil
import com.example.common.viewmodel.BaseViewModel
import com.example.mychatapp.R
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.ActivitySignUpBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class SignUpActivity : BaseActivity<ActivitySignUpBinding, BaseViewModel>() {
    private var registerRes: MyObservable<ResBean<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
        initListener()
    }

    private fun init() {
        registerRes = object : MyObservable<ResBean<String>>() {
            override fun success(res: ResBean<String>) {
                if (res.code != 200) {
                    res.info?.let {
                        MyToast(mContext).show(it)
                    }
                        ?: MyToast(mContext).show("注册失败")

                    return
                }

                ToastUtil.showToastMsg("注册成功", mContext)
                switchActivity(
                    this@SignUpActivity,
                    SignInActivity::class.java,
                    R.anim.enter_animation,
                    R.anim.exit_animation, true
                )
            }

            override fun failed(e: Throwable) {
                LogUtil.error("注册失败->$e")
                MyToast(mContext).show("注册失败")
            }
        }
    }

    private fun initListener() {
        dataBinding.btJumpToLogin.setOnClickListener {
            switchActivity(
                this,
                SignInActivity::class.java,
                R.anim.enter_animation,
                R.anim.exit_animation, true
            )
        }

        dataBinding.btSingUp.setOnClickListener {
            val email = dataBinding.registerEmail.text.toString()
            val username = dataBinding.registerName.text.toString()
            val pwd = dataBinding.registerPwd.text.toString()
            val confirmedPwd = dataBinding.registerConfirmPwd.text.toString()

            if (pwd != confirmedPwd) {
                MyToast(mContext).show("两次密码输入不一致，请检查输入")
                //ToastUtil.showToastMsg("两次密码输入不一致，请检查输入", mContext)
            } else {
                ApiServiceHelper.service()
                    .register(
                        email = email,
                        username = username,
                        password = pwd, code = ""
                    ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(registerRes!!)
            }
        }
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_sign_up)
    }
}