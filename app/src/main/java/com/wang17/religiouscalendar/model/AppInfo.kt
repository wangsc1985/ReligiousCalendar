package com.wang17.religiouscalendar.model

class AppInfo(private val packageName: String, private val versionCode: Int, private val versionName: String, private val loadUrl: String, private val accessToken: String) {
    fun getPackageName(): String {
        return packageName
    }

    fun getVersionCode(): Int {
        return versionCode
    }

    fun getVersionName(): String {
        return versionName
    }

    fun getLoadUrl(): String {
        return loadUrl
    }

    fun getAccessToken(): String {
        return accessToken
    }
}