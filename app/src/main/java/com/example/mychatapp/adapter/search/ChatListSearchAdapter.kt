package com.example.mychatapp.adapter.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.api.bean.HttpUrl
import com.example.common.util.DateFormatUtil
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.ChatBean
import com.example.database.bean.UserFriBean
import com.example.mychatapp.R
import com.example.mychatapp.databinding.ItemSelectUserBinding
import com.example.mychatapp.listener.SearchChatListener

class ChatListSearchAdapter(
    private val searchItem: String,
    private val friend: UserFriBean,
    private val chatList: MutableList<ChatBean>,
    private val listener: SearchChatListener
) : RecyclerView.Adapter<ChatListSearchAdapter.ViewHolder>(), BaseAdapter {


    class ViewHolder(val dataBinding: ItemSelectUserBinding) :
        RecyclerView.ViewHolder(dataBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflate: ItemSelectUserBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_select_user, parent, false
        )
        return ViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataBinding = holder.dataBinding
        val chat = chatList[position]

        if (friend.email == chat.sender) {
            Glide.with(holder.itemView.context)
                .load(HttpUrl.IMG_URL + friend.avatar)
                .into(dataBinding.imageProfile)
        } else {
            Glide.with(holder.itemView.context)
                .load(HttpUrl.IMG_URL + UserStatusUtil.getUserAvatar())
                .into(dataBinding.imageProfile)
        }

//        dataBinding.textName.text =
//            if (friend.email == chat.sender) friend.username else chat.receiverName

        LogUtil.info("nko -> $searchItem")
        dataBinding.textMsg.text = bindHighlightedItem(chat.message, searchItem) ?: chat.message

        dataBinding.textTime.text = DateFormatUtil.getFormatTime(chat.sendTime)

        holder.itemView.setOnClickListener {
            listener.toChatActivityWithData(friend, chat)
        }
    }
}