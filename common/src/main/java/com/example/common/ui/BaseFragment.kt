package com.example.common.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.common.common.DataBindingConfig
import com.example.common.util.ClazzUtil
import com.example.common.viewmodel.BaseViewModel
import java.lang.reflect.ParameterizedType

/**
 * Fragment基类
 */
abstract class BaseFragment<T : ViewDataBinding, VM : BaseViewModel> : Fragment() {
    lateinit var dataBinding: T

    lateinit var viewModel: VM


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        activity?.application?.let { application ->
            getViewModelType()?.let {
                viewModel =
                    ViewModelProvider(
                        this,
                        ViewModelProvider.AndroidViewModelFactory(application)
                    )[it]
            }
        }
        val dataBindingConfig = getDataBindingConfig()
        dataBinding = DataBindingUtil.inflate(inflater, dataBindingConfig.layout, container, false)
        dataBinding.lifecycleOwner = this
        if (dataBindingConfig.vmVariableId != -1)
            dataBinding.setVariable(dataBindingConfig.vmVariableId, viewModel)
        for (index in 0 until dataBindingConfig.bindingParams.size()) {
            dataBinding.setVariable(
                dataBindingConfig.bindingParams.keyAt(index),
                dataBindingConfig.bindingParams.valueAt(index)
            )
        }
        return dataBinding.root
    }

    override fun onResume() {
        super.onResume()
        show()
    }

    override fun onPause() {
        super.onPause()
        hide()
    }


    /**
     * 页面显示回调
     */
    abstract fun show()

    /**
     * 页面隐藏回调
     */
    abstract fun hide()

    /**
     * 获取DataBinding配置
     */
    abstract fun getDataBindingConfig(): DataBindingConfig

    /**
     * 获取ViewModel的类型
     */
    private fun getViewModelType(): Class<VM>? {
        val superClass = javaClass.genericSuperclass
        val type = (superClass as ParameterizedType).actualTypeArguments[1]
        return ClazzUtil.getRawType(type) as Class<VM>?
    }

    private var dialog: Dialog? = null
    fun showDialog(
        context: Context,
        positiveText: String = "",
        negativeText: String = "",
        message: String = "",
        title: String = "",
    ) {

        dialog?.dismiss()
        dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
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
    }

    open fun onPositiveButtonClick() {}
    open fun onNegativeButtonClick() {}

    fun switchActivity(
        intent: Intent,
        enterAni: Int = 0,
        exitAni: Int = 0,
        isFinish: Boolean = false,
        hideSelf: Boolean = true
    ) {
        startActivity(intent)
        requireActivity().overridePendingTransition(enterAni, exitAni)
        if (isFinish) requireActivity().finish()
        if (hideSelf) hideSelf()
    }

    private fun hideSelf() {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.hide(this)
        transaction.commit()
    }
}