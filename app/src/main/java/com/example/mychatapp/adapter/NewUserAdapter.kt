package com.example.mychatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.UserFriBean
import com.example.database.helper.MainUserSelectHelper
import com.example.mychatapp.R
import com.example.mychatapp.databinding.ItemContainerUserBinding
import com.example.mychatapp.listener.UserListener

class NewUserAdapter(
    private var friendList: MutableList<UserFriBean>,
    private var userListener: UserListener
) :
    RecyclerView.Adapter<NewUserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
//        ItemContainerUserBinding
        val inflate: ItemContainerUserBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_container_user,
            parent,
            false
        )
        return UserViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val friend = friendList[position]
        holder.dataBinding.textName.text = friend.username
        holder.dataBinding.textEmail.text = friend.email

        holder.itemView.setOnClickListener {
            if (holder.dataBinding.friendActionWrapper.visibility == View.VISIBLE)
                holder.dataBinding.friendActionWrapper.visibility = View.INVISIBLE
            else userListener.onUserClicked(friend)
        }

        // 长按事件
        holder.itemView.setOnLongClickListener {
            holder.dataBinding.friendActionWrapper.visibility = View.VISIBLE
            true // 返回 true 表示事件已被处理
        }

        holder.dataBinding.blacklistFriendBtn.setOnClickListener {
            userListener.blackListFriend(friend) {
                removeCallback(position)
                LogUtil.info("${UserStatusUtil.getCurLoginUser()} 拉黑好友 ${friend.email}")
            }
        }

        holder.dataBinding.deleteFriendBtn.setOnClickListener {
            userListener.deleteFriend(friend) {
                removeCallback(position)
                LogUtil.info("${UserStatusUtil.getCurLoginUser()} 删除好友 ${friend.email}")
            }
        }
    }

    class UserViewHolder(val dataBinding: ItemContainerUserBinding) :
        RecyclerView.ViewHolder(dataBinding.root) {
    }

    fun returnFriendListSize(): Int {
        return friendList.size
    }

    private fun removeCallback(position: Int) {
        friendList.removeAt(position)
        // 通知Adapter更新UI
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, friendList.size)
    }
}
