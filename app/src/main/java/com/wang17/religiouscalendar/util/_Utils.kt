package com.wang17.religiouscalendar.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Handler
import android.support.v7.app.AlertDialog
import com.wang17.religiouscalendar.model.DataContext
import com.wang17.religiouscalendar.model.DateTime
import com.wang17.religiouscalendar.model.RunLog

/**
 * Created by 阿弥陀佛 on 2016/10/2.
 */
object _Utils {
    /**
     * 行房节欲期
     *
     * @param birthday
     * @return
     */
    fun getTargetInMillis(birthday: DateTime): Long {
        val now = DateTime()
        var age = now.getYear() - birthday.getYear() + 1
        if (now.getMonth() < birthday.getMonth()) {
            age -= 1
        }
        var day = 100.0
        if (age >= 16 && age < 18) {
            day = 3.0
        } else if (age >= 18 && age < 20) {
            day = 3.0
        } else if (age >= 20 && age < 30) {
            day = 4 + (age - 20) * 0.4
        } else if (age >= 30 && age < 40) {
            day = 8 + (age - 30) * 0.8
        } else if (age >= 40 && age < 50) {
            day = 16 + (age - 40) * 0.5
        } else if (age >= 50 && age < 60) {
            day = 21 + (age - 50) * 0.9
        }
        return (day * 24).toInt().toLong() * 60 * 60000
    }

    fun getTargetInHour(birthday: DateTime): Int {
        return (getTargetInMillis(birthday) / 3600000).toInt()
    }

    fun printException(context: Context, e: Exception) {
        if (e.stackTrace.size == 0) return
        var msg = ""
        for (ste in e.stackTrace) {
            if (ste.className.contains(context.packageName)) {
                msg += """
                    类名：
                    ${ste.className}
                    方法名：
                    ${ste.methodName}
                    行号：${ste.lineNumber}
                    错误信息：
                    ${e.message}
                    
                    """.trimIndent()
            }
        }
        try {
            AlertDialog.Builder(context).setMessage(msg).setPositiveButton("知道了", null).show()
        } catch (e1: Exception) {
        }
        addRunLog(context, "err","运行错误", msg)
        e.printStackTrace()
    }

    fun addRunLog(context: Context,tag:String, item: String, message: String) {
        DataContext(context).addRunLog(RunLog(tag, item, message))
    }

    fun printExceptionSycn(context: Context, handler: Handler, e: Exception) {
        try {
            if (e.stackTrace.size == 0) return
            for (ste in e.stackTrace) {
                if (ste.className.contains(context.packageName)) {
                    val msg = """
                        类名：
                        ${ste.className}
                        方法名：
                        ${ste.methodName}
                        行号：${ste.lineNumber}
                        错误信息：
                        ${e.message}
                        """.trimIndent()
                    handler.post { AlertDialog.Builder(context).setTitle("运行错误").setMessage(msg).setPositiveButton("知道了", null).show() }
                    break
                }
            }
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
    }

    /**
     * 判断WIFI是否可用
     *
     * @param context
     * @return
     */
    fun isWiFiActive(context: Context): Boolean {
        val connectivity = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val info = connectivity.activeNetworkInfo
            if (info!!.typeName == "WIFI" && info.isConnected) {
                return true
            }
        }
        return false
    }

    /**
     * 判断网络是否可用
     *
     * @param context
     * @return
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.activeNetworkInfo
        return info != null && info.isConnected
    }

    /**
     * 判断当前是否获得了某项权限
     *
     * @param context       例：MainActivity.this
     * @param permissionStr 例：android.permission.ACCESS_NETWORK_STATE
     * @return
     */
    fun havePermission(context: Context, permissionStr: String?): Boolean {
        val pm = context.packageManager
        return PackageManager.PERMISSION_GRANTED == pm.checkPermission(permissionStr!!, context.packageName)
    }
}