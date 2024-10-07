package com.example.mychatapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.api.bean.MyObservable
import com.example.api.bean.ResBean
import com.example.api.helper.ApiServiceHelper
import com.example.common.common.Constants
import com.example.common.common.DataBindingConfig
import com.example.common.ui.BaseActivity
import com.example.common.util.LogUtil
import com.example.common.util.ToastUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.ChatBean
import com.example.database.bean.UserFriBean
import com.example.database.enums.MessageType
import com.example.database.helper.ChatListHelper
import com.example.mychatapp.BR
import com.example.mychatapp.R
import com.example.mychatapp.adapter.ChatAdapter
import com.example.mychatapp.components.MyToast
import com.example.mychatapp.databinding.ActivityChatBinding
import com.example.mychatapp.listener.ChatMsgListener
import com.example.mychatapp.util.ChatHelper
import com.example.mychatapp.util.FileDownloadHelper
import com.example.mychatapp.util.FileUploadWorker
import com.example.mychatapp.util.SelectMediaHelper
import com.example.mychatapp.viewmodel.ChatViewModel
import com.example.mychatapp.websocket.WebSocketManager
import com.flyjingfish.openimagelib.OpenImage
import com.google.gson.Gson
import com.luck.picture.lib.entity.LocalMedia
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.rosuh.filepicker.config.FilePickerManager
import java.io.File
import java.util.concurrent.TimeUnit


class ChatActivity : BaseActivity<ActivityChatBinding, ChatViewModel>(), ChatMsgListener {
    private var chatListAdapter: ChatAdapter? = null

    //private val gson = Gson()
    private var uploadRequest: OneTimeWorkRequest? = null

    private val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadReceiver()

        initListener()
        loadMsg()
        //ZoomMediaLoader.getInstance().init(ImagePreviewLoader())


    }

    override fun onResume() {
        super.onResume()
        listener()
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_chat, BR.vm)
    }

    // 加载对方用户基本信息
    private fun loadReceiver() {
        val curChatUser = intent.getSerializableExtra(Constants.CHAT_FRIEND) as UserFriBean?

        //LogUtil.info("loadReceiver " + gson.toJson(curChatUser))
        viewModel.receiver.postValue(curChatUser)

        dataBinding.chatName.text = curChatUser?.username
    }

    private fun initListener() {
        // 返回
        dataBinding.imgBack.setOnClickListener {
            finish()
        }

        dataBinding.inputMsg.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) changeMoreWrapperStyle(true)
        }

        // 发送消息
        dataBinding.layoutSendMsg.setOnClickListener {
            val msg = dataBinding.inputMsg.text.toString()
            if (msg.isEmpty()) return@setOnClickListener


            lifecycleScope.launch(Dispatchers.IO) {
                val chat: ChatBean = ChatHelper.generateChat(
                    viewModel.receiver.value?.email!!, viewModel.receiver.value?.username!!, msg
                )


                WebSocketManager.instance.sendMsg(chat) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        MyToast(this@ChatActivity).show(getString(R.string.info_send_failed_check_network))
                    }
                }
