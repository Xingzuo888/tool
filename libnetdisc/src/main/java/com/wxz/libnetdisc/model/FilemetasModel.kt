package com.wxz.libnetdisc.model

/**
 *    Author : wxz
 *    Time   : 2020/11/06
 *    Desc   :
 */
data class FilemetasModel(
    val errmsg: String,
    val errno: Int,
    val list: List<FilemetasInfoModel>, //文件信息列表
    val names: Names, //如果查询共享目录，该字段为共享目录文件上传者的uk和账户名称
    val request_id: String
)

data class FilemetasInfoModel(
    val category: Int,//文件类型
    val date_taken: Int,//图片拍摄时间
    val dlink: String,//文件下载地址
    val filename: String,//文件名
    val fs_id: Long,
    val height: Int,//图片高度
    val isdir: Int,//是否是目录
    val md5: String,
    val oper_id: Long,
    val path: String,
    val server_ctime: Long,//文件的服务器创建时间
    val server_mtime: Long,//文件的服务修改时间
    val size: Long,//文件大小
    val thumbs: Thumbs,//缩略图地址
    val width: Int//图片宽度
)

class Names(
)

data class Thumbs(
    val icon: String,
    val url1: String,
    val url2: String,
    val url3: String
)