package com.example.mychatapp.activities

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.BitmapUtil
import com.example.common.util.LogUtil
import com.example.common.util.ToastUtil
import com.example.common.viewmodel.BaseViewModel
import com.example.database.bean.UserBean
import com.example.mychatapp.MainActivity
import com.example.mychatapp.R
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.ActivitySignUpBinding
import com.example.mychatapp.util.SelectMediaHelper
import com.example.mychatapp.util.SystemUtil
import com.example.mychatapp.util.UserUtil
import com.google.gson.Gson
import com.luck.picture.lib.entity.LocalMedia
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File


class SignUpActivity : BaseActivity<ActivitySignUpBinding, BaseViewModel>() {
    private var registerRes: MyObservable<ResBean<UserBean>>? = null

    private var fileData: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
        initListener()
    }

    private fun init() {
        registerRes = object : MyObservable<ResBean<UserBean>>() {
            override fun success(res: ResBean<UserBean>) {
                if (res.code != 200) {
                    res.info?.let {
                        MyToast(mContext).show(it)
                    }
                        ?: MyToast(mContext).show("注册失败")

                    return
                }

                ToastUtil.showToastMsg("注册成功", mContext)

                res.data?.let {
                    UserUtil.setLoginStatus(it, true)
                }
                LogUtil.info("data -> ${Gson().toJson(res.data)}")
                // TODO 跳转 主页 or 登录页

//                switchActivity(
//                    this@SignUpActivity,
//                    SignInActivity::class.java,
//                    R.anim.enter_animation,
//                    R.anim.exit_animation, true
//                )

                switchActivity(
                    this@SignUpActivity,
                    MainActivity::class.java,
                    R.anim.enter_animation,
                    R.anim.exit_animation, true
                )
            }

            override fun failed(e: Throwable) {
                Log.e(TAG, "failed: ", e)
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
                if (fileData == null || fileData!!.length() == 0L) {
                    ApiServiceHelper.service()
                        .register(
                            email = email,
                            username = username,
                            password = pwd, code = ""
                        ).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(registerRes!!)
                } else {
                    LogUtil.info("开始上传")
                    registerWithProfile(
                        fileData!!,
                        username = username,
                        email = email,
                        password = pwd,
                        code = "0"
                    )
                }
            }
        }

        dataBinding.imageProfile.setOnClickListener {
            SelectMediaHelper.selectMedia(this, 1) {
                it as ArrayList<LocalMedia>
                val profile = it[0]
                LogUtil.info(Gson().toJson(profile))
                val uri = Uri.parse("file:///${profile.cutPath}")
                fileData = File(profile.cutPath)

                Glide.with(this)
                    .load(uri)
                    .into(dataBinding.imageProfile)

                SystemUtil.saveToSystemGallery(this, profile.cutPath)
            }
        }

    }

    private fun registerWithProfile(
        file: File,
        email: String,
        username: String,
        password: String,
        code: String
    ) {
        //LogUtil.info("开始上传")
        val emailF = ApiServiceHelper.getRequestBody(email, "text/plain")
        val usernameF = ApiServiceHelper.getRequestBody(username, "text/plain")
        val passwordF = ApiServiceHelper.getRequestBody(password, "text/plain")
        val codeF = ApiServiceHelper.getRequestBody(code, "text/plain")
        val fileData = ApiServiceHelper.getRequestBodyPart(file, "image/*")

        ApiServiceHelper.service().registerWithProfile(
            fileData = fileData,
            username = usernameF,
            email = emailF,
            password = passwordF,
            code = codeF
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(registerRes!!)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_sign_up)
    }


    private fun savaToSystemPictures(filePath: String) {
        val bitmap: Bitmap = BitmapUtil.getBitmapFromLocalPath(filePath)
        BitmapUtil.saveImageToGallery(this, bitmap)
    }
}