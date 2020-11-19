package com.nextclass.ai.folder.utils.nc

import android.content.Context
import com.google.gson.GsonBuilder
import com.tool.nc.NcServer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

/**
 *    Author : wxz
 *    Time   : 2020/10/26
 *    Desc   : 网络请求
 *
 *
 *    接口文档  https://pan.baidu.com/union/document/entrance#%E7%AE%80%E4%BB%8B
 */

object NetWorkUtil {
    const val TAG = "NetWorkUtil"
    private val gson = GsonBuilder().setLenient().create()
    private const val host2 = "https://d.pcs.baidu.com/"
    private const val dataParse = "multipart/form-data"
    private var accessToken = "" //自己授权后获得的token，这里就解释了，不会的去百度网盘开发者文档查看
    private val server: NcServer

    init {
        val client = OkHttpClient.Builder()

        val retrofit = Retrofit.Builder()
            .baseUrl(host2)
            .client(client.build())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        server = retrofit.create(NcServer::class.java)
    }


    /**
     * 分片上传
     * @param path 上传后使用的文件绝对路径，需要urlencode
     * @param uploadid precreate接口下发的uploadid
     * @param partseq 文件分片的位置序号，从0开始，参考precreate接口返回的block_list
     * @param file 上传的文件内容
     */
    fun upload(
        context: Context,
        path: String,
        uploadid: String,
        partseq: Int,
        file: File
    ) {
        accessToken = SPUtils.get(context, "access_token", "") as String
        //以表单的方式上传文件
        val map: MutableMap<String, RequestBody> = HashMap()
        val requestFile = RequestBody.create(
            MediaType.parse(dataParse),
            file
        )
        map["file"] = requestFile
        val createFormData = MultipartBody.Part.createFormData("file", path, requestFile)
        server.upload(
            accessToken,
            "tmpfile",
            path,
            uploadid,
            partseq,
            createFormData
        )
            .subscribeOn(Schedulers.io())//被观察者
            .observeOn(AndroidSchedulers.mainThread())//响应端
            .subscribeBy(
                onNext = {
                    //请求接口成功，在此处理
                },
                onError = {
                    //请求返回异常，在此处理
                }
            )
    }
}