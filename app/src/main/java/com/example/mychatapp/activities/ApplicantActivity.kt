package com.example.mychatapp.activities

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.common.viewmodel.BaseViewModel
import com.example.database.bean.FriendApply
import com.example.database.bean.UserBean
import com.example.mychatapp.R
import com.example.mychatapp.adapter.search.SearchResultAdapter
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.ActivityApplicantBinding
import com.example.mychatapp.listener.ApplyListener
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ApplicantActivity : BaseActivity<ActivityApplicantBinding, BaseViewModel>(), ApplyListener {
    private var userAdapter = SearchResultAdapter("", mutableListOf(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSearchItem()

        dataBinding.imgBack.setOnClickListener {
            finish()
        }
    }

    // 搜索结果渲染
    private fun getSearchItem() {
        val searchItem = intent.getSerializableExtra("searchItem") as String
        LogUtil.info("搜索内容 $searchItem")
        if (searchItem.isEmpty())
            return

        ApiServiceHelper.service().searchRes(searchItem)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MyObservable<ResBean<MutableList<UserBean>>>() {
                override fun success(res: ResBean<MutableList<UserBean>>) {
                    if (res.code == 200 && !res.data.isNullOrEmpty()) {
                        userAdapter =
                            SearchResultAdapter(searchItem, res.data!!, this@ApplicantActivity)
                        dataBinding.userListRecycleView.adapter = userAdapter
                    }
                    LogUtil.info(Gson().toJson(res))
                    LogUtil.info("请求成功")
                }

                override fun failed(e: Throwable) {
                    Log.e(TAG, "failed: ", e)
                }
            })
    }


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_applicant)
    }

    override fun loadMore() {
    }


    private var dialog: Dialog? = null

    // TODO
    override fun sendApply(applyTarget: UserBean, info: String, callback: () -> Unit) {
        val friendApply = FriendApply()

        friendApply.applicant = UserStatusUtil.getCurLoginUser()
        friendApply.applicantName = UserStatusUtil.getUsername()
        friendApply.info = info
        friendApply.target = applyTarget.email

        LogUtil.info("${UserStatusUtil.getCurLoginUser()}申请$${applyTarget.email}")

        dialog?.dismiss()
        dialog = AlertDialog.Builder(this).setTitle("确定申请该用户?")
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                ApiServiceHelper.service().addFriendApply(
                    friendApply
                ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : MyObservable<ResBean<Nothing>>() {
                        override fun success(res: ResBean<Nothing>) {
                            if (res.code != 200) {
                                MyToast(mContext).show("操作失败")
                                return
                            }
                            MyToast(this@ApplicantActivity).show("申请成功")
                            callback()
                        }

                        override fun failed(e: Throwable) {
                            MyToast(mContext).show("操作失败，请稍后再试")
                            Log.d(TAG, "failed blackListFriend: ", e)
                        }

                    })
            }.setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }.create()


        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
        supportActionBar?.hide()
    }

}