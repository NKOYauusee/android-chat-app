package com.example.mychatapp.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.lifecycleScope
import com.example.common.common.AppManager
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.LogUtil
import com.example.common.util.SettingUtil
import com.example.common.util.ToastUtil
import com.example.mychatapp.BR
import com.example.mychatapp.R
import com.example.mychatapp.databinding.ActivitySettingBinding
import com.example.mychatapp.viewmodel.SettingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingActivity : BaseActivity<ActivitySettingBinding, SettingViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.initServerInfo()
        initListener()
    }

    private fun initListener() {
        dataBinding.zh.setOnClickListener {
            SettingUtil.setLanguage("zh")

            lifecycleScope.launch {
                ToastUtil.showToastMsg("修改成功", this@SettingActivity)
                delay(700)
                AppManager.instant.restartApp(this@SettingActivity)
            }
        }

        dataBinding.en.setOnClickListener {
            SettingUtil.setLanguage("en")

            lifecycleScope.launch {
                ToastUtil.showToastMsg("修改成功", this@SettingActivity)
                delay(700)
                AppManager.instant.restartApp(this@SettingActivity)
            }
        }


        // 基本设置
        dataBinding.serverIp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val ip = s.toString()

                if (ip.isEmpty() || !isValidIp(ip))
                    return

                LogUtil.info("New Server Address $ip")
                viewModel.setServerIp(ip)
            }
        })

        dataBinding.serverPort.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val port = s.toString()

                if (port.isEmpty() || !isValidPort(port))
                    return

                LogUtil.info("New Server Port $port")
                viewModel.setServerPort(port)
            }
        })
        //
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_setting, BR.vm)
    }


    fun isValidPort(segment: String): Boolean {
        return segment.toIntOrNull() != null
    }

    fun isValidIp(ip: String): Boolean {
        val regex =
            Regex("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$")
        return regex.matches(ip)
    }
}