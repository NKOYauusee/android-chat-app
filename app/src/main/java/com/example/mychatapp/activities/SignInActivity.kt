package com.example.mychatapp.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.LogUtil
import com.example.common.util.SettingUtil
import com.example.common.util.UserStatusUtil
import com.example.common.viewmodel.BaseViewModel
import com.example.database.bean.UserBean
import com.example.mychatapp.MainActivity
import com.example.mychatapp.R
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.ActivitySignInBinding
import com.example.mychatapp.util.HttpHelper
import com.example.mychatapp.util.UserUtil.setLoginStatus
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SignInActivity : BaseActivity<ActivitySignInBinding, BaseViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
        initListener()

        SettingUtil.initSystemUserSetting()
    }

    override fun onResume() {
        super.onResume()
        // 初始化
        initAppSetting()
        // 免登录操作
        verifyToken()
    }

    private fun initAppSetting() {
        LogUtil.info(HttpHelper.getHttpBaseUrl())
        ApiServiceHelper.initService(HttpHelper.getHttpBaseUrl())
    }

    private fun init() {
        // 请求结果回调
    }

    private fun initListener() {
        // 创建账户
        dataBinding.createNewAccount.setOnClickListener {
            switchActivity(
                this,
                SignUpActivity::class.java,
                R.anim.enter_animation,
                R.anim.exit_animation,
                true
            )
        }

        dataBinding.settingBtn.setOnClickListener {
            switchActivity(
                this,
                SettingActivity::class.java,
                R.anim.enter_animation,
                R.anim.exit_animation,
                false
            )
        }

        // 登录
        dataBinding.btSingIn.setOnClickListener {

            val email = dataBinding.loginEmail.text.toString()
            val pwd = dataBinding.loginPassword.text.toString()
            if (email.isEmpty() || pwd.isEmpty()) {
                MyToast(this).show(getString(R.string.info_check_input))
                return@setOnClickListener
            }
            Log.d("xht", "email: $email, pwd: $pwd")

            // TODO 输入校验
            loading()
            // 登录请求
            login(email, pwd)
        }
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_sign_in)
    }

    private fun login(email: String, pwd: String, code: String = "") {
        ApiServiceHelper.service().login(email, pwd, code).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MyObservable<ResBean<UserBean>>() {
                override fun success(res: ResBean<UserBean>) {
                    if (res.code != 200 || res.data == null || res.data!!.token.isNullOrEmpty()) {
                        loading(false)
                        MyToast(mContext).show(getString(R.string.info_login_fail))
                        LogUtil.error("登录失败 -> " + Gson().toJson(res))
                        return
                    }

                    loading(false)

                    setLoginStatus(res.data!!, true)
                    //ToastUtil.showToastMsg("登录成功", mContext)
                    Log.d("xht", "登录成功: ${Gson().toJson(res.data)}")

                    switchActivity(
                        this@SignInActivity,
                        MainActivity::class.java,
                        R.anim.enter_animation,
                        R.anim.exit_animation,
                        true
                    )
                }

                override fun failed(e: Throwable) {
                    Log.d("xht", "login failed", e)
                    MyToast(mContext).show(getString(R.string.info_login_fail))
                    loading(false)
                }
            })
    }

    private fun loading(isLoading: Boolean = true) {
        if (isLoading) {
            dataBinding.loginProgress.visibility = View.VISIBLE
            dataBinding.btSingIn.visibility = View.INVISIBLE
        } else {
            dataBinding.loginProgress.visibility = View.INVISIBLE
            dataBinding.btSingIn.visibility = View.VISIBLE
        }
    }

    //免登录操作
    private fun verifyToken() {
        LogUtil.info("免登录操作")
        val email = UserStatusUtil.getCurLoginUser()
        val token = UserStatusUtil.getLoginToken()

        if (email.isEmpty() || token.isEmpty()) {
            LogUtil.info("email / token 为空")
            return
        }

        verifyTokenJob = object : MyObservable<ResBean<String>>() {
            override fun success(res: ResBean<String>) {
                if (res.code != 200 || res.data.isNullOrEmpty() || !UserStatusUtil.getIsSignIn()) {
                    LogUtil.error("免登录失败> ${Gson().toJson(res)}")
                    return
                }

                //ToastUtil.showToastMsg("免登录成功", mContext)
                //MyToast(mContext).show("免登录成功")

                LogUtil.info("免登录成功: ${res.data}")

                UserStatusUtil.setLoginToken(res.data!!)
                UserStatusUtil.setIsSignIn(true)

                switchActivity(
                    this@SignInActivity,
                    MainActivity::class.java,
                    R.anim.enter_animation,
                    R.anim.exit_animation,
                    true
                )
            }

            override fun failed(e: Throwable) {
                LogUtil.error("免登录失败")
            }

        }

        ApiServiceHelper.service().verifyToken(email, token).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(verifyTokenJob!!)
    }

    private var verifyTokenJob: MyObservable<ResBean<String>>? = null
}