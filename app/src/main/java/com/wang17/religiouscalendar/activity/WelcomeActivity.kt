package com.wang17.religiouscalendar.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.*
import com.wang17.religiouscalendar.R
import com.wang17.religiouscalendar.model.*
import com.wang17.religiouscalendar.util._Session
import com.wang17.religiouscalendar.util._Utils

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_welcome)
            val context = DataContext(this@WelcomeActivity)
            val softVersion = 13
            val welcomeImg = findViewById(R.id.imageBuddha) as ImageView
            val setting = context.getSetting(Setting.KEYS.welcome)
            var itemPosition = 0
            if (setting != null) {
                itemPosition = setting.getInt()
            } else {
                context.addSetting(Setting.KEYS.welcome, itemPosition.toString() + "")
            }
            if (itemPosition >= _Session.welcomes.size) {
                itemPosition = 0
                context.editSetting(Setting.KEYS.welcome, itemPosition.toString() + "")
            }
            welcomeImg.setImageResource(_Session.welcomes[itemPosition].getResId())
            val animation = AlphaAnimation(1f, 1f)
            animation.duration = 2500 // 设置动画显示时间
            animation.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    Log.i("wangsc", "WelcomeActivity animation start ...")
                }

                override fun onAnimationEnd(animation: Animation) {
                    Log.i("wangsc", "WelcomeActivity animation end ...")
                    startActivity(Intent(application, MainActivity::class.java))
                    finish()
                }

                override fun onAnimationRepeat(animation: Animation) {
                    Log.i("wangsc", "WelcomeActivity animation repeat ...")
                }
            })
            welcomeImg.startAnimation(animation)
        } catch (ex: Exception) {
            _Utils.printExceptionSycn(this@WelcomeActivity, uiHandler, ex)
        }
    }

    private val uiHandler = Handler()

    internal inner class splashhandler : Runnable {
        override fun run() {
            startActivity(Intent(application, MainActivity::class.java))
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
}