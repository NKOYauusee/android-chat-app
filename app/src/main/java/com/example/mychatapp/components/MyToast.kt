package com.example.mychatapp.components

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.common.util.DensityUtils
import com.example.mychatapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyToast(private val context: Context) : Toast(context) {
    private var toastView: View? = null
    private var toastContainer: FrameLayout? = null
    private var isToastVisible = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    fun show(
        message: String,
        duration: Long = 1500L,
        layoutId: Int = R.layout.custom_toast_layout,
        textId: Int = R.id.toast_text
    ) {
        if (isToastVisible) return

        val inflater = LayoutInflater.from(context)
        // R.layout.custom_toast_layout
        // R.id.toast_text
        toastView = inflater.inflate(layoutId, null).apply {
            findViewById<TextView>(textId).text = message
        }
        toastContainer = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP
                setMargins(
                    DensityUtils.dip2px(context, 80f),
                    DensityUtils.dip2px(context, DensityUtils.dip2px(context, 45f).toFloat()),
                    DensityUtils.dip2px(context, 80f),
                    0
                )
            }
            addView(toastView)
        }


        // 添加 Toast 到当前窗口的顶层视图
        val decorView = (context as? AppCompatActivity)?.window?.decorView as? ViewGroup

        decorView?.addView(toastContainer)

        coroutineScope.launch {
            showToastAnimation()
            delay(duration)
            hideToastAnimation()
        }
    }

    private suspend fun showToastAnimation() = withContext(Dispatchers.Main) {
        toastView?.apply {
            translationY = -300f
            alpha = 0f
            animate().translationY(0f).alpha(1f).setDuration(500)
                .setInterpolator(DecelerateInterpolator()).start()
        }
        isToastVisible = true
    }

    private suspend fun hideToastAnimation() = withContext(Dispatchers.Main) {
        toastView?.animate()?.translationY(-300f)?.alpha(0f)?.setDuration(500)
            ?.setInterpolator(AccelerateInterpolator())?.withEndAction {
                toastContainer?.let {
                    val decorView = (context as? AppCompatActivity)?.window?.decorView as? ViewGroup
                    if (decorView != null && toastContainer != null) {
                        // 使用 findViewById 来确保正确找到 toastContainer
                        val container = decorView.findViewById<ViewGroup>(toastContainer!!.id)
                        if (container != null) {
                            decorView.removeView(container)
                        }
                    }
                }

                isToastVisible = false
            }?.start()

    }

    // Cancel any ongoing coroutines if needed
    override fun cancel() {
        coroutineScope.cancel()
    }
}