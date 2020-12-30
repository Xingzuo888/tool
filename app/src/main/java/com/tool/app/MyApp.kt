package com.tool.app

import android.app.Application
import android.content.Context
import android.util.Log.DEBUG
import android.util.Log.INFO
import androidx.work.Configuration
import com.nextclass.ai.nxhomework.BuildConfig
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

/**
 *    Author : wxz
 *    Time   : 2020/12/14
 *    Desc   :
 */
class MyApp : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        val formatStrategy = PrettyFormatStrategy.newBuilder()
//            .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
//            .methodCount(2)         // (Optional) How many method line to show. Default 2
//            .methodOffset(5)        // (Optional) Hides internal method calls up to offset. Default 5
//            .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
            .tag("nxhomework:")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
            .build()
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
        Logger.i("MyApp on create")

    }

    companion object {
        const val TAG = "MyApp"
        lateinit var context: Context
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return if (BuildConfig.DEBUG) {
            Configuration.Builder().setMinimumLoggingLevel(DEBUG).build()
        } else {
            Configuration.Builder().setMinimumLoggingLevel(INFO).build()
        }
    }
}