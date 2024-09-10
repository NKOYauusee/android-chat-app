package com.example.common.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.example.common.common.AppManager
import com.example.common.common.DataBindingConfig
import com.example.common.util.ClazzUtil
import com.example.common.util.DensityUtils
import com.example.common.viewmodel.BaseViewModel
import com.tencent.mmkv.MMKV
import java.lang.reflect.ParameterizedType


@Suppress("DEPRECATION")
abstract class BaseActivity<T : ViewDataBinding, VM : BaseViewModel> : AppCompatActivity() {
    protected lateinit var mContext: Context
    private var dialog: Dialog? = null

    //当前页面是否进行屏幕适配(true为进行适配)
    open var isSetDensity = true
    open val TAG = this::class.java.simpleName

    //是否全屏显示
    open var isFullScreen = true

    //当前页面ViewModel
    lateinit var viewModel: VM

    //当前页面的dataBinding
    lateinit var dataBinding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mContext = this
        if (isSetDensity) {
            //屏幕适配
            DensityUtils.setDensity(application, this)
        }


        if (isFullScreen) {
            //去掉标题栏
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            //全屏显示
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // 创建ViewModel
        getViewModelType()?.let {
            viewModel =
                ViewModelProvider(
                    this,
                    ViewModelProvider.AndroidViewModelFactory(application)
                )[it]
        }
        //获取配置
        val dataBindingConfig = getDataBindingConfig()
        //设置布局
        dataBinding = DataBindingUtil.setContentView(this, dataBindingConfig.layout)
        //绑定生命周期
        dataBinding.lifecycleOwner = this
        //设置参数
        if (dataBindingConfig.vmVariableId != -1)
            dataBinding.setVariable(dataBindingConfig.vmVariableId, viewModel)
        for (index in 0 until dataBindingConfig.bindingParams.size()) {
            dataBinding.setVariable(
                dataBindingConfig.bindingParams.keyAt(index),
                dataBindingConfig.bindingParams.valueAt(index)
            )
        }

        //把当前页面添加集合统一管理
        AppManager.instant.addActivity(this)
        MMKV.initialize(this)
    }

    abstract fun getDataBindingConfig(): DataBindingConfig

    override fun onResume() {
        super.onResume()
        if (isFullScreen) {
            //隐藏导航栏
            hideSystemNavigationBar(window)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isSetDensity) {
            //屏幕适配
            DensityUtils.setDensity(application, this)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //隐藏系统状态栏.虚拟按键
        if (hasFocus)
            hideSystemNavigationBar(window)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppManager.instant.finishActivity(this::class.java)

    }

    private fun hideSystemNavigationBar(window: Window?) {
        if (window != null) {
            val params = window.attributes
            params.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }


    //获取viewModel类型
    private fun getViewModelType(): Class<VM>? {
        val superClass = javaClass.genericSuperclass
        val type = (superClass as ParameterizedType).actualTypeArguments[1]
        return ClazzUtil.getRawType(type) as Class<VM>?
    }


    // 返回键监听
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        /*
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        */

        return super.onKeyDown(keyCode, event)
    }

    fun <C> switchActivity(
        context: Context,
        clazz: Class<C>,
        enterAni: Int = 0,
        exitAni: Int = 0,
        isFinish: Boolean = false
    ) {
        if (BaseActivity::class.java.isAssignableFrom(clazz)) {
            val intent = Intent(context, clazz)
            startActivity(intent)
            overridePendingTransition(enterAni, exitAni)
            if (isFinish) finish()
        }
    }

    fun switchActivity(
        intent: Intent,
        enterAni: Int = 0,
        exitAni: Int = 0,
        isFinish: Boolean = false
    ) {
        startActivity(intent)
        overridePendingTransition(enterAni, exitAni)
        if (isFinish) finish()
    }

    fun showDialog(
        context: Context,
        positiveText: String = "",
        negativeText: String = "",
        title: String = "",
    ) {

        dialog?.dismiss()
        dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setPositiveButton(positiveText) { dialog, _ ->
                dialog.dismiss()
                onPositiveButtonClick()
            }
            .setNegativeButton(negativeText) { dialog, _ ->
                dialog.dismiss()
                onNegativeButtonClick()
            }
            .create()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
        supportActionBar?.hide();
    }

    open fun onPositiveButtonClick() {}
    open fun onNegativeButtonClick() {}
}