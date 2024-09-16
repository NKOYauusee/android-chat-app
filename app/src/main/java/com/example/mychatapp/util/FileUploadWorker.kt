package com.example.mychatapp.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.Constants
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.enums.MessageType
import com.example.mychatapp.websocket.WebSocketManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FileUploadWorker(
    context: Context,
    worker: WorkerParameters
) : CoroutineWorker(context, worker) {
    override suspend fun doWork(): Result {
//        LogUtil.info(inputData.getString("file_path").toString())
//        LogUtil.info(inputData.getString("receiver").toString())
//        LogUtil.info(inputData.getString("name").toString())

        // 获取文件路径
        val filePath = inputData.getString("file_path") ?: return Result.failure()
        val fileType: Int = inputData.getInt("file_type", MessageType.FILE.type)
        val receiver = inputData.getString("receiver") ?: return Result.failure()
        val receiverName = inputData.getString("name") ?: return Result.failure()

        val file = File(filePath)

        return try {
            val path = uploadFile(file)
            LogUtil.info("path $path")
            val chat = ChatHelper.generateChat(receiver, receiverName, path, fileType)

            WebSocketManager.instance.sendMsg(chat)

            Result.success(workDataOf("filePath" to path))  // 上传成功
        } catch (e: Exception) {
            LogUtil.info("文件上传失败: ${e.message}")
            Result.failure()  // 上传失败
        }
    }

    private suspend fun uploadFile(file: File): String {
        val totalSize = file.length()
        val totalChunk =
            (totalSize / Constants.CHUNK_SIZE + if (totalSize % Constants.CHUNK_SIZE != 0L) 1 else 0).toInt()

        var filePath = ""

        // 并发上传分片的任务数量
        val semaphore = Semaphore(2)
        withContext(Dispatchers.IO) {
            val finalChunk = totalChunk.coerceAtLeast(1)
            LogUtil.info("${file.name} 文件大小 -> $totalSize 分片数 $finalChunk")
            file.inputStream().use { inputStream ->
                var chunkIndex = 0
                var remainingSize = totalSize

                while (remainingSize > 0) {
                    val buffer = ByteArray(Constants.CHUNK_SIZE)
                    val bytesRead = inputStream.read(buffer)

                    if (bytesRead > 0) {
                        semaphore.acquire()

                        // 截取实际读取的数据，避免多余的空字节
                        val requestBody =
                            ApiServiceHelper.getBytesRequestBody(buffer.copyOf(bytesRead))

                        val filePart =
                            ApiServiceHelper.getRequestBodyPart(file.name, requestBody)

                        val userIdPart = ApiServiceHelper.getRequestBody(
                            UserStatusUtil.getUserId(), "text/plain"
                        )
                        val fileNamePart = ApiServiceHelper.getRequestBody(
                            file.name, "text/plain"
                        )

                        val chunkIndexPart = ApiServiceHelper.getRequestBody(
                            chunkIndex, "text/plain"
                        )

                        ApiServiceHelper.service().multiPartUpload(
                            fileData = filePart,
                            fileName = fileNamePart,
                            userId = userIdPart,
                            chunkIndex = chunkIndexPart
                        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : MyObservable<ResBean<String>>() {
                                override fun success(res: ResBean<String>) {
                                    //res.info?.let { LogUtil.info(it) }
                                    filePath = res.data.toString()
                                    chunkIndex++
                                    remainingSize -= bytesRead

                                    semaphore.release()
                                    LogUtil.info("chunkIndex api $chunkIndex")
                                }

                                override fun failed(e: Throwable) {
                                    LogUtil.info("上传失败")
                                }
                            })
                    }
                }

                LogUtil.info("chunkIndex $chunkIndex")
                LogUtil.info("totalChunk $totalChunk")
            }

        }

        return filePath
    }
}