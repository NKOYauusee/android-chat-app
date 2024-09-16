package com.example.mychatapp.util

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.common.util.LogUtil
import com.example.mychatapp.util.engine.GlideEngine
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import me.rosuh.filepicker.bean.FileItemBeanImpl
import me.rosuh.filepicker.config.AbstractFileFilter
import me.rosuh.filepicker.config.FilePickerManager
import java.io.File
import java.util.Date


object SelectMediaHelper {
    fun selectMedia(
        context: Context,
        maxSelectNum: Int = 1,
        callback: (result: ArrayList<LocalMedia>) -> Unit
    ) {
        PictureSelector.create(context)
            .openGallery(SelectMimeType.ofAll())
            .setMaxSelectNum(maxSelectNum)
            .setImageSpanCount(4)
            .setImageEngine(GlideEngine.createGlideEngine())
            .isDirectReturnSingle(true)
            .setCropEngine { fragment, srcUri, _, selectedList, _ ->
                // 获取系统相册的路径
                val pictureDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val picturePath = pictureDirectory.path
                // 创建一个文件用于保存裁剪后的图片

                // 创建一个文件用于保存裁剪后的图片
                val fileName = Date().time.toString()
                val destinationFile = File(picturePath, "chatapp_${fileName}_crop.jpg")

                //
                val ucropOptions = UCrop.Options()
                ucropOptions.setHideBottomControls(true)
                ucropOptions.setShowCropFrame(true)
                // 不允许自由调整裁剪框


                if(maxSelectNum == 1){
                    ucropOptions.withAspectRatio(1f, 1f)
                    ucropOptions.setFreeStyleCropEnabled(false)
                }

                ucropOptions.setAllowedGestures(
                    UCropActivity.SCALE,
                    UCropActivity.ROTATE,
                    UCropActivity.ALL
                )

                UCrop.of(srcUri, Uri.fromFile(destinationFile), selectedList)
                    .withOptions(ucropOptions).start(context, fragment)

            }
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    if (result.isNullOrEmpty()) {
                        LogUtil.info("未选择任何图片")
                        return
                    }
                    LogUtil.info("选择结束")
                    callback(result)
                    //LogUtil.info("选择了 ${result.size} 图片")
                    //LogUtil.info("选择了 ${result[0].availablePath} 图片")
                }

                override fun onCancel() {
                    LogUtil.info("取消选择")
                }
            })
    }


    fun selectFile(activity: Activity) {
        FilePickerManager.from(activity)
            .filter(object : AbstractFileFilter() {
                override fun doFilter(listData: ArrayList<FileItemBeanImpl>): ArrayList<FileItemBeanImpl> {

//                    return ArrayList(listData.filter { item ->
//                        // 文件夹或是图片，则被滤出来，然后新建一个 ArrayList 返回给调用者即可
//                        ((item.isDir) || item.fileType !is RasterImageFileType)
//                    })

                    return listData
                }
            })
            //.registerFileType(arrayListOf(CustomFileType(0,"mp4")), true)
            .maxSelectable(3)
            .forResult(FilePickerManager.REQUEST_CODE)
    }
}