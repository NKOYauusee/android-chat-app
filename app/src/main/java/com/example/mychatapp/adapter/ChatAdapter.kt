package com.example.mychatapp.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.common.util.DateFormatUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.ChatBean
import com.example.mychatapp.R
import com.makeramen.roundedimageview.RoundedImageView


class ChatAdapter(
    private var chatBeanList: MutableList<ChatBean>,
    private var receiverBitmap: Bitmap?,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setChatList(list: MutableList<ChatBean>) {
        //this.chatBeanList = list
        chatBeanList.addAll(list)
        notifyItemRangeInserted(chatBeanList.size, list.size)
    }

    fun addNewMsg(
        chatBean: ChatBean,
        callback: (chatBeanList: MutableList<ChatBean>) -> Unit = {}
    ) {
        //this.chatBeanList = list
        chatBeanList.add(chatBean)
        notifyItemInserted(chatBeanList.size - 1)
        callback(chatBeanList)
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatBeanList[position].sender == UserStatusUtil.getCurLoginUser()) {
            TYPE_SENT
        } else {
            TYPE_RECEIVED
        }
    }

    class ReceiverHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val msg: TextView = itemView.findViewById(R.id.textMessage)
        private val dateTime: TextView = itemView.findViewById(R.id.textDateTime)
        private val profile: RoundedImageView? = itemView.findViewById(R.id.imageProfile)

        fun setMsg(chat: ChatBean, bitmap: Bitmap? = null) {
            msg.text = chat.message
            dateTime.text = DateFormatUtil.formatTime(chat.sendTime)

            bitmap?.let {
                profile?.setImageBitmap(bitmap)
            }
        }
    }

    class SendHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val msg: TextView = itemView.findViewById(R.id.textMessage)
        private val dateTime: TextView = itemView.findViewById(R.id.textDateTime)

        fun setMsg(chat: ChatBean) {
            msg.text = chat.message
            dateTime.text = DateFormatUtil.formatTime(chat.sendTime)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_container_sent_msg, parent, false)

            SendHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_container_receive_msg, parent, false)

            ReceiverHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return chatBeanList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_SENT) {
            holder as SendHolder
            holder.setMsg(chatBeanList[position])
        } else {
            holder as ReceiverHolder
            holder.setMsg(chatBeanList[position], receiverBitmap)
        }
    }

    companion object {
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }

    fun getLastIdx(): Int {
        return chatBeanList.size - 1
    }

    fun saveChatBeanList(save: (list: MutableList<ChatBean>) -> Unit) {
        save(this.chatBeanList)
    }

    fun getLastItem(): ChatBean {
        return chatBeanList.last()
    }
}