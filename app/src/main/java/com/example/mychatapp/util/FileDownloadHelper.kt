package com.example.mychatapp.util

import android.os.Environment
import com.example.api.bean.HttpUrl
import com.example.common.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile

class FileDownloadHelper(url: String, fileName: String) {
    private val chunkSize: Long = 2 * 1024 * 1024  // 分片大小 (例如 2MB)
    private val client = OkHttpClient()
    private val semaphore = Semaphore(3)
    private var downloadPath =
        HttpHelper.getHttpBaseUrl() + "media/download?filePath=" + url.substringAfterLast(HttpUrl.FILE_URL)

    private var sizePath =
        HttpHelper.getHttpBaseUrl() + "media/getSize?filePath=" + url.substringAfterLast(HttpUrl.FILE_URL)

    private val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

    private val targetFile = File(path, fileName)


    // 获取文件总大小
    private suspend fun getContentLength(): Long {
        return withContext(Dispatchers.IO) { // 确保网络请求在正确的线程上执行
            val request = Request.Builder()
                .url(sizePath)
                .head()  // HEAD 请求只获取 header 信息
                .build()

            var size: Long

            client.newCall(request).execute().use { response: okhttp3.Response ->
                size = response.header("Content-Length")?.toLong() ?: 0
            }

            size
        }
    }

    // 下载指定区间的数据
    private suspend fun downloadChunk(start: Long, end: Long) {
        semaphore.acquire()

        val request = Request.Builder()
            .url(downloadPath)
            .addHeader("Range", "bytes=$start-$end")  // 指定分片的范围
            .build()
        client.newCall(request).execute().use { response: okhttp3.Response ->
            if (response.isSuccessful) {
                appendToFile(response.body?.byteStream(), start)
                LogUtil.info("区间 $start-$end 下载完成")
                semaphore.release()
            } else {
                semaphore.release()
                throw Exception("Failed to download chunk: $start-$end")
            }
        }
    }

    // 将分片追加到文件
    private fun appendToFile(inputStream: InputStream?, offset: Long) {


        inputStream?.use { input ->
            RandomAccessFile(targetFile, "rw").use { raf ->
                raf.seek(offset)  // 定位到要写入的偏移量
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    raf.write(buffer, 0, bytesRead)
                }
            }
        }
    }

    // 下载视频
    suspend fun download() {
        val totalSize = getContentLength()
        LogUtil.info("下载文件大小 $totalSize")
        var downloadedSize = 0L

        while (downloadedSize < totalSize) {
            val end = (downloadedSize + chunkSize - 1).coerceAtMost(totalSize - 1)

            withContext(Dispatchers.IO) {
                downloadChunk(downloadedSize, end)
                downloadedSize = end + 1
            }
        }

        LogUtil.info("下载完成")
    }

    fun getFileDownLoadPath(): String {
        return targetFile.path
    }
}