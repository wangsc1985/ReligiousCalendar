package com.wang17.religiouscalendar.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by 阿弥陀佛 on 2015/11/18.
 */
class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        // 创建数据库后，对数据库的操作
        db.execSQL("create table if not exists setting("
                + "key TEXT PRIMARY KEY,"
                + "value TEXT)")
        db.execSQL("create table if not exists memorialDay("
                + "id TEXT PRIMARY KEY,"
                + "type TEXT,"
                + "relation TEXT,"
                + "month INTEGER,"
                + "day INTEGER)")
        db.execSQL("create table if not exists runLog("
                + "id TEXT PRIMARY KEY,"
                + "runTime LONG,"
                + "tag TEXT,"
                + "item TEXT,"
                + "message TEXT)")
        db.execSQL("create table if not exists sexualDay("
                + "id TEXT PRIMARY KEY,"
                + "dateTime LONG,"
                + "item TEXT,"
                + "summary TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 更改数据库版本的操作
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("create table if not exists runLog("
                    + "id TEXT PRIMARY KEY,"
                    + "runTime LONG,"
                    + "tag TEXT,"
                    + "item TEXT,"
                    + "message TEXT)")
            db.execSQL("create table if not exists sexualDay("
                    + "id TEXT PRIMARY KEY,"
                    + "dateTime LONG,"
                    + "item TEXT,"
                    + "summary TEXT)")
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // 每次成功打开数据库后首先被执行
    }

    companion object {
        private const val VERSION = 2
        private const val DATABASE_NAME = "religiouscalendar.db"
    }
}