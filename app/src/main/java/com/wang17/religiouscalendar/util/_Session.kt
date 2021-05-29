package com.wang17.religiouscalendar.util

import android.support.v7.app.AppCompatActivity
import com.wang17.religiouscalendar.R
import com.wang17.religiouscalendar.model.PicNameRes
import java.util.*

/**
 * Created by 阿弥陀佛 on 2016/9/28.
 */
object _Session : AppCompatActivity() {
    var welcomes: MutableList<PicNameRes>
    var banners: MutableList<PicNameRes>
    var duration = intArrayOf(0, 3000, 4000, 5000, 6000, 7000)

    //    public static AppInfo newVersionAppInfo;
    init {
        welcomes = ArrayList()
        welcomes.add(PicNameRes(R.drawable.welcome01, "观世音菩萨一"))
        welcomes.add(PicNameRes(R.drawable.welcome02, "观世音菩萨二"))
        welcomes.add(PicNameRes(R.drawable.welcome03, "观世音菩萨三"))
        welcomes.add(PicNameRes(R.drawable.welcome04, "观世音菩萨四"))
        welcomes.add(PicNameRes(R.drawable.welcome05, "观世音菩萨五"))
        welcomes.add(PicNameRes(R.drawable.amtf, "南无阿弥陀佛"))
        welcomes.add(PicNameRes(R.drawable.jyt, "阿弥陀佛接引图"))
        banners = ArrayList()
        banners.add(PicNameRes(R.drawable.banner01, "释迦牟尼佛"))
        banners.add(PicNameRes(R.drawable.banner02, "弥勒菩萨"))
        banners.add(PicNameRes(R.drawable.banner03, "大智度论"))
        banners.add(PicNameRes(R.drawable.banner04, "善导大师"))
        banners.add(PicNameRes(R.drawable.banner06, "佛法会"))
    }
}