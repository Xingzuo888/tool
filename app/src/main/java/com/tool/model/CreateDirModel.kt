package com.tool.model

/**
 *    Author : wxz
 *    Time   : 2020/10/30
 *    Desc   :
 */
data class CreateDirModel(
    val category: Int,
    val ctime: Int,
    val errno: Int,
    val fs_id: Long,
    val isdir: Int,
    val mtime: Int,
    val name: String,
    val path: String,
    val status: Int
)
data class CreateFileModel(
    val category: Int,
    val ctime: Int,
    val errno: Int,
    val fs_id: Long,
    val isdir: Int,
    val md5: String,
    val mtime: Int,
    val name: String,
    val path: String,
    val server_filename: String,
    val size: Int
)