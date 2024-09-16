package com.example.mychatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.UserFriBean
import com.example.mychatapp.R
import com.example.mychatapp.databinding.ItemContainerUserBinding
import com.example.mychatapp.listener.UserListener
import com.example.mychatapp.util.HttpHelper

class UserFriendListAdapter(
    private var friendList: MutableList<UserFriBean>,
    private var userListener: UserListener
) :
    RecyclerView.Adapter<UserFriendListAdapter.UserViewHolder>() {

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
        Glide.with(holder.itemView.context)
            .load(HttpHelper.getFileUrl(friend.avatar))
            .placeholder(R.drawable.default_profile)
            .into(holder.dataBinding.imageProfile)

        //LogUtil.info("头像 ${HttpUrl.IMG_URL + friend.avatar}")

        holder.dataBinding.textName.text = friend.username
        holder.dataBinding.textEmail.text = friend.email

        holder.itemView.setOnClickListener {
            if (holder.dataBinding.friendActionWrapper.visibility == View.VISIBLE)
                holder.dataBinding.friendActionWrapper.visibility = View.INVISIBLE
            else userListener.onUserClicked(friend)
        }

        // 长按事件
        holder.itemView.setOnLongClickListener {
            var isHandler = false
            userListener.preventLongClick {
                if (!it) holder.dataBinding.friendActionWrapper.visibility = View.VISIBLE
                isHandler = !it
            }

            isHandler // 返回 true 表示事件已被处理
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
        //notifyItemRangeChanged(position, friendList.size)
    }

    fun getEmailFromPos(position: Int): UserFriBean {
        return friendList[position]
    }

    fun batchRemove(list: MutableList<Int>) {
        for (i in list) {
            friendList.removeAt(i)
            notifyItemRemoved(i)
        }
    }
}
