package com.wxz.libnetdisc.model

/**
 *    Author : wxz
 *    Time   : 2020/11/05
 *    Desc   :
 */
data class PrecreateModel(
    val block_list: List<Int>, //需要上传的分片序号，索引从0开始
    val errno: Int, //错误码
    val path: String, //文件的绝对路径
    val request_id: Long,
    val return_type: Int, //返回类型，1 文件在云端不存在、2 文件在云端已存在
    val uploadid: String //上传id
)