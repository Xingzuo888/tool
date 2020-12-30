package com.wxz.libnetdisc.model

/**
 *    Author : wxz
 *    Time   : 2020/11/05
 *    Desc   :
 */
data class UploadModel(
    val errno: Int, //错误码
    val md5: String,
    val request_id: Long
)