package com.tool.nc

/**
 *    Author : wxz
 *    Time   : 2020/11/06
 *    Desc   :
 */
interface DownloadListener {
    fun onStartDownload()

    fun onProgress(progress: Int)

    fun onFinishDownload()

    fun onFail(errorInfo: String?)
}