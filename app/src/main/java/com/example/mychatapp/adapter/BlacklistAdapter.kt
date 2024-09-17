package com.example.mychatapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.UserFriBean
import com.example.mychatapp.R
import com.example.mychatapp.databinding.ItemContainerBlacklistBinding
import com.example.mychatapp.listener.BlackListListener
import com.example.mychatapp.util.HttpHelper

class BlacklistAdapter(
    private val blackList: MutableList<UserFriBean>,
    private val listener: BlackListListener
) : RecyclerView.Adapter<BlacklistAdapter.ViewHolder>() {


    class ViewHolder(val dataBinding: ItemContainerBlacklistBinding) :
        RecyclerView.ViewHolder(dataBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflate: ItemContainerBlacklistBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_container_blacklist,
            parent,
            false
        )

        return ViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return blackList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = blackList[position]
        val dataBinding = holder.dataBinding
        val context = holder.itemView.context


        Glide.with(context)
            .load(HttpHelper.getFileUrl(friend.avatar))
            .placeholder(R.drawable.default_profile)
            .into(dataBinding.imageProfile)

        dataBinding.textEmail.text = friend.email
        dataBinding.textName.text = friend.username

        dataBinding.blacklistDelete.setOnClickListener {
            listener.deleteBlacklist(friend) {
                removeCallback(position)
                LogUtil.info("${UserStatusUtil.getCurLoginUser()} 解除拉黑好友 ${friend.email}")
            }
        }
    }

    private fun removeCallback(position: Int) {
        blackList.removeAt(position)
        // 通知Adapter更新UI
        notifyItemRemoved(position)
        //notifyItemRangeChanged(position, friendList.size)
    }
}