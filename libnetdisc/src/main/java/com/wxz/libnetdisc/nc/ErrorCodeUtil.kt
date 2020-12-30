package com.wxz.libnetdisc.nc

/**
 *    Author : wxz
 *    Time   : 2020/10/28
 *    Desc   :
 */
object ErrorCodeUtil {
    fun getMsg(code: Int): ErrorInfo {
        return when (code) {
            -10 -> {
                ErrorInfo("云端容量已满")
            }
            -9 -> {
                ErrorInfo("文件或目录不存在")
            }
            -8 -> {
                ErrorInfo("文件或目录已存在")
            }
            -7 -> {
                ErrorInfo("文件或目录名错误或无权访问")
            }
            -6 -> {
                ErrorInfo("身份验证失败", "access_token 是否有效;部分接口需要申请对应的网盘权限")
            }
            0 -> {
                ErrorInfo("请求成功")
            }
            2 -> {
                ErrorInfo("参数错误", "检查必填字段；get/post 参数位置")
            }
            10 -> {
                ErrorInfo("创建文件的superfile失败")
            }
            12 -> {
                ErrorInfo("批量操作失败")
            }
            9100 -> {
                ErrorInfo("一级封禁")
            }
            9200 -> {
                ErrorInfo("二级封禁")
            }
            9300 -> {
                ErrorInfo("三级封禁")
            }
            9400 -> {
                ErrorInfo("四级封禁")
            }
            9500 -> {
                ErrorInfo("五级封禁")
            }
            31024 -> {
                ErrorInfo("没有申请上传权限")
            }
            31034 -> {
                ErrorInfo("命中接口频控", "核对频控规则;稍后再试;申请单独频控规则")
            }
            31066 -> {
                ErrorInfo("文件不存在")
            }
            31076 -> {
                ErrorInfo("")
            }
            31208 -> {
                ErrorInfo("form data format invalid")
            }
            31299 -> {
                ErrorInfo("第一个分片的大小小于4MB")
            }
            31364 -> {
                ErrorInfo("超出分片大小限制")
            }
            42000 -> {
                ErrorInfo("访问过于频繁")
            }
            42001 -> {
                ErrorInfo("rand校验失败")
            }
            42211 -> {
                ErrorInfo("图片详细信息查询失败")
            }
            42212 -> {
                ErrorInfo("共享目录文件上传者信息查询失败，可重试")
            }
            42213 -> {
                ErrorInfo("共享目录鉴权失败")
            }
            42214 -> {
                ErrorInfo("文件基础信息查询失败")
            }
            42905 -> {
                ErrorInfo("查询用户名失败，可重试")
            }
            42999 -> {
                ErrorInfo("功能下线")
            }
            else -> ErrorInfo("")
        }
    }
}

data class ErrorInfo(val type: String, val msg: String = "")