package com.wxz.libnetdisc.model

/**
 *    Author : wxz
 *    Time   : 2020/10/30
 *    Desc   :
 */
data class OperaFileModel(
    val errno: Int,
    val info: List<Info>, //文件信息
    val request_id: Long,
    val taskid: Long //异步任务id, async=2时返回
)

data class Info(
    val errno: Int,
    val path: String
)