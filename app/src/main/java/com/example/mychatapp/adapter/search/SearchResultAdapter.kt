package com.example.mychatapp.adapter.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.common.util.UserStatusUtil
import com.example.database.bean.UserBean
import com.example.database.bean.UserFriBean
import com.example.mychatapp.R
import com.example.mychatapp.databinding.ItemApplicantBinding
import com.example.mychatapp.listener.ApplyListener
import com.example.mychatapp.util.HttpHelper

class SearchResultAdapter(
    private val searchItem: String,
    private val friendList: MutableList<UserFriBean>,
    private val userList: MutableList<UserBean>,
    private val listener: ApplyListener
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>(), BaseAdapter {


    class ViewHolder(val dataBinding: ItemApplicantBinding) :
        RecyclerView.ViewHolder(dataBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflate: ItemApplicantBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_applicant,
            parent,
            false
        )

        return ViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataBinding = holder.dataBinding
        val user = userList[position]

        val email = user.email
        if (email == UserStatusUtil.getCurLoginUser()) {
            dataBinding.btnSendApply.visibility = View.INVISIBLE
        }

        for (fri in friendList) {
            if (fri.email == email)
                dataBinding.btnSendApply.visibility = View.INVISIBLE
        }


        dataBinding.applyName.text =
            bindHighlightedItem(user.username!!, searchItem) ?: user.username
        dataBinding.applyEmail.text = bindHighlightedItem(user.email, searchItem) ?: user.email

        dataBinding.imageProfile

        Glide.with(holder.itemView.context)
            .load(HttpHelper.getFileUrl(user.avatar))
            .placeholder(R.drawable.default_profile)
            .into(dataBinding.imageProfile)

        // 准备申请
        dataBinding.btnSendApply.setOnClickListener {
            if (dataBinding.btnSendApply.text == holder.itemView.context.getString(R.string.info_applied))
                return@setOnClickListener

            dataBinding.applyWrapper.visibility = View.VISIBLE
            it.visibility = View.INVISIBLE
        }
        // 取消申请
        dataBinding.cancelApply.setOnClickListener {
            dataBinding.btnSendApply.visibility = View.VISIBLE
            dataBinding.applyWrapper.visibility = View.GONE
            dataBinding.applyInfo.setText("")
        }

        // 提交申请
        dataBinding.sureApply.setOnClickListener {
            listener.sendApply(user, dataBinding.applyInfo.text.toString()) {
                // TODO 提交申请后 修改ui显示
                dataBinding.applyWrapper.visibility = View.GONE
                dataBinding.btnSendApply.visibility = View.VISIBLE
                dataBinding.btnSendApply.text =
                    holder.itemView.context.getString(R.string.info_applied)
            }
        }
    }

    fun getPage(): String {
        return ((this.userList.size / 10) + 1).toString()
    }

    fun loadMore(res: MutableList<UserBean>) {
        if (userList.contains(res[0]))
            return

        val start = userList.size
        userList.addAll(res)
        // 通知适配器从特定位置插入了一系列项目
        notifyItemRangeInserted(start, res.size)
    }

    fun hasMoreRes(): Boolean {
        return this.userList.size % 10 == 0
    }
}