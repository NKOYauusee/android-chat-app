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
import com.example.database.bean.UserFriBean
import com.example.database.helper.UserFriendHelper
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
import kotlinx.coroutines.withContext

class ApplicantActivity : BaseActivity<ActivityApplicantBinding, ApplicantViewModel>(),
    ApplyListener {
    private var userAdapter = SearchResultAdapter("", mutableListOf(), mutableListOf(), this)
    private var searchItem: String = ""

    // 防止滚动到底部时发起多个请求
    private var loadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.hasResult.postValue(false)

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
        //viewModel.hasResult.postValue(searchItem.isNotEmpty())
        if (searchItem.isEmpty()) {
            return
        }

        getMoreSearchRes(searchItem, userAdapter.getPage()) {
            lifecycleScope.launch(Dispatchers.Main) {
                val friendList = getLocalFriendList()

                userAdapter =
                    SearchResultAdapter(searchItem, friendList, it, this@ApplicantActivity)
                dataBinding.userListRecycleView.adapter = userAdapter
            }
        }
    }

    private suspend fun getLocalFriendList(): MutableList<UserFriBean> {
        val list = withContext(Dispatchers.IO) {
            UserFriendHelper.selectFriends(this@ApplicantActivity, UserStatusUtil.getCurLoginUser())
        }

        return list
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

                        viewModel.hasResult.postValue(!res.data.isNullOrEmpty())
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
        dialog = AlertDialog.Builder(this).setTitle(getString(R.string.info_not_sure_apply))
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                ApiServiceHelper.service().addFriendApply(
                    friendApply
                ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : MyObservable<ResBean<Nothing>>() {
                        override fun success(res: ResBean<Nothing>) {
                            if (res.code != 200) {
                                MyToast(mContext).show(getString(R.string.info_load_fail))
                                return
                            }
                            MyToast(this@ApplicantActivity).show(getString(R.string.info_application_fail))
                            callback()
                        }

                        override fun failed(e: Throwable) {
                            MyToast(mContext).show(getString(R.string.info_act_fail))
                            Log.d(TAG, "failed blackListFriend: ", e)
                        }

                    })
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.create()


        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
        supportActionBar?.hide()
    }

}