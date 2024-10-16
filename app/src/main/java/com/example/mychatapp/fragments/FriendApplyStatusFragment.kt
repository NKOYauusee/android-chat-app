package com.example.mychatapp.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseFragment
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.FriendApply
import com.example.database.enums.ApplyStatusEnum
import com.example.mychatapp.BR
import com.example.mychatapp.R
import com.example.mychatapp.adapter.FriendApplyStatusAdapter
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.FragmentFriendApplyStatusListBinding
import com.example.mychatapp.listener.FriendApplyStatusListener
import com.example.mychatapp.viewmodel.FriendApplyViewModel
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class FriendApplyStatusFragment :
    BaseFragment<FragmentFriendApplyStatusListBinding, FriendApplyViewModel>(),
    FriendApplyStatusListener {

    // 好友请求结果回调
    private var friendApplyStatusList: MyObservable<ResBean<List<FriendApply>>>? = null

    var applyAdapter: FriendApplyStatusAdapter? = null

    private var friendListJob: Job? = null

    override fun show() {
        initAndStartReqFriendApplyStatusList()
    }

    override fun hide() {
        friendListJob?.cancel()
    }

    private fun initAndStartReqFriendApplyStatusList() {
        //viewModel.hasApplyData.postValue(false)

//        applyAdapter = FriendApplyStatusAdapter(
//            this@FriendApplyStatusFragment,
//            mutableListOf<FriendApply>().apply {
//                add(FriendApply())
//            }
//        )

        dataBinding.friendStatusList.adapter = applyAdapter
        LogUtil.info("开始请求好友申请列表")
        friendListJob?.cancel()
        friendApplyStatusList = object : MyObservable<ResBean<List<FriendApply>>>() {
            override fun success(res: ResBean<List<FriendApply>>) {
                LogUtil.info("好友申请请求结果 成功 ->" + Gson().toJson(res.data!!))
                viewModel.hasApplyData.postValue(!res.data.isNullOrEmpty())
                dataBinding.progress.visibility = View.INVISIBLE
                //LogUtil.info(res.data!!.toMutableList().size.toString())
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(500)
                    applyAdapter = FriendApplyStatusAdapter(
                        this@FriendApplyStatusFragment,
                        res.data!!.toMutableList()
                    )
                    dataBinding.friendStatusList.adapter = applyAdapter
                }
            }

            override fun failed(e: Throwable) {
                dataBinding.progress.visibility = View.INVISIBLE
                Log.d("xht", "failed friendApplyStatusList", e)
                viewModel.hasApplyData.postValue(false)
            }
        }

        friendListJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                if (isVisible) {
                    ApiServiceHelper.service()
                        .getFriendApplyStatus(UserStatusUtil.getCurLoginUser())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(friendApplyStatusList!!)
                }
                delay(2500)
            }
        }
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_friend_apply_status_list, BR.vm)
    }

    companion object {
        val instance by lazy {
            FriendApplyStatusFragment()
        }
    }

    private var dialog: Dialog? = null
    override fun setApplyStatus(friendApply: FriendApply) {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(context)
            .setTitle(getString(R.string.info_confirm_add_as_friend))
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                friendApply.status = ApplyStatusEnum.APPROVED.code
                setApplyStatusByNet(friendApply)
            }
            .setNegativeButton(getString(R.string.info_refuse)) { dialog, _ ->
                dialog.dismiss()
                friendApply.status = ApplyStatusEnum.REJECTED.code
                setApplyStatusByNet(friendApply)
            }
            .create()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
    }

    private var applyRes: MyObservable<ResBean<Nothing>>? = null

    private fun setApplyStatusByNet(friendApply: FriendApply) {
        applyRes = object : MyObservable<ResBean<Nothing>>() {
            override fun success(res: ResBean<Nothing>) {
                if (res.code == 200) {
                    context?.let { MyToast(it).show(getString(R.string.info_operation_successful)) }
                } else {
                    context?.let { MyToast(it).show(getString(R.string.info_add_failed)) }
                }
            }

            override fun failed(e: Throwable) {
                context?.let { MyToast(it).show(getString(R.string.info_network_error_add_failed_try_again)) }
            }

        }

        ApiServiceHelper.service().setApplyStatus(friendApply)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(applyRes!!)
    }

}