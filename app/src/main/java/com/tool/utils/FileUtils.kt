package com.tool.utils

import com.tool.app.MyApp
import com.orhanobut.logger.Logger
import com.tool.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 *    Author : wxz
 *    Time   : 2020/12/17
 *    Desc   :
 */
object FileUtils {
    const val FILENAME = "homework_yyyyMMdd_HHmmssSSS"
    const val PHOTO_EXTENSION = ".jpg"

    /** Helper function used to create a timestamped file */
    fun createFile(baseFolder: File, format: String, extension: String) =
        File(
            baseFolder, SimpleDateFormat(format, Locale.CHINA)
                .format(System.currentTimeMillis()) + extension
        )

    /** Use external media if it is available, our app's file directory otherwise */
    fun getOutputDirectory(): File {
        val appContext = MyApp.context
        val mediaDir = MyApp.context.externalMediaDirs.firstOrNull()?.let {
            File(
                it,
                appContext.resources.getString(R.string.picture_upload_path)
            ).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    fun deleteFile(path: String) {
        val file = File(path)
        if (file.exists()) {
            if (file.delete()) {
                Logger.d(MyApp.context.getString(R.string.file_delete_success, path))
            }
        }
    }
}