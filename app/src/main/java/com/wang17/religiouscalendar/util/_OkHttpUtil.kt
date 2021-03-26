package com.wang17.religiouscalendar.util

import com.wang17.religiouscalendar.model.HttpCallback
import okhttp3.*
import java.io.IOException

/**
 * @Description
 * @ClassName OkHttpClientUtil
 * @Author
 * @Copyright
 */
object _OkHttpUtil {

    @JvmField
    var client: OkHttpClient
    init {
        client = OkHttpClient()
    }

    @JvmStatic
    fun getRequest(url: String?, callback: HttpCallback) {
        //创建okHttpClient对象
        val mOkHttpClient = client

        //创建一个Request
        val request = Request.Builder()
                .url(url!!)
                .build()
        //new call
        val call = mOkHttpClient!!.newCall(request)
        //请求加入调度
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                    callback.excute("xxxxxxxxxxx  ${e.message}")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    //回调的方法执行在子线程。
                    val htmlStr = response.body!!.string()
                    callback.excute(htmlStr)
                }else{
                    callback.excute("xxxxxxxxxxx  response is not Successful......")
                }
            }
        })
    }
}