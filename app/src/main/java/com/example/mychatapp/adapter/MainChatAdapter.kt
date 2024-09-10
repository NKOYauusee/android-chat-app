package com.example.mychatapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.common.common.Constants
import com.example.common.util.DateFormatUtil
import com.example.database.bean.HasChatBean
import com.example.mychatapp.R
import com.example.mychatapp.listener.MainChatListener
import com.makeramen.roundedimageview.RoundedImageView
import java.util.Date

class MainChatAdapter(
    private var chatList: MutableList<HasChatBean>,
    private var listen: MainChatListener
) :
    RecyclerView.Adapter<MainChatAdapter.MainChatViewHolder>() {

    // 添加新选项
    private fun addItem(
        chatBean: HasChatBean,
        callback: (chatList: MutableList<HasChatBean>) -> Unit = {}
    ) {
        chatList.add(chatBean)
        notifyItemInserted(chatList.size - 1)
        notifyItemChanged(chatList.size - 1, true)
        callback(chatList)
    }

    fun setUserList(
        list: MutableList<HasChatBean>,
        callback: (chatList: MutableList<HasChatBean>) -> Unit = {}
    ) {
        val oldSize = chatList.size
        chatList.clear() // 清空旧数据
        chatList.addAll(list) // 添加新数据
        notifyItemRangeInserted(oldSize, list.size) // 从旧数据的末尾开始插入新数据
        notifyItemRangeChanged(oldSize, list.size, false)
        callback(chatList)
    }

    class MainChatViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val avatar: RoundedImageView = item.findViewById(R.id.imageProfile)
        val nickName: TextView = item.findViewById(R.id.textName)
        val msg: TextView = item.findViewById(R.id.textMsg)
        val time: TextView = item.findViewById(R.id.textTime)
        val dot: ImageView = item.findViewById(R.id.msgNotify)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_user, parent, false)

        return MainChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(
        holder: MainChatViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)

        updateHolder(holder, position)
        if (payloads.isNotEmpty()) {
            if (payloads[0] is Boolean && payloads[0] as Boolean || !chatList[position].isRead) {
                holder.dot.visibility = View.VISIBLE
            } else {
                holder.dot.visibility = View.INVISIBLE
            }
        }
    }

    override fun onBindViewHolder(holder: MainChatViewHolder, position: Int) {
        updateHolder(holder, position)
    }

    fun updateMsg(hasChatBean: HasChatBean, callback: ((idx: Int) -> Unit)) {
        if (!contains(hasChatBean)) {
            addItem(hasChatBean)

            chatList = mutableListOf(hasChatBean)
        }

        chatList.forEachIndexed { idx, item ->
            if (item.email == hasChatBean.email) {
                item.sendTime = hasChatBean.sendTime
                item.newMsg = hasChatBean.newMsg
                notifyItemChanged(idx, true)

                callback(idx)
                return
            }
        }
    }

    fun contains(hasChatBean: HasChatBean): Boolean {
        if (chatList.isEmpty())
            return false

        for (item in chatList) {
            if (item.email == hasChatBean.email)
                return true
        }

        return false
    }

    @SuppressLint("SetTextI18n")
    private fun updateHolder(holder: MainChatViewHolder, position: Int) {
        //holder.avatar
        holder.nickName.text = chatList[position].nickname
        holder.msg.text = chatList[position].newMsg

        // 获取当前日期的零点时间（即今天开始的时间）
        val todayStart = DateFormatUtil.getStartOfDay(System.currentTimeMillis())

        // 获取昨天日期的零点时间（即昨天开始的时间）
        val yesterdayStart = todayStart - Constants.ONE_DAY

        val gap: Long = Date().time - chatList[position].sendTime!!
        when {
            (chatList[position].sendTime!! >= todayStart) -> {
                holder.time.text = DateFormatUtil.formatTime(chatList[position].sendTime!!, "HH:mm")
            }

            (chatList[position].sendTime!! in yesterdayStart until todayStart) -> {
                holder.time.text =
                    "昨天 ${DateFormatUtil.formatTime(chatList[position].sendTime!!, "HH:mm")}"
            }

            (gap in Constants.ONE_DAY * 30 until Constants.ONE_DAY * 365) -> {
                holder.time.text =
                    DateFormatUtil.formatTime(
                        chatList[position].sendTime!!,
                        "MM月dd HH:mm"
                    )
            }

            else -> {
                holder.time.text =
                    DateFormatUtil.formatTime(chatList[position].sendTime!!)
            }
        }

        if (!chatList[position].isRead) {
            holder.dot.visibility = View.VISIBLE
        } else {
            holder.dot.visibility = View.INVISIBLE
        }

        holder.itemView.setOnClickListener {
            holder.dot.visibility = View.INVISIBLE
            chatList[position].isRead = true

            listen.onClicked(chatList[position])
        }
    }

    fun isEmpty(): Boolean {
        return chatList.isEmpty()
    }
}