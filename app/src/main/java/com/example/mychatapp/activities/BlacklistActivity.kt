package com.example.mychatapp.activities

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.UserStatusUtil
import com.example.common.viewmodel.BaseViewModel
import com.example.database.bean.UserFriBean
import com.example.database.enums.FriendStatusEnum
import com.example.database.helper.UserFriendHelper
import com.example.mychatapp.R
import com.example.mychatapp.adapter.BlacklistAdapter
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.ActivityBlacklistBinding
import com.example.mychatapp.listener.BlackListListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlacklistActivity : BaseActivity<ActivityBlacklistBinding, BaseViewModel>(),
    BlackListListener {
    private var blackListAdapter = BlacklistAdapter(mutableListOf(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        lifecycleScope.launch(Dispatchers.Main) {
            val list = withContext(Dispatchers.IO) {
                UserFriendHelper.selectBlockedFriends(
                    this@BlacklistActivity,
                    UserStatusUtil.getCurLoginUser()
                )
            }
            if (list.isEmpty())
                dataBinding.textErrorMessage.visibility = View.VISIBLE
            else
                dataBinding.textErrorMessage.visibility = View.INVISIBLE


            blackListAdapter = BlacklistAdapter(list, this@BlacklistActivity)
            dataBinding.userRecycleView.adapter = blackListAdapter
        }

        dataBinding.imgBack.setOnClickListener {
            finish()
        }
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_blacklist)
    }

    private var dialog: Dialog? = null
    override fun deleteBlacklist(friend: UserFriBean, callback: () -> Unit) {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this).setTitle("确定解除该用户的拉黑")
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()
                //
                friend.status = FriendStatusEnum.NORMAL.statusCode
                //
                ApiServiceHelper.service().setFriendStatus(friend).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : MyObservable<ResBean<Nothing>>() {
                        override fun success(res: ResBean<Nothing>) {
                            if (res.code != 200) {
                                MyToast(mContext).show(getString(R.string.info_act_fail))
                                return
                            }
                            MyToast(mContext).show("解除成功")
                            callback()

                            lifecycleScope.launch(Dispatchers.IO) {
                                UserFriendHelper.blacklistFriend(this@BlacklistActivity, friend)
                            }
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