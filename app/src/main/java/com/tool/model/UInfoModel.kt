package com.tool.model

/**
 *    Author : wxz
 *    Time   : 2020/10/27
 *    Desc   :
 */
data class UInfoModel(
    val avatar_url: String, //头像地址
    val baidu_name: String, //百度账号
    val errmsg: String,
    val errno: Int,
    val netdisk_name: String, //网盘账号
    val request_id: String,
    val uk: Long, //用户ID
    val vip_type: Int //会员类型，0普通用户、1普通会员、2超级会员
)