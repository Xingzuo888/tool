package com.wxz.libnetdisc.nc

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.wxz.netdisc.R
import com.wxz.libnetdisc.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream
import java.security.SecureRandom
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import kotlin.collections.HashMap

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
    var currentlyPath = "/"
    var fileList: MutableList<FileInfoModel> = ArrayList()
    private lateinit var server1: NcServer
    private lateinit var server2: NcServer
    private var loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
        override fun log(message: String?) {
            //打印retrofit日志
            Log.i("RetrofitLog", "retrofitBack = " + message);
        }
    })
    private val gson = GsonBuilder().setLenient().create()
    private const val host1 = "https://pan.baidu.com/"
    private const val host2 = "https://d.pcs.baidu.com/"
    private const val host3 = "https://openapi.baidu.com/"
    private const val jsonParse = "application/json; charset=UTF-8"
    private const val dataParse = "multipart/form-data"
    private var accessToken = ""
    var listener: DownloadListener? = null
    val client: OkHttpClient.Builder
    var retrofitBuilder1: Retrofit.Builder
    var retrofitBuilder2: Retrofit.Builder
    lateinit var retrofit1: Retrofit
    lateinit var retrofit2: Retrofit
    var downloadBy: Disposable? = null

    init {
        loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
        client = OkHttpClient.Builder()
        client.addInterceptor(loggingInterceptor)
        retrofitBuilder1 = Retrofit.Builder()
        retrofitBuilder1.addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        retrofitBuilder1.addConverterFactory(GsonConverterFactory.create(gson))

        retrofitBuilder2 = Retrofit.Builder()
        retrofitBuilder2.addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        retrofitBuilder2.addConverterFactory(GsonConverterFactory.create(gson))


    }

    private fun initServer1() {
        if (listener != null) {
            client.addInterceptor(DownloadInterceptor(listener!!))
        }
        retrofitBuilder1.baseUrl(host1)
        retrofitBuilder1.client(client.build())
        retrofit1 = retrofitBuilder1.build()
        server1 = retrofit1.create(NcServer::class.java)
    }

    private fun initServer2() {
        retrofitBuilder2.baseUrl(host2)
        retrofitBuilder2.client(client.build())
        retrofit2 = retrofitBuilder2.build()
        server2 = retrofit2.create(NcServer::class.java)
    }

    private fun initServer3() {
        retrofitBuilder2.baseUrl(host3)
        retrofitBuilder2.client(client.build())
        retrofit2 = retrofitBuilder2.build()
        server2 = retrofit2.create(NcServer::class.java)
    }

    /**
     * https支持
     *
     * @return
     */
    private fun getSSLSocketFactory(): SSLSocketFactory? {
        var sslSocketFactory: SSLSocketFactory? = null
        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, arrayOf(CustomTrustManager()), SecureRandom())
            sslSocketFactory = sc.socketFactory
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sslSocketFactory
    }

    private fun checkNetwork(): Boolean {
        if (!NetUtil.isConnected()) {
            return false
        }
        return true
    }

    /**
     * 获取用户信息
     */
    fun getUserInfo(
        context: Context,
        method: (model: UInfoModel) -> Unit,
        error: (code: Int, msg: String) -> Unit
    ) {
        if (!checkNetwork()) {
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT)
                .show()
            Log.d(TAG, "getUserInfo---OnFail  ${context.getString(R.string.network_error)}")
            error(0, context.getString(R.string.network_error))
            return
        }
        initServer1()
        accessToken = SPUtils.get(context, "access_token", "") as String
        server1.uInfo(accessToken)
            .subscribeOn(Schedulers.io())//被观察者
            .observeOn(Schedulers.io())
            .doOnNext {
                if (it.errno != 0) {
                    val error = ErrorCodeUtil.getMsg(it.errno)
                    Log.d(
                        TAG,
                        "getUserInfo---OnSuccess  error  type = ${error.type}   ,msg = ${error.msg}"
                    )
                    error(it.errno, error.type)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())//响应端
            .subscribeBy(
                onNext = {
                    Log.i(TAG, it.toString())
                    if (it.errno == 0) {
                        method(it)
                    } else {
                        Toast.makeText(
                            context,
                            ErrorCodeUtil.getMsg(it.errno).type,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onError = {
                    Log.i(TAG, it.toString())
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(
                        TAG,
                        "getUserInfo---OnFail  error  msg.toString() = ${it.toString()}"
                    )
                    error(0, "")
                }
            )
    }

    /**
     * 获取网盘容量
     * @param checkFree 是否检查免费信息，0为不查，1为查，默认为0
     * @param checkExpire 是否检查过期信息，0为不查，1为查，默认为0
     */
    fun getQuota(
        context: Context,
        checkFree: Int,
        checkExpire: Int,
        method: (model: QuotaModel) -> Unit,
        error: (code: Int, msg: String) -> Unit
    ) {
        if (!checkNetwork()) {
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT)
                .show()
            Log.d(TAG, "getQuota---OnFail  ${context.getString(R.string.network_error)}")
            error(0, context.getString(R.string.network_error))
            return
        }
        initServer1()
        accessToken = SPUtils.get(context, "access_token", "") as String
        server1.quota(accessToken, checkFree, checkExpire)
            .subscribeOn(Schedulers.io())//被观察者
            .observeOn(Schedulers.io())
            .doOnNext {
                if (it.errno != 0) {
                    val error = ErrorCodeUtil.getMsg(it.errno)
                    Log.d(
                        TAG,
                        "getQuota---OnSuccess  error  type = ${error.type}   ,msg = ${error.msg}"
                    )
                    error(it.errno, error.type)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())//响应端
            .subscribeBy(
                onNext = {
                    Log.i(TAG, it.toString())
                    if (it.errno == 0) {
                        method(it)
                    } else {
                        Toast.makeText(
                            context,
                            ErrorCodeUtil.getMsg(it.errno).type,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onError = {
                    Log.i(TAG, it.toString())
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "getQuota---OnFail  error  msg.toString() = ${it.toString()}")
                    error(0, "")
                }
            )
    }

    /**
     *  获取文件列表
     * @param dir 需要list的目录，以/开头的绝对路径, 默认为/
     * @param order 排序字段：默认为name ;time表示先按文件类型排序，后按修改时间排序 ;name表示先按文件类型排序，后按文件名称排序 ;size表示先按文件类型排序， 后按文件大小排序
     * @param desc 该KEY存在为降序，否则为升序 （注：排序的对象是当前目录下所有文件，不是当前分页下的文件）
     * @param start 起始位置，从0开始
     * @param limit 每页条目数，默认为1000，最大值为10000
     * @param web 值为web时， 返回dir_empty属性 和 缩略图数据
     * @param folder 是否只返回文件夹，0 返回所有，1 只返回目录条目，且属性只返回path字段。
     * @param showempty 是否返回 dir_empty 属性，0 不返回，1 返回
     */
    fun getFileList(
        context: Context,
        dir: String,
        order: String,
        desc: String,
        start: Int,
        limit: Int,
        web: String,
        folder: Int,
        showempty: Int,
        method: (list: List<FileInfoModel>) -> Unit,
        error: (code: Int, msg: String) -> Unit
    ) {
        if (!checkNetwork()) {
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT)
                .show()
            Log.d(TAG, "getFileList---OnFail  ${context.getString(R.string.network_error)}")
            error(0, context.getString(R.string.network_error))
            return
        }
        initServer1()
        accessToken = SPUtils.get(context, "access_token", "") as String
        server1.getFile(
            accessToken,
            if (dir.isNullOrBlank()) "/" else dir,
            if (order.isNullOrBlank()) "name" else order,
            if (desc.isNullOrBlank()) "" else "1",
            if (start < 0) 0 else start,
            if (limit < 0 || limit > 10000) 1000 else limit,
            if (web.isNullOrBlank() || web != "web") "" else web,
            if (folder < 0 || folder > 1) 0 else folder,
            if (showempty < 0 || showempty > 1) 0 else showempty
        )
            .subscribeOn(Schedulers.io())//被观察者
            .observeOn(Schedulers.io())
            .doOnNext {
                if (it.errno != 0) {
                    val error = ErrorCodeUtil.getMsg(it.errno)
                    Log.d(
                        TAG,
                        "getFileList---OnSuccess  error  type = ${error.type}   ,msg = ${error.msg}"
                    )
                    error(it.errno, error.type)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())//响应端
            .subscribeBy(
                onNext = {
                    Log.i(TAG, it.toString())
                    if (it.errno == 0) {
                        if (start == 0) {
                            fileList.clear()
                        }
                        fileList.addAll(it.list)
                        method(it.list)
                    } else {
                        Toast.makeText(
                            context,
                            ErrorCodeUtil.getMsg(it.errno).type,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onError = {
                    Log.i(TAG, it.toString())
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(
                        TAG,
                        "getFileList---OnFail  error  msg.toString() = ${it.toString()}"
                    )
                    error(0, "")
                }
            )
    }

    /**
     *  搜索文件
     * @param key 搜索关键字
     * @param dir 搜索目录，默认根目录
     * @param recursion 是否递归，0不递归，1递归，默认0
     * @param page 页数，从1开始，缺省则返回所有条目
     * @param num 每页条目数，默认为1000，最大值为1000
     * @param web 默认0，为1时返回缩略图信息
     */
    fun searchFile(
        context: Context,
        key: String,
        dir: String,
        recursion: String,
        page: Int,
        num: Int,
        web: Int,
        method: (list: List<FileInfoModel>) -> Unit,
        error: (code: Int, msg: String) -> Unit
    ) {
        if (!checkNetwork()) {
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT)
                .show()
            Log.d(TAG, "searchFile---OnFail  ${context.getString(R.string.network_error)}")
            error(0, context.getString(R.string.network_error))
            return
        }
        initServer1()
        accessToken = SPUtils.get(context, "access_token", "") as String
        server1.search(
            accessToken,
            if (key.isNullOrBlank()) "" else key,
            if (dir.isNullOrBlank()) "/" else dir,
            if (recursion.isNullOrBlank()) "0" else recursion,
            if (page < 1) 1 else page,
            if (num < 0 || num > 1000) 1000 else num,
            if (web < 0 || web > 1) 0 else web
        )
            .subscribeOn(Schedulers.io())//被观察者
            .observeOn(Schedulers.io())
            .doOnNext {
                if (it.errno != 0) {
                    val error = ErrorCodeUtil.getMsg(it.errno)
                    Log.d(
                        TAG,
                        "searchFile---OnSuccess  error  type = ${error.type}   ,msg = ${error.msg}"
                    )
                    error(it.errno, error.type)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())//响应端
            .subscribeBy(
                onNext = {
                    Log.i(TAG, it.toString())
                    if (it.errno == 0) {
                        if (page == 1) {
                            fileList.clear()
                        }
                        fileList.addAll(it.list)
                        method(it.list)
                    } else {
                        Toast.makeText(
                            context,
                            ErrorCodeUtil.getMsg(it.errno).type,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onError = {
                    Log.i(TAG, it.toString())
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "searchFile---OnFail  error  msg.toString() = ${it.toString()}")
                    error(0, "")
                }
            )
    }

    /**
     *  操作文件
     * @param opera 文件操作:copy, mover, rename, delete
     * @param async 0:同步， 1 自适应，2异步
     * @param filelist 文件列表,
     * copy/move:[{"path":"/测试目录/123456.docx","dest":"/测试目录/abc","newname":"11223.docx","ondup":"fail"}]
     * rename:[{path":"/测试目录/123456.docx","newname":test.docx"}]
     * delete:["/测试目录/123456.docx"]
     * @param ondup 全局ondup,遇到重复文件的处理策略,
     * fail(默认，直接返回失败)、newcopy(重命名文件)、overwrite、skip
     */
    fun operaFile(
        context: Context,
        opera: String,
        async: Int,
        filelist: String,
        ondup: String,
        method: (model: OperaFileModel) -> Unit,
        error: (code: Int, msg: String) -> Unit,
        reMethod: (model: OperaFileModel) -> Unit
    ) {
        if (!checkNetwork()) {
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT)
                .show()
            Log.d(TAG, "operaFile---OnFail  ${context.getString(R.string.network_error)}")
            error(0, context.getString(R.string.network_error))
            return
        }
        initServer1()
        accessToken = SPUtils.get(context, "access_token", "") as String
        val map: MutableMap<String, RequestBody> = HashMap()
        val requestAsync = RequestBody.create(
            MediaType.parse(dataParse),
            "${if (async < 0 || async > 2) 0 else async}"
        )
        val requestFilelist = RequestBody.create(
            MediaType.parse(dataParse),
            filelist
        )
        val requestOndup = RequestBody.create(
            MediaType.parse(dataParse),
            if (ondup.isNullOrBlank()) "fail" else ondup
        )
        map["async"] = requestAsync
        map["filelist"] = requestFilelist
        map["ondup"] = requestOndup
//        val body = RequestBody.create(MediaType.parse(jsonParse), jsonObject.toString())
        server1.operaFile(accessToken, opera, map)
            .subscribeOn(Schedulers.io())//被观察者
            .observeOn(AndroidSchedulers.mainThread())//响应端
            .subscribeBy(
                onNext = {
                    Log.i(TAG, it.toString())
                    if (it.errno == 0) {
                        method(it)
                    } else {
                        if (it.errno == 12) {
                            reMethod(it)
                        } else {
                            val error = ErrorCodeUtil.getMsg(it.errno)
                            Log.d(
                                TAG,
                                "operaFile---OnSuccess  error  type = ${error.type}   ,msg = ${error.msg}"
                            )
                            error(it.errno, error.type)
                            Toast.makeText(
                                context,
                                error.type,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onError = {
                    Log.i(TAG, it.toString())
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "operaFile---OnFail  error  msg.toString() = ${it.toString()}")
                    error(0, "")
                }
            )
    }

    /**
     * 创建文件夹
     * @param path 创建目录路径
     */
    fun createDir(
        context: Context,
        path: String,
        method: (model: CreateDirModel) -> Unit,
        error: (code: Int, msg: String) -> Unit
    ) {
        if (!checkNetwork()) {
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT)
                .show()
            Log.d(TAG, "createDir---OnFail  ${context.getString(R.string.network_error)}")
            error(0, context.getString(R.string.network_error))
            return
        }
        initServer1()
        accessToken = SPUtils.get(context, "access_token", "") as String
        val dir = "$currentlyPath/$path"
//        val jsonObject = JSONObject()
//        jsonObject.put("path", dir)
//        jsonObject.put("isdir", 1)
//        val body = RequestBody.create(MediaType.parse(path), jsonObject.toString())
        val map: MutableMap<String, RequestBody> = HashMap()
        val requestPath = RequestBody.create(
            MediaType.parse(dataParse),
            dir
        )
        val requestIsdir = RequestBody.create(
            MediaType.parse(dataParse),
            "${1}"
        )
        map["path"] = requestPath
        map["isdir"] = requestIsdir
        server1.createDir(accessToken, map)
            .subscribeOn(Schedulers.io())//被观察者
            .observeOn(Schedulers.io())
            .doOnNext {
                if (it.errno != 0) {
                    val error = ErrorCodeUtil.getMsg(it.errno)
                    Log.d(
                        TAG,
                        "createDir---OnSuccess  error  type = ${error.type}   ,msg = ${error.msg}"
                    )
                    error(it.errno, error.type)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())//响应端
            .subscribeBy(
                onNext = {
                    Log.i(TAG, it.toString())
                    if (it.errno == 0) {
                        Toast.makeText(context, "成功创建文件夹：${it.name}", Toast.LENGTH_SHORT).show()
                        method(it)
                    } else {
                        Toast.makeText(
                            context,
                            ErrorCodeUtil.getMsg(it.errno).type,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onError = {
                    Log.i(TAG, it.toString())
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "createDir---OnFail  error  msg.toString() = ${it.toString()}")
                    error(0, "")
                }
            )
    }

    /**
     *  预上传
     * @param fileName 上传后使用的文件绝对路径，需要urlencode
     * @param size 文件或目录的大小，单位B，目录的话大小为0
     * @param isdir 是否目录，0 文件、1 目录
     * @param rtype 文件命名策略，默认0 ;0 为不重命名，返回冲突; 1 为只要path冲突即重命名; 2 为path冲突且block_list不同才重命名; 3 为覆盖
     * @param uploadid 上传id
     * @param block_list 文件各分片MD5数组的json串
     * @param content_md5 文件MD5
     * @param slice_md5 文件校验段的MD5，校验段对应文件前256KB
     */
    fun precreate(
        context: Context,
        path: String,
        size: String,
        isdir: String,
        rtype: Int,
        uploadid: String,
        block_list: String,
        content_md5: String,
        slice_md5: String,
        method: (model: PrecreateModel) -> Unit,
        error: (code: Int, msg: String) -> Unit
    ) {
        if (!checkNetwork()) {
            EventBus.getDefault().post(NetdiscEvent(3))
            Log.d(TAG, "precreate---OnFail  ${context.getString(R.string.network_error)}")
            error(0, context.getString(R.string.network_error))
            return
        }
        initServer1()
        accessToken = SPUtils.get(context, "access_token", "") as String
        val map: MutableMap<String, RequestBody> = HashMap()
        val requestPath = RequestBody.create(
            MediaType.parse(dataParse),
            path
        )
        map["path"] = requestPath
        val requestSize = RequestBody.create(
            MediaType.parse(dataParse),
            size
        )
        map["size"] = requestSize
        val requestIsdir = RequestBody.create(
            MediaType.parse(dataParse),
            isdir
        )
        map["isdir"] = requestIsdir
        val requestAutoinit = RequestBody.create(
            MediaType.parse(dataParse),
            "${1}"
        )
        map["autoinit"] = requestAutoinit
        val requestRtype = RequestBody.create(
            MediaType.parse(dataParse),
            "${if (rtype < 0 || rtype > 3) 0 else rtype}"
        )
        map["rtype"] = requestRtype
        if (!uploadid.isNullOrBlank()) {
            val requestUploadid = RequestBody.create(
                MediaType.parse(dataParse),
                uploadid
            )
            map["uploadid"] = requestUploadid
        }
        val requestBlock_list = RequestBody.create(
            MediaType.parse(dataParse),
            block_list
        )
        map["block_list"] = requestBlock_list
        if (!content_md5.isNullOrBlank()) {
            val requestContent_md5 = RequestBody.create(
                MediaType.parse(dataParse),
                content_md5
            )
            map["content_md5"] = requestContent_md5
        }
        if (!slice_md5.isNullOrBlank()) {
            val requestSlice_md5 = RequestBody.create(
                MediaType.parse(dataParse),
                slice_md5
            )
            map["slice_md5"] = requestSlice_md5
        }
        val requestLocal_ctime = RequestBody.create(
            MediaType.parse(dataParse),
            "${System.currentTimeMillis() / 1000}"
        )
        map["local_ctime"] = requestLocal_ctime
        val requestLocal_mtime = RequestBody.create(
            MediaType.parse(dataParse),
            "${System.currentTimeMillis() / 1000}"
        )
        map["local_mtime"] = requestLocal_mtime

        server1.precreate(accessToken, map)
            .subscribeOn(Schedulers.io())//被观察者
            .observeOn(Schedulers.io())
            .doOnNext {
                if (it.errno != 0) {
                    val error = ErrorCodeUtil.getMsg(it.errno)
                    Log.d(
                        TAG,
                        "precreate---OnSuccess  error  type = ${error.type}   ,msg = ${error.msg}"
                    )
                    error(it.errno, error.type)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())//响应端
            .subscribeBy(
                onNext = {
                    Log.i(TAG, it.toString())
                    if (it.errno == 0) {
                        method(it)
                    } else {
                        Toast.makeText(
                            context,
                            ErrorCodeUtil.getMsg(it.errno).type,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onError = {
                    Log.i(TAG, it.toString())
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "precreate---OnFail  error  msg.toString() = ${it.toString()}")
                    error(0, "")
                }
            )
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
        file: File,
        method: (model: UploadModel) -> Unit,
        error: (code: Int, msg: String) -> Unit
    ) {
        if (!checkNetwork()) {
            EventBus.getDefault().post(NetdiscEvent(3))
            Log.d(TAG, "upload---OnFail  ${context.getString(R.string.network_error)}")
            error(0, context.getString(R.string.network_error))
            return
        }
        initServer2()
        accessToken = SPUtils.get(context, "access_token", "") as String
        val map: MutableMap<String, RequestBody> = HashMap()
        val requestFile = RequestBody.create(
            MediaType.parse(dataParse),
            file
        )
        map["file"] = requestFile
        val createFormData = MultipartBody.Part.createFormData("file", path, requestFile)
        if (path.isNullOrEmpty()) {
            EventBus.getDefault().post(NetdiscEvent(4))
            Log.d(TAG, "upload --- path = $path")
            return
        }
        server2.upload(
            accessToken,
            "tmpfile",
            path,
            uploadid,
            partseq,
            createFormData
        )
            .subscribeOn(Schedulers.io())//被观察者
            .observeOn(Schedulers.io())
            .doOnNext {
                if (it.errno != 0) {
                    val error = ErrorCodeUtil.getMsg(it.errno)
                    Log.d(
                        TAG,
                        "upload---OnSuccess  error  type = ${error.type}   ,msg = ${error.msg}"
                    )
                    error(it.errno, error.type)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())//响应端
            .subscribeBy(
                onNext = {
                    Log.i(TAG, it.toString())
                    if (it.errno == 0) {
                        method(it)
                    } else {
                        Toast.makeText(
                            context,
                            ErrorCodeUtil.getMsg(it.errno).type,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onError = {
                    Log.i(TAG, it.toString())
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "upload---OnFail  error  msg.toString() = ${it.toString()}")
                    error(0, "")
                }
            )
    }

    /**
     *  创建文件
     * @param path 上传后使用的文件绝对路径
     * @param size 文件或目录的大小，必须要和文件真实大小保持一致
     * @param isdir 是否目录，0 文件、1 目录
     * @param rtype 文件命名策略，默认1;0 为不重命名，返回冲突;1 为只要path冲突即重命名;2 为path冲突且block_list不同才重命名;3 为覆盖
     * @param uploadid uploadid， 非空表示通过superfile2上传
     * @param block_list 文件各分片MD5的json串;MD5对应superfile2返回的md5，且要按照序号顺序排列
     * @param zip_quality 图片压缩程度，有效值50、70、100
     * @param zip_sign 未压缩原始图片文件真实md5
     * @param is_revision 是否需要多版本支持  1为支持，0为不支持， 默认为0 (带此参数会忽略重命名策略)
     * @param mode 上传方式  1 手动、2 批量上传、3 文件自动备份  4 相册自动备份、5 视频自动备份
     * @param exif_info json字符串，orientation、width、height、recovery为必传字段，其他字段如果没有可以不传
     */
    fun create(
        context: Context,
        path: String,
        size: String,
        isdir: String,
        rtype: Int,
        uploadid: String,
        block_list: String,
        zip_quality: String,
        zip_sign: String,
        is_revision: Int,
        mode: Int,
        exif_info: String,
        method: (model: CreateFileModel) -> Unit,
        error: (code: Int, msg: String) -> Unit
    ) {
        if (!checkNetwork()) {
            EventBus.getDefault().post(NetdiscEvent(3))
            Log.d(TAG, "create---OnFail  ${context.getString(R.string.network_error)}")
            error(0, context.getString(R.string.network_error))
            return
        }
        initServer1()
        accessToken = SPUtils.get(context, "access_token", "") as String
        val map: MutableMap<String, RequestBody> = HashMap()
        val requestPath = RequestBody.create(
            MediaType.parse(dataParse),
            path
        )
        map["path"] = requestPath

        val requestSize = RequestBody.create(
            MediaType.parse(dataParse),
            size
        )
        map["size"] = requestSize
        val requestIsdir = RequestBody.create(
            MediaType.parse(dataParse),
            isdir
        )
        map["isdir"] = requestIsdir
        if (rtype in 0..3) {
            val requestRtype = RequestBody.create(
                MediaType.parse(dataParse),
                "$rtype"
            )
            map["rtype"] = requestRtype
        }
        if (!uploadid.isNullOrBlank()) {
            val requestUploadid = RequestBody.create(
                MediaType.parse(dataParse),
                uploadid
            )
            map["uploadid"] = requestUploadid
        }
        if (!block_list.isNullOrBlank()) {
            val requestBlock_list = RequestBody.create(
                MediaType.parse(dataParse),
                block_list
            )
            map["block_list"] = requestBlock_list
        }
        if (!zip_quality.isNullOrBlank()) {
            val requestZip_quality = RequestBody.create(
                MediaType.parse(dataParse),
                zip_quality
            )
            map["zip_quality"] = requestZip_quality
        }
        if (!zip_sign.isNullOrBlank()) {
            val requestZip_sign = RequestBody.create(
                MediaType.parse(dataParse),
                zip_sign
            )
            map["zip_sign"] = requestZip_sign
        }
        if (is_revision in 0..1) {
            val requestIs_revision = RequestBody.create(
                MediaType.parse(dataParse),
                "$is_revision"
            )
            map["is_revision"] = requestIs_revision
        }
        if (mode in 1..5) {
            val requestMode = RequestBody.create(
                MediaType.parse(dataParse),
                "$mode"
            )
            map["mode"] = requestMode
        }
        if (!exif_info.isNullOrBlank()) {
            val requestExif_info = RequestBody.create(
                MediaType.parse(dataParse),
                exif_info
            )
            map["exif_info"] = requestExif_info
        }

        val requestLocal_ctime = RequestBody.create(
            MediaType.parse(dataParse),
            "${System.currentTimeMillis() / 1000}"
        )
        map["local_ctime"] = requestLocal_ctime
        val requestLocal_mtime = RequestBody.create(
            MediaType.parse(dataParse),
            "${System.currentTimeMillis() / 1000}"
        )
        map["local_mtime"] = requestLocal_mtime

        server1.create(accessToken, map)
            .subscribeOn(Schedulers.io())//被观察者
            .observeOn(Schedulers.io())
            .doOnNext {
                if (it.errno != 0) {
                    val error = ErrorCodeUtil.getMsg(it.errno)
                    Log.d(
                        TAG,
                        "create---OnSuccess  error  type = ${error.type}   ,msg = ${error.msg}"
                    )
                    error(it.errno, error.type)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())//响应端
            .subscribeBy(
                onNext = {
                    Log.i(TAG, it.toString())
                    if (it.errno == 0) {
                        method(it)
                    } else {
                        Toast.makeText(
                            context,
                            ErrorCodeUtil.getMsg(it.errno).type,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onError = {
                    Log.i(TAG, it.toString())
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "create---OnFail  error  msg.toString() = ${it.toString()}")
                    error(0, "")
                }
            )
    }

    /**
     * 获取文件信息，下载链接
     * @param fsids 文件id数组，数组中元素是uint64类型，数组大小上限是：100
     * @param dlink 是否需要下载地址，0为否，1为是，默认为0
     */
    fun filemetas(
        context: Context,
        fsids: LongArray,
        dlink: Int,
        method: (model: List<FilemetasInfoModel>) -> Unit,
        error: (code: Int, msg: String) -> Unit
    ) {
        if (!checkNetwork()) {
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT)
                .show()
            Log.d(TAG, "filemetas---OnFail  ${context.getString(R.string.network_error)}")
            error(0, context.getString(R.string.network_error))
            return
        }
        initServer1()
        accessToken = SPUtils.get(context, "access_token", "") as String
        server1.filemetas(accessToken, fsids.contentToString(), if (dlink in 0..1) dlink else 0)
            .subscribeOn(Schedulers.io())//被观察者
            .observeOn(Schedulers.io())
            .doOnNext {
                if (it.errno != 0) {
                    val error = ErrorCodeUtil.getMsg(it.errno)
                    Log.d(
                        TAG,
                        "filemetas---OnSuccess  error  type = ${error.type}   ,msg = ${error.msg}"
                    )
                    error(it.errno, error.type)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())//响应端
            .subscribeBy(
                onNext = {
                    Log.i(TAG, it.toString())
                    if (it.errno == 0) {
                        method(it.list)
                    } else {
                        Toast.makeText(
                            context,
                            ErrorCodeUtil.getMsg(it.errno).type,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onError = {
                    Log.i(TAG, it.toString())
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "filemetas---OnFail  error  msg.toString() = ${it.toString()}")
                    error(0, "")
                }
            )
    }

    /**
     * @param dlink 文件下载链接
     * @param path 文件保存路径
     */
    fun download(
        context: Context,
        dlink: String,
        path: String,
        write: (inputStream: InputStream, path: String) -> Unit,
        error: (code: Int, msg: String) -> Unit
    ) {
        if (listener == null) {
            Log.d(TAG, "download---OnFail  ${context.getString(R.string.network_error)}")
            return
        }
        if (!checkNetwork()) {
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT)
                .show()
            Log.d(TAG, "download---OnFail  ${context.getString(R.string.network_error)}")
            listener?.onFail(context.getString(R.string.network_error))
            return
        }
        initServer1()
        listener?.onStartDownload()
        accessToken = SPUtils.get(context, "access_token", "") as String
        downloadBy = server1.download(dlink, accessToken)
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .map { responseBody -> responseBody.byteStream() }
            .observeOn(Schedulers.computation()) // 用于计算任务
            .doOnNext { inputStream -> write(inputStream, path) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { },
                onError = {
                    Log.i(TAG, it.toString())
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "download---OnFail  error  msg.toString() = ${it.toString()}")
                    error(0, "")
                })
    }

    /**
     * 取消第三方应用授权
     */
    fun revokeAuthorization(
        context: Context,
        method: (model: RevokeAuthorizationModel) -> Unit,
        error: (code: Int, msg: String) -> Unit
    ) {
        if (!checkNetwork()) {
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT)
                .show()
            Log.d(
                TAG,
                "revokeAuthorization---OnFail  ${context.getString(R.string.network_error)}"
            )
            error(0, context.getString(R.string.network_error))
            return
        }
        initServer3()
        accessToken = SPUtils.get(context, "access_token", "") as String
        val uid = SPUtils.get(context, "uk", 0L) as Long
        val jsonObject = JsonObject()
        jsonObject.addProperty("uid", uid)
        val body = RequestBody.create(MediaType.parse(jsonParse), jsonObject.toString())
        server2.revokeAuthorization(accessToken, body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    method(it)
                },
                onError = {
                    Log.i(TAG, it.toString())
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(
                        TAG,
                        "revokeAuthorization---OnFail  error  msg.toString() = ${it.toString()}"
                    )
                    error(0, "")
                }
            )
    }
}