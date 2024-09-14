package com.example.mychatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.api.bean.HttpUrl
import com.example.common.util.DateFormatUtil
import com.example.common.util.DensityUtils
import com.example.common.util.UserStatusUtil
import com.example.database.bean.ChatBean
import com.example.database.enums.MessageType
import com.example.mychatapp.R
import com.example.mychatapp.listener.ChatMsgListener
import com.makeramen.roundedimageview.RoundedImageView


class ChatAdapter(
    private var chatBeanList: MutableList<ChatBean>,
    private var receiverAvatar: String?,
    var listener: ChatMsgListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setChatList(
        list: MutableList<ChatBean>,
        callback: (chatBeanList: MutableList<ChatBean>) -> Unit
    ) {
        //this.chatBeanList = list
        chatBeanList.addAll(list)
        notifyItemRangeInserted(chatBeanList.size, list.size)
        callback(chatBeanList)
    }

    fun addNewMsg(
        chatBean: ChatBean,
        callback: (chatBeanList: MutableList<ChatBean>) -> Unit = {}
    ) {
        //this.chatBeanList = list
        chatBeanList.add(chatBean)
        notifyItemChanged(chatBeanList.size - 1)
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

    class ReceiverListener(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val msg: TextView = itemView.findViewById(R.id.textMessage)
        val dateTime: TextView = itemView.findViewById(R.id.textDateTime)
        val profile: RoundedImageView = itemView.findViewById(R.id.imageProfile)
        val imageView: ImageView = itemView.findViewById(R.id.message_img)
    }

    class SendListener(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val msg: TextView = itemView.findViewById(R.id.textMessage)
        val dateTime: TextView = itemView.findViewById(R.id.textDateTime)
        val imageView: ImageView = itemView.findViewById(R.id.message_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_container_sent_msg, parent, false)

            SendListener(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_container_receive_msg, parent, false)

            ReceiverListener(view)
        }
    }

    override fun getItemCount(): Int {
        return chatBeanList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_SENT) {
            holder as SendListener
            //holder.setMsg(chatBeanList[position])
            sender(chatBeanList[position], holder)
        } else {
            holder as ReceiverListener
            //holder.setMsg(chatBeanList[position], receiverBitmap)
            receiver(chatBeanList[position], holder, receiverAvatar)
        }
    }

    private fun sender(chat: ChatBean, holder: SendListener) {
        holder.dateTime.text = DateFormatUtil.formatTime(chat.sendTime)

        when (chat.type) {
            MessageType.TEXT.type -> {
                holder.imageView.visibility = View.GONE
                holder.msg.text = chat.message
                holder.msg.visibility = View.VISIBLE
            }

            MessageType.IMAGE.type -> {
                holder.msg.visibility = View.GONE
                //val bitmap = FileUtil.getBitmap(uri)
                val imageWidth = (DensityUtils.getWidth(holder.itemView.context) * 0.4).toInt()

                val requestOptions = RequestOptions().transform(RoundedCorners(20))

                //LogUtil.info("path -> ${getPath(chat.message, chat.type)}")
                Glide.with(holder.itemView.context)
                    .load(getPath(chat.message, chat.type))
                    .placeholder(R.drawable.image_placeholder)
                    .override(imageWidth)
                    .apply(requestOptions)
                    .into(holder.imageView)

                holder.imageView.visibility = View.VISIBLE

                holder.imageView.setOnClickListener {
                    //TODO 图片预览
                    listener
                }
            }
        }
    }

    // 对方消息设置
    private fun receiver(chat: ChatBean, holder: ReceiverListener, avatar: String?) {
        holder.dateTime.text = DateFormatUtil.formatTime(chat.sendTime)

        // TODO 设置头像
        Glide.with(holder.itemView.context)
            .load(HttpUrl.IMG_URL + avatar)
            .placeholder(R.drawable.default_profile)
            .into(holder.profile)

        when (chat.type) {
            MessageType.TEXT.type -> {
                holder.imageView.visibility = View.GONE
                holder.msg.text = chat.message
                holder.msg.visibility = View.VISIBLE
            }

            MessageType.IMAGE.type -> {
                holder.msg.visibility = View.GONE
                //val bitmap = FileUtil.getBitmap(uri)
                val imageWidth = (DensityUtils.getWidth(holder.itemView.context) * 0.4).toInt()

                val requestOptions = RequestOptions().transform(RoundedCorners(20))

                //LogUtil.info("path -> ${getPath(chat.message, chat.type)}")
                Glide.with(holder.itemView.context)
                    .load(getPath(chat.message, chat.type))
                    .placeholder(R.drawable.image_placeholder)
                    .override(imageWidth)
                    .apply(requestOptions)
                    .into(holder.imageView)

                holder.imageView.visibility = View.VISIBLE

                holder.imageView.setOnClickListener {
                    //TODO

                    listener
                }
            }
        }
    }

    companion object {
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }

    fun scrollToLastIdx(cb: (lastIdx: Int) -> Unit) {
        cb(this.chatBeanList.size - 1)
    }

    fun returnListSize(): Int {
        return this.chatBeanList.size
    }

    fun returnTopChat(): ChatBean? {
        if (chatBeanList.isEmpty())
            return null

        return this.chatBeanList[0]
    }

    fun loadMore(historyList: MutableList<ChatBean>, isStart: Boolean = false) {
        if (historyList.isEmpty())
            return

        if (isStart) chatBeanList.addAll(chatBeanList.size - 1, historyList)
        else chatBeanList.addAll(0, historyList)
        // 通知适配器从特定位置插入了一系列项目
        notifyItemRangeInserted(0, historyList.size)
    }

    fun getPath(src: String, type: Int): String? {
        val startIdx = MessageType.getDescFromType(type).length + 1
        val endIdx = src.length

        if (startIdx > endIdx)
            return null

        return src.substring(startIdx, endIdx)
    }
}