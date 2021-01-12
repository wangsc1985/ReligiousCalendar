package com.wang17.religiouscalendar.model

import android.content.ContentValues
import android.content.Context
import com.wang17.religiouscalendar.emnu.MDrelation
import com.wang17.religiouscalendar.emnu.MDtype
import com.wang17.religiouscalendar.model.Setting.KEYS
import com.wang17.religiouscalendar.util._Utils
import java.util.*

/**
 * Created by 阿弥陀佛 on 2015/11/18.
 */
class DataContext(context: Context) {
    private val dbHelper: DatabaseHelper
    private val context: Context

    //region RunLog
    fun getRunLogs(): List<RunLog> {
        val result: MutableList<RunLog> = ArrayList()
        try {
            //获取数据库对象
            val db = dbHelper.readableDatabase
            //查询获得游标
            val cursor = db.query("runLog", null, null, null, null, null, null)
            //判断游标是否为空
            while (cursor.moveToNext()) {
                val model = RunLog(UUID.fromString(cursor.getString(0)),
                        DateTime(cursor.getLong(1)),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4))
                result.add(model)
            }
            cursor.close()
            db.close()
        } catch (e: Exception) {
            _Utils.printException(context, e)
        }
        return result
    }

    fun addRunLog(runLog: RunLog) {
        try {
            //获取数据库对象
            val db = dbHelper.writableDatabase
            //使用insert方法向表中插入数据
            val values = ContentValues()
            values.put("id", runLog.id.toString())
            values.put("runTime", runLog.runTime.timeInMillis)
            values.put("tag", runLog.tag)
            values.put("item", runLog.item)
            values.put("message", runLog.message)
            //调用方法插入数据
            db.insert("runLog", "id", values)
            //关闭SQLiteDatabase对象
            db.close()
        } catch (e: Exception) {
            _Utils.printException(context, e)
        }
    }

    fun updateRunLog(runLog: RunLog) {
        try {
            //获取数据库对象
            val db = dbHelper.writableDatabase

            //使用update方法更新表中的数据
            val values = ContentValues()
            values.put("runTime", runLog.runTime.timeInMillis)
            values.put("tag", runLog.tag)
            values.put("item", runLog.item)
            values.put("message", runLog.message)
            db.update("runLog", values, "id=?", arrayOf(runLog.id.toString()))
            db.close()
        } catch (e: Exception) {
            _Utils.printException(context, e)
        }
    }

    fun deleteRunLog() {
        try {
            //获取数据库对象
            val db = dbHelper.writableDatabase
            db.delete("runLog", null, null)
            //关闭SQLiteDatabase对象
            db.close()
        } catch (e: Exception) {
            _Utils.printException(context, e)
        }
    }
    //endregion
    //region SexualDay
    /**
     * 增加一条SexualDay
     *
     * @param sexualDay 记录对象
     */
    fun addSexualDay(sexualDay: SexualDay) {
        try {
            //获取数据库对象
            val db = dbHelper.writableDatabase
            //使用insert方法向表中插入数据
            val values = ContentValues()
            values.put("id", sexualDay.id.toString())
            values.put("dateTime", sexualDay.dateTime.timeInMillis)
            values.put("item", sexualDay.item)
            values.put("summary", sexualDay.summary)

            //调用方法插入数据
            db.insert("sexualDay", "id", values)
            //关闭SQLiteDatabase对象
            db.close()
        } catch (e: Exception) {
            _Utils.printException(context, e)
        }
    }

    /**
     * 得到所有SexualDay
     *
     * @return
     */
    fun getLastSexualDay(): SexualDay? {
        try {
            //获取数据库对象
            val db = dbHelper.readableDatabase
            //查询获得游标
            val cursor = db.query("sexualDay", null, null, null, null, null, "DateTime  DESC")
            //判断游标是否为空
            if (cursor.moveToNext()) {
                val model = SexualDay(UUID.fromString(cursor.getString(0)),
                        DateTime(cursor.getLong(1)),
                        cursor.getString(2),
                        cursor.getString(3))
                cursor.close()
                return model
            }
            db.close()
        } catch (e: Exception) {
            _Utils.printException(context, e)
        }
        return null
    }

    /**
     * 得到所有SexualDay
     *
     * @return
     */
    fun getSexualDays(isTimeDesc: Boolean): MutableList<SexualDay> {
        val result: MutableList<SexualDay> = ArrayList()
        try {
            //获取数据库对象
            val db = dbHelper.readableDatabase
            //查询获得游标
            val cursor = db.query("sexualDay", null, null, null, null, null, if (isTimeDesc) "DateTime DESC" else null)
            //判断游标是否为空
            while (cursor.moveToNext()) {
                val model = SexualDay(UUID.fromString(cursor.getString(0)),
                        DateTime(cursor.getLong(1)),
                        cursor.getString(2),
                        cursor.getString(3))
                result.add(model)
            }
            cursor.close()
            db.close()
        } catch (e: Exception) {
            _Utils.printException(context, e)
        }
        return result
    }

    /**
     * 得到所有SexualDay
     *
     * @return
     */
    fun updateSexualDay(sexualDay: SexualDay) {
        try {
            //获取数据库对象
            val db = dbHelper.writableDatabase

            //使用update方法更新表中的数据
            val values = ContentValues()
            values.put("dateTime", sexualDay.dateTime.timeInMillis)
            values.put("item", sexualDay.item)
            values.put("summary", sexualDay.summary)
            db.update("sexualDay", values, "id=?", arrayOf(sexualDay.id.toString()))
            db.close()
        } catch (e: Exception) {
            _Utils.printException(context, e)
        }
    }

    /**
     * 删除指定的record
     *
     * @param id
     */
    fun deleteSexualDay(id: UUID) {
        try {
            //获取数据库对象
            val db = dbHelper.writableDatabase
            db.delete("sexualDay", "id=?", arrayOf(id.toString()))
            //关闭SQLiteDatabase对象
            db.close()
        } catch (e: Exception) {
            _Utils.printException(context, e)
        }
    }

    //endregion
    //region MemorialDay
    fun addMemorialDay(memorialDay: MemorialDay) {
        //获取数据库对象
        val db = dbHelper.writableDatabase
        //使用insert方法向表中插入数据
        val values = ContentValues()
        values.put("id", memorialDay.id.toString())
        values.put("type", memorialDay.type.toInt())
        values.put("relation", memorialDay.relation.toInt())
        values.put("month", memorialDay.lunarDate.month)
        values.put("day", memorialDay.lunarDate.day)

        //调用方法插入数据
        db.insert("memorialDay", "id", values)
        //关闭SQLiteDatabase对象
        db.close()
    }

    fun getMemorialDay(id: UUID): MemorialDay? {

        //获取数据库对象
        val db = dbHelper.readableDatabase
        //查询获得游标
        val cursor = db.query("memorialDay", null, "id=?", arrayOf(id.toString()), null, null, null)
        //判断游标是否为空
        if (cursor.moveToNext()) {
            val model = MemorialDay(UUID.fromString(cursor.getString(0)),
                    MDtype.fromInt(cursor.getInt(1)),
                    MDrelation.fromInt(cursor.getInt(2)),
                    LunarDate(cursor.getInt(3), cursor.getInt(4)))
            cursor.close()
            return model
        }
        return null
    }

    fun getMemorialDays(lunarMonth: Int, lunarDay: Int): List<MemorialDay> {
        val result: MutableList<MemorialDay> = ArrayList()
        //获取数据库对象
        val db = dbHelper.readableDatabase
        //查询获得游标
        val cursor = db.query("memorialDay", null, "month=? AND day=?", arrayOf(lunarMonth.toString() + "", lunarDay.toString() + ""), null, null, null)
        //判断游标是否为空
        while (cursor.moveToNext()) {
            val model = MemorialDay(UUID.fromString(cursor.getString(0)),
                    MDtype.fromInt(cursor.getInt(1)),
                    MDrelation.fromInt(cursor.getInt(2)),
                    LunarDate(cursor.getInt(3), cursor.getInt(4)))
            result.add(model)
        }
        cursor.close()
        return result
    }

    fun getMemorialDays(): List<MemorialDay> {
        val result: MutableList<MemorialDay> = ArrayList()
        //获取数据库对象
        val db = dbHelper.readableDatabase
        //查询获得游标
        val cursor = db.query("memorialDay", null, null, null, null, null, null)
        //判断游标是否为空
        while (cursor.moveToNext()) {
            val model = MemorialDay(UUID.fromString(cursor.getString(0)),
                    MDtype.fromInt(cursor.getInt(1)),
                    MDrelation.fromInt(cursor.getInt(2)),
                    LunarDate(cursor.getInt(3), cursor.getInt(4)))
            result.add(model)
        }
        cursor.close()
        return result
    }

    fun deleteMemorialDay(id: UUID) {
        //获取数据库对象
        val db = dbHelper.writableDatabase
        db.delete("memorialDay", "id=?", arrayOf(id.toString()))
        //关闭SQLiteDatabase对象
        db.close()
    }

    //endregion
    //region Setting
    fun getSetting(key: KEYS): Setting? {

        //获取数据库对象
        val db = dbHelper.readableDatabase
        //查询获得游标
        val cursor = db.query("setting", null, "key=?", arrayOf(key.toString()), null, null, null)
        //判断游标是否为空
        while (cursor.moveToNext()) {
            val setting = Setting(key.toString(), cursor.getString(1))
            cursor.close()
            return setting
        }
        return null
    }

    fun getSetting(key: KEYS, defaultValue: Any): Setting {
        var setting = getSetting(key)
        if (setting == null) {
            addSetting(key, defaultValue)
            setting = Setting(key.toString(), defaultValue.toString())
            return setting
        }
        return setting
    }

    /**
     * 修改制定key配置，如果不存在则创建。
     *
     * @param key
     * @param value
     */
    fun editSetting(key: KEYS, value: Any) {
        //获取数据库对象
        val db = dbHelper.writableDatabase
        //使用update方法更新表中的数据
        val values = ContentValues()
        values.put("value", value.toString())
        if (db.update("setting", values, "key=?", arrayOf(key.toString())) == 0) {
            addSetting(key, value.toString())
        }
        db.close()
    }

    fun deleteSetting(key: KEYS) {
        //获取数据库对象
        val db = dbHelper.writableDatabase
        db.delete("setting", "key=?", arrayOf(key.toString()))
        //        String sql = "DELETE FROM setting WHERE userId="+userId.toString()+" AND key="+key;
//        addLog(new Log(sql,userId),db);
        //关闭SQLiteDatabase对象
        db.close()
    }

    fun addSetting(key: KEYS, value: Any) {
        //获取数据库对象
        val db = dbHelper.writableDatabase
        //使用insert方法向表中插入数据
        val values = ContentValues()
        values.put("key", key.toString())
        values.put("value", value.toString())
        //调用方法插入数据
        db.insert("setting", "key", values)
        //关闭SQLiteDatabase对象
        db.close()
    } //endregion

    init {
        dbHelper = DatabaseHelper(context)
        this.context = context
    }
}