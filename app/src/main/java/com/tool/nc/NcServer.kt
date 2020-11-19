package com.tool.nc

import com.tool.model.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 *    Author : wxz
 *    Time   : 2020/10/26
 *    Desc   :
 */
interface NcServer {


    @GET("rest/2.0/xpan/nas?method=uinfo")
    fun uInfo(@Query("access_token") accessToken: String): Observable<UInfoModel>

    @GET("api/quota")
    fun quota(
        @Query("access_token") accessToken: String,
        @Query("checkfree") checkFree: Int,
        @Query("checkexpire") checkExpire: Int
    ): Observable<QuotaModel>


    @GET("rest/2.0/xpan/file?method=list")
    fun getFile(
        @Query("access_token") accessToken: String,
        @Query("dir") dir: String,
        @Query("order") order: String,
        @Query("desc") desc: String,
        @Query("start") start: Int,
        @Query("limit") limit: Int,
        @Query("web") web: String,
        @Query("folder") folder: Int,
        @Query("showempty") showempty: Int
    ): Observable<FileModel>

    @GET("rest/2.0/xpan/file?method=search")
    fun search(
        @Query("access_token") accessToken: String,
        @Query("key") key: String,
        @Query("dir") dir: String,
        @Query("recursion") recursion: String,
        @Query("page") page: Int,
        @Query("num") num: Int,
        @Query("web") web: Int
    ): Observable<SearchModel>

    @Multipart
    @POST("rest/2.0/xpan/file?method=filemanager")
//    @Headers("User-Agent:com.nextclass.ai.folder","Accept-Language:zh-CN,zh;q=0.9","accept:application/json; */*")
    fun operaFile(
        @Query("access_token") accessToken: String,
        @Query("opera") opera: String,
//        @Body body: RequestBody
        @PartMap body: MutableMap<String, RequestBody>
    ): Observable<OperaFileModel>

    @Multipart
    @POST("api/create")
    fun createDir(
        @Query("access_token") accessToken: String,
        @PartMap body: MutableMap<String, RequestBody>
    ): Observable<CreateDirModel>

    @Multipart
    @POST("rest/2.0/xpan/file?method=precreate")
    fun precreate(
        @Query("access_token") accessToken: String,
        @PartMap body: MutableMap<String, RequestBody>
    ): Observable<PrecreateModel>

    @Multipart
    @POST("rest/2.0/pcs/superfile2?method=upload")
    @Headers("accept: */*")
    fun upload(
        @Query("access_token") accessToken: String,
        @Query("type") type: String,
        @Query("path") path: String,
        @Query("uploadid") uploadid: String,
        @Query("partseq") partseq: Int,
        @Part file:MultipartBody.Part
    ): Observable<UploadModel>

    @Multipart
    @POST("rest/2.0/xpan/file?method=create")
    fun create(
        @Query("access_token") accessToken: String,
        @PartMap body: MutableMap<String, RequestBody>
    ): Observable<CreateFileModel>

    @GET("rest/2.0/xpan/multimedia?method=filemetas")
    fun filemetas(
        @Query("access_token") accessToken: String,
        @Query("fsids") fsids: String,
        @Query("dlink") dlink: Int
    ): Observable<FilemetasModel>

    @Streaming
    @GET
    @Headers("User-Agent:com.nextclass.ai.folder")
    fun download(
        @Url dlink: String,
        @Query("access_token") accessToken: String
    ): Observable<ResponseBody>

    @POST("rest/2.0/passport/auth/revokeAuthorization")
    fun revokeAuthorization(
        @Query("access_token") access_token:String,
        @Body body:RequestBody
    ):Observable<RevokeAuthorizationModel>
}