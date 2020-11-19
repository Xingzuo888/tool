package com.tool.nc

import android.util.Log
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

/**
 *    Author : wxz
 *    Time   : 2020/11/06
 *    Desc   :
 */
class MyResponseBody : ResponseBody {
    private var responseBody: ResponseBody
    private var listener: DownloadListener
    private var bufferedSource: BufferedSource? = null

    constructor(responseBody: ResponseBody, listener: DownloadListener) {
        this.responseBody = responseBody
        this.listener = listener
    }

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()))
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L

            @Throws(IOException::class)
            override fun read(sink: Buffer?, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                Log.e(
                    "download",
                    "read: " + (totalBytesRead * 100 / responseBody.contentLength()).toInt()
                )
                if (null != listener) {
                    if (bytesRead != -1L) {
                        listener.onProgress((totalBytesRead * 100 / responseBody.contentLength()).toInt())
                    }
                }
                return bytesRead
            }
        }
    }
}