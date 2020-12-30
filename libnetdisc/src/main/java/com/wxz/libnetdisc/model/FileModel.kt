package com.wxz.libnetdisc.model

/**
 *    Author : wxz
 *    Time   : 2020/10/27
 *    Desc   :
 */
data class FileModel(
    val errno: Int,
    val guid: Int,
    val guid_info: String,
    val list: List<FileInfoModel>,
    val request_id: Long
)

data class FileInfoModel(
    val category: Int, //文件类型，1 视频、2 音频、3 图片、4 文档、5 应用、6 其他、7 种子
    val fs_id: Long, //文件在云端的唯一标识ID
    val isdir: Int, //是否目录，0 文件、1 目录
    val local_ctime: Long, //文件在客户端创建时间
    val local_mtime: Long, //文件在客户端修改时间
    val md5: String, //文件的md5值，只有是文件类型时，该KEY才存在
    val oper_id: Long,
    val path: String, //文件的绝对路径
    val privacy: Int,
    val server_atime: Long,
    val server_ctime: Long, //文件在服务器创建时间
    val server_filename: String, //文件名称
    val server_mtime: Long, //文件在服务器修改时间
    val share: Int,
    val size: Long, //文件大小，单位B
    val unlist: Int,
    val thumbs: FileThumbs, //只有请求参数带WEB且该条目分类为图片时，该KEY才存在，包含三个尺寸的缩略图URL
    val dir_empty: Int, //该目录是否存在子目录， 只有请求参数带WEB且该条目为目录时，该KEY才存在， 0为存在， 1为不存在
    val wpfile: Int,
    val empty: Int,
    val docpreview: String,
    val lodocpreview: String,
    val delete_type: Int,
    val extent_tinyint1: Int,
    var isChoice: Boolean = false,
    var isRecover: Boolean = false
)

data class FileThumbs(
    val icon: String,
    val url1: String,
    val url2: String,
    val url3: String
)