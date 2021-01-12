package com.wang17.religiouscalendar.util

import android.content.Context
import android.view.View
import android.view.animation.*
import android.view.animation.AnimationUtils
import com.wang17.religiouscalendar.R

/**
 * Created by Administrator on 2017/6/30.
 */
object AnimationUtils {
    /**
     * 闪烁动画
     *
     * @param targetView 动画对象
     */
    fun setFlickerAnimation(targetView: View, vararg duration: Long) {
        val animation: Animation = AlphaAnimation(1f, 0f)
        if (duration.size == 0) animation.duration = 700 //闪烁时间间隔
        else if (duration.size == 1) animation.duration = duration[0] //闪烁时间间隔
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.repeatMode = Animation.REVERSE
        targetView.animation = animation
    }

    fun setRorateAnimation(context: Context?, target: View, vararg duration: Long) {
        val rorateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_animation)
        if (duration.size == 1) rorateAnimation.duration = duration[0]
        val lin = LinearInterpolator()
        rorateAnimation.interpolator = lin
        target.startAnimation(rorateAnimation)
        //        return rorateAnimation;
    }
}