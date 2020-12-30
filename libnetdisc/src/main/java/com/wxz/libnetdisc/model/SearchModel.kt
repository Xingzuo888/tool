package com.wxz.libnetdisc.model

/**
 *    Author : wxz
 *    Time   : 2020/10/29
 *    Desc   :
 */
data class SearchModel(
    val contentlist: List<Any>,
    val errno: Int,
    val has_more: Int,
    val list: List<FileInfoModel>,
    val request_id: Long
)
