package com.example.mychatapp.util

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.example.mychatapp.R

object GlideUtil {
    fun loadUrlImage(context: Context, url: String?): RequestBuilder<Drawable> {
        return Glide.with(context)
            .load(url)
            .placeholder(R.drawable.image_placeholder)
    }


    fun loadLocalImage(context: Context, source: Int): RequestBuilder<Drawable> {
        return Glide.with(context)
            .load(source)
            .placeholder(R.drawable.image_placeholder)
    }
}