package com.example.mychatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.common.util.DateFormatUtil
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.FriendApply
import com.example.database.enums.ApplyStatusEnum
import com.example.mychatapp.R
import com.example.mychatapp.listener.FriendApplyStatusListener
import com.example.mychatapp.util.HttpHelper
import com.google.gson.Gson
import com.makeramen.roundedimageview.RoundedImageView

class FriendApplyStatusAdapter(
    private var listener: FriendApplyStatusListener,
    private var applyList: MutableList<FriendApply>
) :
    RecyclerView.Adapter<FriendApplyStatusAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profile: RoundedImageView = itemView.findViewById(R.id.imageProfile)
        val targetName: TextView = itemView.findViewById(R.id.textName)
        val applyInfo: TextView = itemView.findViewById(R.id.applyInfo)
        val applyStatus: TextView = itemView.findViewById(R.id.applyStatus)
        val applyBtn: TextView = itemView.findViewById(R.id.applyBtn)
        val wrapper: LinearLayout = itemView.findViewById(R.id.applyInfoWrapper)
        val time: TextView = itemView.findViewById(R.id.applyTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_status, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return applyList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val applyInfo = applyList[position]

        holder.time.text = DateFormatUtil.formatTime(applyInfo.time)
        LogUtil.info("applicant ${Gson().toJson(applyInfo)}")
        if (applyInfo.target == UserStatusUtil.getCurLoginUser()) {
            Glide.with(holder.itemView.context)
                .load(HttpHelper.getFileUrl(applyInfo.applicantAvatar))
                .placeholder(R.drawable.default_profile)
                .into(holder.profile)
        } else {
            Glide.with(holder.itemView.context)
                .load(HttpHelper.getFileUrl(applyInfo.targetAvatar))
                .placeholder(R.drawable.default_profile)
                .into(holder.profile)
        }



        if (applyInfo.info.isNullOrEmpty()) {
            holder.wrapper.visibility = View.INVISIBLE
        } else {
            holder.applyInfo.text = applyInfo.info
        }

        // 被申请
        if (applyInfo.target == UserStatusUtil.getCurLoginUser()) {
            //holder.profile
            holder.targetName.text = applyInfo.applicantName
            holder.applyBtn.visibility = View.VISIBLE
            holder.applyStatus.visibility = View.INVISIBLE

            holder.applyBtn.text = ApplyStatusEnum.fromCode(applyInfo.status).description

            if (applyInfo.status == ApplyStatusEnum.PENDING.code) {
                holder.applyBtn.setOnClickListener {
                    listener.setApplyStatus(applyInfo)
                }
            }
        } else {
            holder.targetName.text = applyInfo.target
            holder.applyBtn.visibility = View.INVISIBLE
            holder.applyStatus.visibility = View.VISIBLE
            holder.applyStatus.text = ApplyStatusEnum.fromCode(applyInfo.status).description
        }
    }
}