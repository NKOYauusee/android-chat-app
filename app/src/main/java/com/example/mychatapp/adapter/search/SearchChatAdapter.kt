package com.example.mychatapp.adapter.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.common.util.DateFormatUtil
import com.example.database.bean.ChatBean
import com.example.database.bean.UserFriBean
import com.example.mychatapp.R
import com.example.mychatapp.databinding.ItemSearchResBinding
import com.example.mychatapp.listener.SearchListener
import com.example.mychatapp.util.HttpHelper

class SearchChatAdapter(
    private val searchItem: String,
    private val friend: MutableList<UserFriBean>,
    private var chatList: MutableList<MutableList<ChatBean>>,
    private var listener: SearchListener,
) :
    RecyclerView.Adapter<SearchChatAdapter.UserViewHolder>(), BaseAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflate: ItemSearchResBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_search_res,
            parent,
            false
        )
        return UserViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        if (position >= friend.size || position >= chatList.size)
            return

        val who = friend[position]
        val chats = chatList[position]

        Glide.with(holder.itemView.context)
            .load(HttpHelper.getFileUrl(who.avatar))
            .placeholder(R.drawable.image_placeholder)
            .into(holder.dataBinding.imageProfile)

        holder.dataBinding.textName.text = who.username!!
        if (chats.size == 1) {
            holder.dataBinding.textContent.text =
                bindHighlightedItem(chats[0].message, searchItem) ?: chats[0].message

            holder.dataBinding.textTime.text = DateFormatUtil.getFormatTime(chats[0].sendTime)

            holder.dataBinding.textTime.visibility = View.VISIBLE
        } else {
            holder.dataBinding.textContent.text = "${chats.size}条相关聊天记录"
        }

        // TODO
        holder.itemView.setOnClickListener {
            chats.reverse()
            listener.jumpToActivity(who, chats, searchItem)
        }
    }

    class UserViewHolder(val dataBinding: ItemSearchResBinding) :
        RecyclerView.ViewHolder(dataBinding.root)
}