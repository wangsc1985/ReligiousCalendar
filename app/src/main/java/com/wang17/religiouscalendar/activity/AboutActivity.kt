package com.wang17.religiouscalendar.activity

import android.R.attr.versionName
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.wang17.religiouscalendar.R
import com.wang17.religiouscalendar.model.DateTime
import com.wang17.religiouscalendar.util._Session
import kotlinx.android.synthetic.main.activity_about.*


class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val pm: PackageManager = getPackageManager()
        val pi: PackageInfo = pm.getPackageInfo(getPackageName(), 0)
        val versionName = pi.versionName
        versionName?.let {
            tv_version.text="寿康宝鉴日历 V${it}"
        }
        tv_about.text="Copyright @ 2012-${DateTime.getToday().getYear()} 王世超. "
        // 下载
        tv_loadUrl.setOnClickListener {
//            AlertDialog.Builder(this).setCancelable(false).setMessage(resources.getString(R.string.load_url_introduce)).setPositiveButton("前往", DialogInterface.OnClickListener { dialog, which ->
//            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resources.getString(R.string.load_url))))
//            }).show()
            AlertDialog.Builder(this).setCancelable(false).setMessage("点击下载rc.apk文件，并安装。").setPositiveButton("下载", DialogInterface.OnClickListener { dialog, which ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(_Session.app_url)))
            }).show()
//            val intent = Intent(this, WebActivity::class.java)
//            intent.putExtra("url", resources.getString(R.string.load_url))
//            startActivity(intent)
        }
        // 联系我们
        tv_contact.setOnClickListener {
            AlertDialog.Builder(this).setTitle("联系方式").setCancelable(false).setMessage(resources.getString(R.string.contact)).setPositiveButton("关闭",null).show()
        }
        // 隐私保护
        tv_info.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resources.getString(R.string.home_url))))
//            startActivity(Intent(this, WebActivity::class.java))
        }
    }
}