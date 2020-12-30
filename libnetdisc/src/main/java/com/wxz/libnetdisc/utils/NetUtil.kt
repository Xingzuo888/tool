package com.wxz.libnetdisc.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager

@SuppressLint("MissingPermission")
object NetUtil {
    const val NETWORK_UNKNOW = -2
    const val NETWORK_NONE = -1
    const val NETWORK_MOBILE = 0
    const val NETWORK_WIFI = 1
    const val NETWORK_ETHERNET = 9

    const val TAG = "net"


    fun isConnected(): Boolean {
        val connectivityManager =
            App.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        LogUtils.i(TAG, "isConnected:${activeNetworkInfo != null && activeNetworkInfo.isConnected}")
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun getNetWorkState(): Int {
        //得到连接管理器对象
        val connectivityManager =
            App.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        //如果网络连接，判断该网络类型
        return if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            when (activeNetworkInfo.type) {
                ConnectivityManager.TYPE_ETHERNET -> NETWORK_ETHERNET
                ConnectivityManager.TYPE_WIFI -> NETWORK_WIFI
                ConnectivityManager.TYPE_MOBILE -> NETWORK_MOBILE
                else -> NETWORK_UNKNOW
            }
        } else {
            //网络异常
            NETWORK_NONE
        }

    }
}