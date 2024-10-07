package com.example.mychatapp.util

object GroupUtil {
    fun isGroup(receiver: String): Boolean {
        if (receiver[0] == 'G') {
            return try {
                val id = receiver.substring(1)
                if (id.length != 11) false else isAllDigits(id)
            } catch (e: Exception) {
                false
            }
        }
        return false
    }

    private fun isAllDigits(str: String?): Boolean {
        if (str == null) {
            return false
        }

        for (element in str) {
            if (!Character.isDigit(element)) {
                return false
            }
        }
        return true
    }
}