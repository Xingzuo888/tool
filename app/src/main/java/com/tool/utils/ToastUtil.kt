package com.tool.utils

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import com.tool.app.MyApp

/**
 *    Author : wxz
 *    Time   : 2020/12/14
 *    Desc   :
 */
object ToastUtil {
    fun showForce(stringId: Int) {
        try {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(MyApp.context, MyApp.context.getString(stringId), Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showForce(string: String?) {
        if (TextUtils.isEmpty(string)) return
        try {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(MyApp.context, string, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}