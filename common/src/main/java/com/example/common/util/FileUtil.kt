package com.example.common.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import com.zuad.baselib.utils.AppGlobals
import java.io.File
import java.io.FileOutputStream


object FileUtil {
    fun getBitmap(uri: String): Bitmap? {
        val resolver = AppGlobals.get()?.applicationContext?.contentResolver
        val resolverIs = resolver?.openInputStream(uri.toUri()) ?: return null
        // 从输入流解码位图
        val bitmap = BitmapFactory.decodeStream(resolverIs)
        // 现在你可以使用bitmap对象，例如显示在ImageView上
        resolverIs.close()
        return bitmap
    }


    fun saveContentToFile(context: Context, uri: Uri, fileName: String): String? {
        LogUtil.info("fileName -> $fileName")
        val directory = getPath(context) ?: return null

        val resolver = context.contentResolver
        val resolverIs = resolver.openInputStream(uri) ?: return null

        val file = File(directory, fileName)
        val os = FileOutputStream(file)

        val buffer = ByteArray(1024)
        var byRead = resolverIs.read(buffer)


        while (byRead != -1) {
            os.write(buffer, 0, byRead)
            byRead = resolverIs.read(buffer)
        }

        LogUtil.info("保存成功")
        LogUtil.info("保存路径 -> $directory")
        resolverIs.close()
        os.close()

        return file.absolutePath
    }

    fun getPath(context: Context): String? {

        val externalFilesDir =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null // 没有外部存储

        if (!externalFilesDir.exists())
            externalFilesDir.mkdirs()

        LogUtil.info("目录是否存在 ${externalFilesDir.exists()}")
        // 构建文件路径
        return "${externalFilesDir.absolutePath}/${UserStatusUtil.getUserId()}"
    }
}