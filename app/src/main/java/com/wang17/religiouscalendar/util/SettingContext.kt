package com.wang17.religiouscalendar.util

import android.content.Context
import com.wang17.religiouscalendar.model.Setting
import java.io.*

/**
 * Created by 阿弥陀佛 on 2015/7/4.
 */
object SettingContext {
    @Throws(IOException::class, ClassNotFoundException::class)
    fun loadSetting(context: Context): Setting? {
        val file = File(context.filesDir.toString() + "/setting.dat")
        var setting: Setting? = null
        if (file.exists()) {
            val oin = ObjectInputStream(FileInputStream(file))
            setting = oin.readObject() as Setting
            oin.close()
        }
        return setting
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    fun saveSetting(context: Context, setting: Setting?) {
        val file = File(context.filesDir.toString() + "/setting.dat")
        if (!file.exists()) {
            file.createNewFile()
        }
        val oout = ObjectOutputStream(FileOutputStream(file))
        oout.writeObject(setting)
        oout.close()
    }
}