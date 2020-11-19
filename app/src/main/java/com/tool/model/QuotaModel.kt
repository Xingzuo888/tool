package com.tool.model

/**
 *    Author : wxz
 *    Time   : 2020/10/27
 *    Desc   :
 */
data class QuotaModel(
    val errno: Int,
    val expire: Boolean, //7天内是否有容量到期
    val free: Long, //剩余大小，单位B
    val request_id: Long,
    val total: Long, //总空间大小，单位B
    val used: Long //已使用大小，单位B
)