package com.example.mychatapp.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.database.bean.UserFriBean
import com.example.mychatapp.R
import com.example.mychatapp.listener.UserListener
import com.makeramen.roundedimageview.RoundedImageView


class UserAdapter(
    mutableList: List<UserFriBean>, userListener: UserListener
) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private var userData: MutableList<UserFriBean> = mutableListOf()
    private var userListener: UserListener? = null

    init {
        this.userData = mutableList.toMutableList()
        this.userListener = userListener
    }


    class UserViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.textName)
        val email: TextView = itemView.findViewById(R.id.textEmail)
        val avatar: RoundedImageView = itemView.findViewById(R.id.imageProfile)
        val checkbox: CheckBox = itemView.findViewById(R.id.batch_delete)

        private fun getUserImage(encodeImage: String): Bitmap? {
            val bytes = Base64.decode(encodeImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_container_user, parent, false)

        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userData.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val friend = userData[position]
        holder.name.text = friend.username
        holder.email.text = friend.email
        holder.itemView.setOnClickListener {
            userListener?.onUserClicked(friend)
        }

//        if (userListener?.setBatchStatus()?.isClickBatchManage?.value == true) {
//            holder.checkbox.visibility = View.VISIBLE
//        } else {
//            holder.checkbox.visibility = View.GONE
//        }

        //userListener?.setBatchStatus(holder.checkbox)
    }

    fun isEmpty(): Boolean {
        return userData.isEmpty()
    }

}