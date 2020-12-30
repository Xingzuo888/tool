package com.tool.global

import com.tool.R
import com.tool.app.MyApp
import java.io.File

/**
 *    Author : wxz
 *    Time   : 2020/12/15
 *    Desc   :
 */
object Config {
    val PICTUREUPLOADPATH = File(
        MyApp.context.externalMediaDirs.firstOrNull().toString(), MyApp.context.resources.getString(
            R.string.picture_upload_path
        )
    ).path
    val FILESDIR = File(MyApp.context.filesDir.toString()).path
}