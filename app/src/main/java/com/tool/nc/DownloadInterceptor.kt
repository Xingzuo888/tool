package com.tool.nc

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 *    Author : wxz
 *    Time   : 2020/11/06
 *    Desc   :
 */
class DownloadInterceptor(var listener: DownloadListener) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        return response.newBuilder().body(
            MyResponseBody(response.body()!!, listener)
        ).build()
    }
}