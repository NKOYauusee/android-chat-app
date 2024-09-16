package com.example.mychatapp.activities

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.FriendApply
import com.example.database.bean.UserBean
import com.example.mychatapp.BR
import com.example.mychatapp.R
import com.example.mychatapp.adapter.search.SearchResultAdapter
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.ActivityApplicantBinding
import com.example.mychatapp.listener.ApplyListener
import com.example.mychatapp.viewmodel.ApplicantViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ApplicantActivity : BaseActivity<ActivityApplicantBinding, ApplicantViewModel>(),
    ApplyListener {
    private var userAdapter = SearchResultAdapter("", mutableListOf(), this)
    private var searchItem: String = ""

    // 防止滚动到底部时发起多个请求
    private var loadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSearchItem()

        dataBinding.imgBack.setOnClickListener {
            finish()
        }

        dataBinding.userListRecycleView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastItemPos = layoutManager.itemCount - 1
                if (!userAdapter.hasMoreRes())
                    return

                if (layoutManager.findLastVisibleItemPosition() == lastItemPos) {
                    LogUtil.info("userAdapter.getPage() ${userAdapter.getPage()}")
                    getMoreSearchRes(searchItem, userAdapter.getPage()) {
                        LogUtil.info("size ${it.size}")
                        userAdapter.loadMore(it)
                    }
                }
            }
        })
    }

    // 搜索结果渲染
    private fun getSearchItem() {
        searchItem = intent.getSerializableExtra("searchItem") as String
        LogUtil.info("搜索内容 $searchItem")
        viewModel.hasResult.postValue(searchItem.isNotEmpty())
        if (searchItem.isEmpty()) {
            return
        }

        getMoreSearchRes(searchItem, userAdapter.getPage()) {
            userAdapter =
                SearchResultAdapter(searchItem, it, this@ApplicantActivity)
            dataBinding.userListRecycleView.adapter = userAdapter
        }
    }


    private fun getMoreSearchRes(
        searchItem: String,
        page: String,
        callback: (list: MutableList<UserBean>) -> Unit
    ) {

        if (loadJob?.isActive == true)
            return

        if (searchItem.isEmpty())
            return

        loadJob = lifecycleScope.launch(Dispatchers.Main) {
            ApiServiceHelper.service().searchRes(searchItem, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MyObservable<ResBean<MutableList<UserBean>>>() {
                    override fun success(res: ResBean<MutableList<UserBean>>) {
                        if (res.code == 200 && !res.data.isNullOrEmpty()) {
                            callback(res.data!!)
                        }
                        //LogUtil.info(Gson().toJson(res))
                        LogUtil.info("搜索结果请求成功")
                    }

                    override fun failed(e: Throwable) {
                        Log.e(TAG, "failed: ", e)
                    }
                })
        }
    }


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_applicant, BR.vm)
    }


    private var dialog: Dialog? = null


    // 底部加载
    override fun sendApply(applyTarget: UserBean, info: String, callback: () -> Unit) {
        val friendApply = FriendApply()

        friendApply.applicant = UserStatusUtil.getCurLoginUser()
        friendApply.applicantName = UserStatusUtil.getUsername()
        friendApply.info = info
        friendApply.target = applyTarget.email
        friendApply.targetAvatar = applyTarget.avatar ?: ""
        friendApply.applicantAvatar = UserStatusUtil.getUserAvatar()

        LogUtil.info("${UserStatusUtil.getCurLoginUser()}申请${applyTarget.email}")

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
                                MyToast(mContext).show("加载失败")
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