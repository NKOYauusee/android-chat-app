package com.example.mychatapp.adapter.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.database.bean.UserFriBean
import com.example.mychatapp.R
import com.example.mychatapp.databinding.ItemSearchResBinding
import com.example.mychatapp.listener.UserListener

class SearchFriendAdapter(
    private val searchTerm: String,
    private var chatList: MutableList<UserFriBean>,
    private var listener: UserListener
) : RecyclerView.Adapter<SearchFriendAdapter.UserViewHolder>(), BaseAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
//        ItemContainerUserBinding
        val inflate: ItemSearchResBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.item_search_res, parent, false
        )
        return UserViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        if (position >= chatList.size)
            return

        val friend = chatList[position]
        holder.dataBinding.textName.text =
            bindHighlightedItem(friend.username!!, searchTerm) ?: friend.username
        holder.dataBinding.textContent.text =
            bindHighlightedItem(friend.email, searchTerm) ?: friend.email
        // TODO
        holder.itemView.setOnClickListener {
            listener.onUserClicked(friend)
        }
    }

    class UserViewHolder(val dataBinding: ItemSearchResBinding) :
        RecyclerView.ViewHolder(dataBinding.root)

    private fun removeCallback(position: Int) {
        chatList.removeAt(position)
        // 通知Adapter更新UI
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, chatList.size)
    }
}