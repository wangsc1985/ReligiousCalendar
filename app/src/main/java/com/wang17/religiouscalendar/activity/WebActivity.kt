package com.wang17.religiouscalendar.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.*
import com.wang17.religiouscalendar.R
import com.wang17.religiouscalendar.e
import com.wang17.religiouscalendar.fragment.ActionBarFragment
import kotlinx.android.synthetic.main.activity_web.*

class WebActivity : AppCompatActivity() , ActionBarFragment.OnActionFragmentBackListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        val url = intent.getStringExtra("url")
        webview.settings.javaScriptEnabled=true
        webview.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webview.webViewClient = WebviewDlgClient()
        pb_loadding.visibility=View.VISIBLE
        webview.loadUrl(url ?: "https://sahacloudmanager.azurewebsites.net")
    }

    override fun onBackListener() {
        finish()
    }

    inner class WebviewDlgClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            e("onPageFinished")
            super.onPageFinished(view, url)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            e("onPageStarted")
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            e("onPageCommitVisible")
            super.onPageCommitVisible(view, url)
            pb_loadding.visibility = View.INVISIBLE
        }
    }
}


