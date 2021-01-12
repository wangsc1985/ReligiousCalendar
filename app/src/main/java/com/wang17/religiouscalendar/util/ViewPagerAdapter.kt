package com.wang17.religiouscalendar.util

import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View

/**
 * Created by 阿弥陀佛 on 2016/10/4.
 */
class ViewPagerAdapter(  //界面列表
        private val views: List<View>?) : PagerAdapter() {
    //销毁arg1位置的界面
    override fun destroyItem(arg0: View, arg1: Int, arg2: Any) {
        (arg0 as ViewPager).removeView(views!![arg1])
    }

    //获得当前界面数
    override fun getCount(): Int {
        return views?.size ?: 0
    }

    //初始化arg1位置的界面
    override fun instantiateItem(arg0: View, arg1: Int): Any {
        (arg0 as ViewPager).addView(views!![arg1], 0)
        return views[arg1]
    }

    //判断是否由对象生成界面
    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        return arg0 === arg1
    }
}