//                MainUserSelectHelper.insertProfile(
//                    this@ChatActivity,
//                    viewModel.receiver.value!!
//                )
            }
            dataBinding.inputMsg.setText("")
        }

        dataBinding.layoutMore.setOnClickListener {
            changeMoreWrapperStyle()
        }
        // 媒体选择
        dataBinding.selectMedia.setOnClickListener {
            SelectMediaHelper.selectMedia(this, 5) {
                LogUtil.info(Gson().toJson(it))

                for (selectFile in it) {
                    val file = if (selectFile.isCut) File(selectFile.cutPath) else File(
                        selectFile.realPath
                    )

                    if (file.exists()) {
                        if (selectFile.mimeType.contains("image/")) {
                            if (file.length() < Constants.CHUNK_SIZE)
                                uploadImg(file, selectFile)
                            else {
                                backgroundUploadFile(
                                    viewModel.receiver.value?.email!!,
                                    viewModel.receiver.value?.username!!,
                                    file,
                                    MessageType.IMAGE.type
                                )
                            }
                        } else if (selectFile.mimeType.contains("video/")) {
                            backgroundUploadFile(
                                viewModel.receiver.value?.email!!,
                                viewModel.receiver.value?.username!!,
                                file,
                                MessageType.VIDEO.type
                            )
                        } else {
                            backgroundUploadFile(
                                viewModel.receiver.value?.email!!,
                                viewModel.receiver.value?.username!!,
                                file,
                                MessageType.FILE.type
                            )
                        }
                    }
                }
            }
        }

        dataBinding.selectFile.setOnClickListener {
            SelectMediaHelper.selectFile(this)
        }


        dataBinding.chatRecycleView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                lifecycleScope.launch(Dispatchers.Main) {
                    chatListAdapter?.scrollToLastIdx {
                        dataBinding.chatRecycleView.scrollToPosition(it)
                    }

                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerManager.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val list = FilePickerManager.obtainData()
                    LogUtil.info("文件选择 ${Gson().toJson(list)}")
                    handleFile(list)
                }
            }
        }
    }

    private fun handleFile(list: MutableList<String>) {
        for (filePath in list) {
            val file = File(filePath)
            if (!file.exists()) continue
            backgroundUploadFile(
                viewModel.receiver.value?.email!!,
                viewModel.receiver.value?.username!!,
                file,
                MessageType.FILE.type
            )
        }
    }

    private val mutex = Mutex()

    private fun backgroundUploadFile(receiver: String, name: String, file: File, fileType: Int) {
        lifecycleScope.launch {
            mutex.withLock {
                val inputData = workDataOf(
                    "file_path" to file.absolutePath,
                    "file_type" to fileType,
                    "receiver" to receiver,
                    "name" to name
                )

                uploadRequest =
                    OneTimeWorkRequestBuilder<FileUploadWorker>()
                        .setInputData(inputData)
                        .setConstraints(constraints)
                        .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                        .build()

                WorkManager.getInstance(this@ChatActivity)
                    .enqueueUniqueWork(
                        System.currentTimeMillis().toString(),
                        ExistingWorkPolicy.REPLACE,
                        uploadRequest!!
                    )
            }
        }

//        // 监听任务状态
//        WorkManager.getInstance(this).getWorkInfoByIdLiveData(uploadRequest.id)
//            .observe(this) { workInfo ->
//                if (workInfo != null && workInfo.state.isFinished) {
//                    val result = workInfo.outputData.getString("filePath")
//                    LogUtil.info("上传任务完成 res -> $result")
//                }
//            }
    }

    private fun listener() {
        // 下滑顶部/底部监听
        dataBinding.chatRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastItemPos = layoutManager.itemCount - 1
                if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    // 到达顶部，加载旧数据
                    lifecycleScope.launch(Dispatchers.Main) {
                        val topChat = chatListAdapter?.returnTopChat() ?: return@launch
                        //LogUtil.info("顶部消息 -> ${Gson().toJson(topChat)}")
                        val list = withContext(Dispatchers.IO) {
                            ChatListHelper.loadHistory10Msg(this@ChatActivity, topChat)
                        }
                        //LogUtil.info("历史消息 -> ${Gson().toJson(list)}")
                        chatListAdapter?.topLoadMore(list)
                    }
                } else if (layoutManager.findLastVisibleItemPosition() == lastItemPos) {
                    loadNewMsg()
                }
            }
        })

        //消息接收
        WebSocketManager.instance.receiveMessage(this) {
            lifecycleScope.launch(Dispatchers.Main) {
                //viewModel.historyMsg.value?.add(chat)
                chatListAdapter!!.addNewMsg(it) { i ->
                    dataBinding.chatRecycleView.scrollToPosition(i.size - 1)
                }
//                withContext(Dispatchers.IO) {
//                    MainUserSelectHelper.insertProfile(
//                        this@ChatActivity,
//                        viewModel.receiver.value!!
//                    )
//                }
            }
        }
    }

    // 滚动到底部数 防抖处理
    private var loadNewMsgJob: Job? = null
    private fun loadNewMsg() {
        if (loadNewMsgJob?.isActive == true) return

        loadNewMsgJob = lifecycleScope.launch(Dispatchers.Main) {
            val bottomChat = chatListAdapter?.returnBottomChat() ?: return@launch
            val friend =
                if (bottomChat.owner == bottomChat.sender) bottomChat.receiver else bottomChat.sender

            val list = withContext(Dispatchers.IO) {
                ChatListHelper.loadNewMsg(this@ChatActivity, friend, bottomChat.sendTime)
            }
            chatListAdapter?.bottomLoadMore(list)

        }
    }

    private fun uploadImg(file: File, data: LocalMedia) {
        //LogUtil.info("开始上传")
        val fileSize = ApiServiceHelper.getRequestBody(data.size, "text/plain")
        val fileName = ApiServiceHelper.getRequestBody(data.fileName, "text/plain")
        val fileType = ApiServiceHelper.getRequestBody(data.mimeType, "text/plain")
        val userId = ApiServiceHelper.getRequestBody(UserStatusUtil.getUserId(), "text/plain")
        val fileData = ApiServiceHelper.getRequestBodyPart(file, data.mimeType)

        LogUtil.info("文件类型: ${data.mimeType}")

        ApiServiceHelper.service().uploadFile(
            fileData, userId, fileName, fileType, fileSize
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : MyObservable<ResBean<String>>() {

                override fun success(res: ResBean<String>) {
                    //LogUtil.info(Gson().toJson(res))
                    // res.data -> 2/20240912/20240912_W7ZYt5OnmdCY.jpg
                    val chat: ChatBean = ChatHelper.generateChat(
                        viewModel.receiver.value?.email!!,
                        viewModel.receiver.value?.username!!,
                        res.data!!,
                        MessageType.IMAGE.type
                    )

                    WebSocketManager.instance.sendMsg(chat)
                }

                override fun failed(e: Throwable) {
                    Log.e("xht", "failed: ", e)
                }
            })
    }

    //加载特定位置的起的10条数据 (默认最近10条消息)
    private fun loadMsg() {
        val chat = intent.getSerializableExtra(Constants.CHAT_START) as? ChatBean

        //LogUtil.info("消息起始位置" + Gson().toJson(chat))
        val startDate: Long = chat?.sendTime ?: 0
        //LogUtil.info("消息起始位置" + Gson().toJson(startDate))
        lifecycleScope.launch(Dispatchers.Main) {
            loadNewestMsg(startDate)
            //else loadMoreMsg(startIdx)
        }
    }

    private suspend fun loadNewestMsg(startDate: Long = 0) {
        var list = withContext(Dispatchers.IO) {
            if (startDate == 0L) {
                viewModel.receiver.value?.email?.let {
                    ChatListHelper.loadRecentMsg(
                        this@ChatActivity, it
                    )
                }
            } else {
                viewModel.receiver.value?.email?.let {
                    ChatListHelper.loadSpecificMsg(
                        this@ChatActivity, it, startDate
                    )
                }
            }
        }
        //LogUtil.info(Gson().toJson(chatListAdapter?.returnTopChat()))
        //LogUtil.info(Gson().toJson(list))

        loading(false)

        if (list == null) list = mutableListOf()
        chatListAdapter = ChatAdapter(mutableListOf(), viewModel.receiver.value?.avatar, this)

        dataBinding.chatRecycleView.adapter = chatListAdapter

        chatListAdapter!!.setChatList(list) {
            dataBinding.chatRecycleView.scrollToPosition(it.size - 1)
        }
        // TODO 强制滚动到底部
        if (startDate == 0L) {
            delay(500)
            chatListAdapter?.scrollToLastIdx {
                dataBinding.chatRecycleView.scrollToPosition(it)
            }
        }
    }

    //
    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            dataBinding.chatProgressBar.visibility = View.VISIBLE
        } else {
            dataBinding.chatProgressBar.visibility = View.INVISIBLE
        }
    }

    //
    private fun changeMoreWrapperStyle(forceClose: Boolean = false) {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(300)
            if (forceClose) {
                dataBinding.layoutMoreWrapper.visibility = View.GONE
                return@launch
            }

            if (dataBinding.layoutMoreWrapper.visibility == View.VISIBLE) {
                dataBinding.layoutMoreWrapper.visibility = View.GONE
            } else {
                dataBinding.layoutMoreWrapper.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
        uploadRequest?.let {
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(it.id)
                .removeObservers(this)
        }
    }

    override fun videoPreview(
        videoPlayer: StandardGSYVideoPlayer,
        url: String,
        callback: () -> Unit
    ) {
        LogUtil.info("视频预览 $url")

        videoPlayer.setUp(url, true, "")
        videoPlayer.backButton.visibility = View.INVISIBLE

        //getPath(chat.message, chat.msgType)?.let { it1 -> listener.videoPreView(it1) }
        videoPlayer.setIsTouchWiget(true)
//        videoPlayer.backButton.setOnClickListener {
//            videoPlayer.setVideoAllCallBack(null);
//        }

        videoPlayer.setVideoAllCallBack(object : GSYSampleCallBack() {
            override fun onAutoComplete(url: String?, vararg objects: Any?) {
                super.onAutoComplete(url, *objects)
                callback()
            }
        })

        videoPlayer.isNeedOrientationUtils = false
        videoPlayer.startPlayLogic()
    }


    override fun imagePreview(list: MutableList<ChatBean>, position: Int) {
        OpenImage.with(this).setClickRecyclerView(
            dataBinding.chatRecycleView
        ) { _, _ ->
            R.id.message_img
        } //点击的ImageView的ScaleType类型（如果设置不对，打开的动画效果将是错误的）
            .setSrcImageViewScaleType(ImageView.ScaleType.CENTER_CROP, true)
            //RecyclerView的数据
            .setImageUrlList(list)
            //点击的ImageView所在数据的位置
            .setClickPosition(position).show()
    }

    // TODO 下载文件
    override fun download(chat: ChatBean, callback: () -> Unit) {
        MyToast(this).show(getString(R.string.info_start_downloading))
        lifecycleScope.launch(Dispatchers.IO) {
            ChatHelper.getPath(chat.message, chat.msgType)?.let {
                LogUtil.info("视频下载地址 $it")
                val fileName = "file_" + chat.message.substringAfterLast("/")
                val downloader = FileDownloadHelper(it, fileName)
                downloader.download()

                withContext(Dispatchers.Main) {
                    ToastUtil.showToastMsg(
                        "${downloader.getFileDownLoadPath()} ${getString(R.string.info_download_complete)}",
                        this@ChatActivity
                    )
                    callback()
                }
                //val size = FileDownloadHelper(it, fileName).getContentLength()
                //LogUtil.info("$size")
            }
        }
    }

    private var dialog: Dialog? = null
    override fun deleteMsg(chat: ChatBean, callback: () -> Unit) {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this).setTitle("确定删除该条聊天记录")
            .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                dialog.dismiss()

                lifecycleScope.launch(Dispatchers.IO) {
                    ChatListHelper.deleteOneMsg(this@ChatActivity, chat)
                    withContext(Dispatchers.Main) {
                        callback()
                    }
                }
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.create()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
        supportActionBar?.hide()
    }
}