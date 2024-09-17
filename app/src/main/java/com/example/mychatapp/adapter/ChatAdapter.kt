package com.example.mychatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.common.util.DateFormatUtil
import com.example.common.util.DensityUtils
import com.example.common.util.UserStatusUtil
import com.example.database.bean.ChatBean
import com.example.database.enums.MessageType
import com.example.mychatapp.R
import com.example.mychatapp.databinding.ItemContainerReceiveMsgBinding
import com.example.mychatapp.databinding.ItemContainerSentMsgBinding
import com.example.mychatapp.listener.ChatMsgListener
import com.example.mychatapp.util.GlideUtil
import com.example.mychatapp.util.HttpHelper


class ChatAdapter(
    private var chatList: MutableList<ChatBean>,
    private var receiverAvatar: String?,
    var listener: ChatMsgListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 添加新数据
    fun setChatList(
        list: MutableList<ChatBean>,
        callback: (chatBeanList: MutableList<ChatBean>) -> Unit
    ) {
        val start = chatList.size
        chatList.addAll(list)
        notifyItemRangeInserted(start, list.size)

        callback(chatList)
    }


    fun addNewMsg(
        chatBean: ChatBean,
        callback: (chatBeanList: MutableList<ChatBean>) -> Unit = {}
    ) {
        chatList.add(chatBean)

        notifyItemChanged(chatList.size - 1)
        notifyItemInserted(chatList.size - 1)

        callback(chatList)
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].sender == UserStatusUtil.getCurLoginUser()) {
            TYPE_SENT
        } else {
            TYPE_RECEIVED
        }
    }

    class ReceiverAdapter(val dataBinding: ItemContainerReceiveMsgBinding) :
        RecyclerView.ViewHolder(dataBinding.root) {


        fun hideAll() {
            dataBinding.textMessage.visibility = View.GONE

            dataBinding.videoWrapper.visibility = View.GONE

            dataBinding.messageImg.visibility = View.GONE

            dataBinding.fileWrapper.visibility = View.GONE

            dataBinding.btnDownload.visibility = View.GONE
        }
    }

    class SendAdapter(val dataBinding: ItemContainerSentMsgBinding) :
        RecyclerView.ViewHolder(dataBinding.root) {
        fun hideAll() {
            dataBinding.textMessage.visibility = View.GONE

            dataBinding.messageImg.visibility = View.GONE

            dataBinding.fileWrapper.visibility = View.GONE

            dataBinding.btnDownload.visibility = View.GONE

            dataBinding.videoWrapper.visibility = View.GONE
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == TYPE_SENT) {
            val inflate: ItemContainerSentMsgBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_container_sent_msg,
                parent,
                false
            )

            SendAdapter(inflate)
        } else {
            val inflate: ItemContainerReceiveMsgBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_container_receive_msg,
                parent,
                false
            )

            ReceiverAdapter(inflate)
        }
    }

    companion object {
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }


    fun scrollToLastIdx(cb: (lastIdx: Int) -> Unit) {
        cb(this.chatList.size - 1)
    }

    fun returnListSize(): Int {
        return this.chatList.size
    }

    fun returnTopChat(): ChatBean? {
        if (chatList.isEmpty())
            return null

        return this.chatList.first()
    }

    fun returnBottomChat(): ChatBean? {
        if (chatList.isEmpty())
            return null

        return this.chatList.last()
    }

    fun topLoadMore(historyList: MutableList<ChatBean>) {
        if (historyList.isEmpty())
            return

        chatList.addAll(0, historyList)
        // 通知适配器从特定位置插入了一系列项目
        notifyItemRangeInserted(0, historyList.size)
    }

    fun bottomLoadMore(list: MutableList<ChatBean>) {
        val start = chatList.size
        chatList.addAll(list)
        // 通知适配器从特定位置插入了一系列项目
        notifyItemRangeInserted(start, list.size)
    }

    private fun getPath(src: String, type: Int): String? {
        val startIdx = MessageType.getDescFromType(type).length + 1
        val endIdx = src.length

        if (startIdx > endIdx)
            return null

        return src.substring(startIdx, endIdx)
    }

    private fun extractFileName(url: String): String {
        return url.substringAfterLast("/")
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_SENT) {
            holder as SendAdapter
            //holder.setMsg(chatBeanList[position])
            sender(holder, position)
        } else {
            holder as ReceiverAdapter
            //holder.setMsg(chatBeanList[position], receiverBitmap)
            receiver(holder, position, receiverAvatar)
        }

    }

    // 条目
    private fun sender(holder: SendAdapter, position: Int) {
        val dataBinding = holder.dataBinding
        val chat = chatList[position]
        val context = holder.itemView.context

        // basic
        dataBinding.textDateTime.text = DateFormatUtil.formatTime(chat.sendTime)

        // 下载
        dataBinding.btnDownload.setOnClickListener {
            GlideUtil.loadLocalGifImage(context, R.drawable.loading).into(dataBinding.btnDownload)

            listener.download(chat) {
                dataBinding.btnDownload.visibility = View.GONE
            }
        }
        // 预览
        dataBinding.messageImg.setOnClickListener {
            listener.imagePreview(this.chatList, position)
        }
        // TODO 视频预览
        dataBinding.messageVideo.setOnClickListener {
            dataBinding.messageVideo.visibility = View.INVISIBLE
            val url = getPath(chat.message, chat.msgType)
            url?.let {
                listener.videoPreview(dataBinding.videoPlayer, it) {
                    dataBinding.messageVideo.visibility = View.VISIBLE
                }
            }
        }

        holder.hideAll()

        when (chat.msgType) {
            MessageType.TEXT.type -> {
                dataBinding.textMessage.visibility = View.VISIBLE
                dataBinding.textMessage.text = chat.message
            }

            MessageType.IMAGE.type -> {
                //val bitmap = FileUtil.getBitmap(uri)
                val maxWidth = (DensityUtils.getWidth(context) * 0.4).toInt()
                val requestOptions = RequestOptions().transform(RoundedCorners(20))
                //LogUtil.info("path -> ${getPath(chat.message, chat.type)}")
                GlideUtil.loadUrlImage(context, getPath(chat.message, chat.msgType))
                    .override(300)
                    .apply(requestOptions)
                    .into(dataBinding.messageImg)

                dataBinding.messageImg.visibility = View.VISIBLE
            }

            MessageType.FILE.type -> {
                dataBinding.btnDownload.visibility = View.VISIBLE
                dataBinding.messageFileName.text =
                    getPath(chat.message, chat.msgType)?.let { extractFileName(it) }
                dataBinding.fileWrapper.visibility = View.VISIBLE
            }

            MessageType.VIDEO.type -> {
                val maxWidth = (DensityUtils.getWidth(holder.itemView.context) * 0.4).toInt()
                val requestOptions = RequestOptions().transform(RoundedCorners(20))

                //LogUtil.info("path -> ${getPath(chat.message, chat.type)}")
//                GlideUtil.loadUrlImage(context, getPath(chat.message, chat.msgType))
//                    .override(maxWidth)
//                    .apply(requestOptions)
//                    .into(dataBinding.messageVideo)

                dataBinding.videoWrapper.visibility = View.VISIBLE
            }
        }
    }

    private fun receiver(holder: ReceiverAdapter, position: Int, receiverAvatar: String?) {
        val dataBinding = holder.dataBinding
        val chat = chatList[position]
        val context = holder.itemView.context

        // basic
        dataBinding.textDateTime.text = DateFormatUtil.formatTime(chat.sendTime)
        GlideUtil.loadUrlImage(context, HttpHelper.getFileUrl(receiverAvatar))
            .into(dataBinding.imageProfile)


        // 下载
        dataBinding.btnDownload.setOnClickListener {
            GlideUtil.loadLocalGifImage(context, R.drawable.loading).into(dataBinding.btnDownload)

            listener.download(chat) {
                dataBinding.btnDownload.visibility = View.GONE
            }
        }

        // 预览
        dataBinding.messageImg.setOnClickListener {
            listener.imagePreview(this.chatList, position)
        }

        // TODO 视频预览
        dataBinding.messageVideo.setOnClickListener {
            dataBinding.messageVideo.visibility = View.INVISIBLE
            val url = getPath(chat.message, chat.msgType)
            url?.let {
                listener.videoPreview(dataBinding.videoPlayer, it) {
                    dataBinding.messageVideo.visibility = View.VISIBLE
                }
            }
        }


        holder.hideAll()

        when (chat.msgType) {
            MessageType.TEXT.type -> {
                dataBinding.textMessage.visibility = View.VISIBLE
                dataBinding.textMessage.text = chat.message
            }

            MessageType.IMAGE.type -> {
                //val bitmap = FileUtil.getBitmap(uri)
                val maxWidth = (DensityUtils.getWidth(holder.itemView.context) * 0.4).toInt()

                val requestOptions = RequestOptions().transform(RoundedCorners(20))

                //LogUtil.info("path -> ${getPath(chat.message, chat.type)}")
                GlideUtil.loadUrlImage(context, getPath(chat.message, chat.msgType))
                    .override(maxWidth)
                    .apply(requestOptions)
                    .into(dataBinding.messageImg)

                dataBinding.messageImg.visibility = View.VISIBLE
            }

            MessageType.FILE.type -> {
                dataBinding.btnDownload.visibility = View.VISIBLE
                dataBinding.messageFileName.text =
                    getPath(chat.message, chat.msgType)?.let { extractFileName(it) }
                dataBinding.fileWrapper.visibility = View.VISIBLE
            }

            MessageType.VIDEO.type -> {
                val maxWidth = (DensityUtils.getWidth(holder.itemView.context) * 0.4).toInt()
                val requestOptions = RequestOptions().transform(RoundedCorners(20))

                //LogUtil.info("path -> ${getPath(chat.message, chat.type)}")
//                GlideUtil.loadUrlImage(context, getPath(chat.message, chat.msgType))
//                    .override(maxWidth)
//                    .apply(requestOptions)
//                    .into(dataBinding.messageVideo)

                dataBinding.videoWrapper.visibility = View.VISIBLE
            }
        }
    }
}