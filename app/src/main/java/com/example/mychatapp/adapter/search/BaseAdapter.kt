package com.example.mychatapp.adapter.search

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan

interface BaseAdapter {
    fun bindHighlightedItem(fullText: String, searchTerm: String): SpannableStringBuilder? {
        if(fullText.isEmpty() || searchTerm.isEmpty())
            return null

        val spanString = SpannableStringBuilder(fullText)
        var start = fullText.indexOf(searchTerm)

        if (start < 0) {
            // 如果没有找到搜索词，返回null
            return null
        }

        do {
            val end = start + searchTerm.length
            // 确保span的长度不为零
            if (start != end) {
                spanString.setSpan(
                    ForegroundColorSpan(0xFFFF0000.toInt()),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            // 继续查找下一个匹配项
            start = fullText.indexOf(searchTerm, end)
        } while (start >= 0)

        return spanString
    }
}