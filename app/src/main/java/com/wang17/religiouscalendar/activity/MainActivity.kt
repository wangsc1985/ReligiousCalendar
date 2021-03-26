package com.wang17.religiouscalendar.activity

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources.NotFoundException
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.View.OnLongClickListener
import android.view.animation.Animation
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.wang17.religiouscalendar.R
import com.wang17.religiouscalendar.activity.IntroduceActivity.ItemName
import com.wang17.religiouscalendar.e
import com.wang17.religiouscalendar.emnu.SolarTerm
import com.wang17.religiouscalendar.model.*
import com.wang17.religiouscalendar.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_main_banner.*
import kotlinx.android.synthetic.main.include_main_info.*
import kotlinx.android.synthetic.main.include_main_menu.*
import kotlinx.android.synthetic.main.inflate_dialog_privacy.*
import java.io.*
import java.nio.ByteBuffer
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.experimental.and


class MainActivity : AppCompatActivity(), View.OnClickListener {

    // 视图变量
    private lateinit var headerView: View
    private lateinit var layoutAbout: View
    private lateinit var tvChijie1: TextView
    private lateinit var tvChijie2: TextView
    private lateinit var calendarAdapter: CalenderGridAdapter
    private lateinit var ibSettting: ImageButton
    private lateinit var userCalender: GridView
    private lateinit var mPopWindow: PopupWindow
    private lateinit var layoutJinJi: LinearLayout
    private lateinit var layoutJyw: LinearLayout
    private lateinit var layout_ygx: LinearLayout
    private lateinit var layoutRecord: LinearLayout
    private lateinit var progressBar_loading: ProgressBar

    // 类变量
    private lateinit var dc: DataContext
    private lateinit var fontHWZS: Typeface
    private lateinit var fontGF: Typeface
    private lateinit var selectedDate: DateTime
    private lateinit var refreshCalendarTask: RefreshCalendarTask

    // 值变量
    lateinit var animation: Animation
    private var calendarItemLength = 0
    private var preSelectedPosition = 0
    private var todayPosition = 0
    private var currentYear = 0
    private var currentMonth = 0
    private var xxxTimeMillis: Long = 0
    private var isFirstTime = false
    private var isShowRecords = false
    private lateinit var calendarItemsMap: MutableMap<Int, CalendarItem>
    private lateinit var solarTermMap: TreeMap<DateTime, SolarTerm>
    private lateinit var currentMonthSolarTermMap: MutableMap<DateTime, SolarTerm>
    private lateinit var calendarItemViewsMap: MutableMap<DateTime, View>
    private lateinit var religiousDayMap: HashMap<DateTime, MutableList<ReligiousInfo>>
    private var uiHandler = Handler()
    private lateinit var calenderHeaderGridAdapter: CalenderHeaderGridAdapter
    private var isWeekendFirst = false
    private var welcomeDurationIndex = 0
    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }

    //region 动态权限申请
    var permissions = arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET
    )
    var mNoPassedPermissionList: MutableList<String> = ArrayList()

    /**
     * 申请权限
     */
    private val MY_PERMISSIONS_REQUEST: Int = 100
    fun requestPermission(): Boolean {
        mNoPassedPermissionList.clear()
        for (i in permissions.indices) {
            if (ContextCompat.checkSelfPermission(this@MainActivity, permissions.get(i)) != PackageManager.PERMISSION_GRANTED) {
                e("权限名称 : ${permissions[i]} , 返回结果 : 未授权")
                mNoPassedPermissionList.add(permissions.get(i))
            }
        }
        if (mNoPassedPermissionList.isEmpty()) {
            return true
        } else {
            //请求权限方法
            val permissions = mNoPassedPermissionList.toTypedArray()
            ActivityCompat.requestPermissions(this@MainActivity, permissions, MY_PERMISSIONS_REQUEST)
            return false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    // 判断 是否仍然继续可以申请权限
                    AlertDialog.Builder(this).setMessage("授权失败").setNegativeButton("知道了", DialogInterface.OnClickListener { dialog, which ->
                        this.finish()
                    }).show()
                    return
                }
            }
            initOnCreate()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    //endregion

    internal class MyClickText(private val context: Context) : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            //设置文本的颜色
            ds.color = context.resources.getColor(R.color.colorPrimary)
            //超链接形式的下划线，false 表示不显示下划线，true表示显示下划线
            ds.isUnderlineText = false
        }

        override fun onClick(widget: View) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(context.resources.getString(R.string.home_url))))
//            context.startActivity(Intent(context,WebActivity::class.java))
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            dc = DataContext(this)

            /**
             * 每次权限有更改，只需增加下面数字就可以。
             */
            val curPrivacyVersion = 2
            val dbPrivacyVersion = dc.getSetting(Setting.KEYS.privacy_version, 0).getInt()

            if (dbPrivacyVersion >= curPrivacyVersion) {
                if (requestPermission()) {
                initOnCreate()
                }
            } else {
                var s = resources.getString(R.string.pravacy_intro)
                val spannable = Spannable.Factory.getInstance().newSpannable(s)//将字符串包装成可操作的文本格式
                val click = MyClickText(this)
                spannable.setSpan(click, 64, 70, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                val view = View.inflate(this, R.layout.inflate_dialog_privacy, null)
                val tv_content = view.findViewById<TextView>(R.id.tv_content)
                tv_content.text = spannable
                tv_content.setMovementMethod(LinkMovementMethod.getInstance())
                val btn_no = view.findViewById<Button>(R.id.btn_no)
                val btn_ok = view.findViewById<Button>(R.id.btn_ok)

                val dialog = AlertDialog.Builder(this).setView(view).setCancelable(false).show()
                btn_no.setOnClickListener {
                    finish()
                }
                btn_ok.setOnClickListener {
                    dialog.dismiss()
                    if (requestPermission()) {
                    initOnCreate()
                    }
                    dc.editSetting(Setting.KEYS.privacy_version, curPrivacyVersion)
                }
            }
        } catch (ex: Exception) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, ex)
        }
    }

    private fun initOnCreate() {
        isWeekendFirst = dc.getSetting(Setting.KEYS.is_weekend_first, true).getBoolean()
        val pics = ArrayList<Int>()
        xxxTimeMillis = System.currentTimeMillis()
        isFirstTime = true
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.setDrawerListener(toggle)
        toggle.syncState()
        val nav_view = findViewById(R.id.nav_view) as NavigationView
        headerView = nav_view.getHeaderView(0)
        nav_view.setNavigationItemSelectedListener { item ->
            menuItemSelected(item)
            drawer_layout.closeDrawer(GravityCompat.START)
            true
        }

        //region 启动界面
        welcomeDurationIndex = dc.getSetting(Setting.KEYS.welcome_duration, 1).getInt()
        if (welcomeDurationIndex == 0) {
            imageView_welcome.visibility = View.INVISIBLE
        } else {
            imageView_welcome.visibility = View.VISIBLE
            var itemPosition = dc.getSetting(Setting.KEYS.welcome, 0).getInt()
            if (itemPosition >= _Session.welcomes.size) {
                itemPosition = 0
                dc.editSetting(Setting.KEYS.welcome, itemPosition.toString() + "")
            }
            imageView_welcome.setImageResource(_Session.welcomes[itemPosition].getResId())
        }

        //endregion


        //
        solarTermMap = loadJavaSolarTerms(R.raw.solar_java_50)

        //
        initViews()

        checkVersion()
    }

    private fun checkVersion() {
        _OkHttpUtil.getRequest(resources.getString(R.string.version_url), HttpCallback { html ->
            try {
                var html = html.replace("\r", "").replace("\n", "")
                var matcher = Pattern.compile("(?<=寿康宝鉴日历版本号)[0-9]*(?=</p>)").matcher(html)
                matcher.find()
                val version = matcher.group().trim().toInt()
                val checkedVersionCode = dc.getSetting(Setting.KEYS.checked_version_code, _Utils.getVersionCode(this)).getInt()
                e("最新版本号：${version}")

                if (version != checkedVersionCode) {
                    uiHandler.post {
                        AlertDialog.Builder(this).setMessage("当前版本号：${_Utils.getVersionCode(this)}，最新版本号：${version}。\n请到百度网盘下载，邀请码：0000。")
                                .setPositiveButton("前往下载") { dialog, which ->
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resources.getString(R.string.load_url))))
                                }
                                .setNegativeButton("忽略") { dialog, which ->
                                    dc.editSetting(Setting.KEYS.checked_version_code, version)
                                }.show()
                    }
                }
            } catch (e: Exception) {
            }
        })
    }

    //region 事件
    var leftMenuClick = View.OnClickListener {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }
    var leftMenuLongClick = OnLongClickListener {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        startActivityForResult(Intent(this@MainActivity, SettingActivity::class.java), TO_SETTING_ACTIVITY)
        true
    }
    var settingClick = View.OnClickListener {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        startActivityForResult(Intent(this@MainActivity, SettingActivity::class.java), TO_SETTING_ACTIVITY)
    }
    //endregion
    /**
     * 六字名号呼吸效果。
     */
    @Throws(Exception::class)
    private fun nianfo(target: View?) {
        val objectAnimator = AnimatorInflater.loadAnimator(this@MainActivity, R.animator.color_animator) as ObjectAnimator
        objectAnimator.setEvaluator(ArgbEvaluator())
        objectAnimator.target = target
        objectAnimator.start()
    }

    private fun setTextForRecord(text1: String, text2: String) {
        var text1 = text1
        var text2 = text2
        if (!text2.isEmpty()) {
            text1 = "禁欲：$text1"
            text2 = if (text2.contains("+")) "元气已经恢复：${text2}" else "${text2}后，元气方可复原。"
        }
        tvChijie1.text = text1
        tvChijie2.text = text2
    }

    /**
     * 方法 - 初始化所有变量
     */
    private fun initViews() {
        try {
            calendarItemViewsMap = HashMap()
            currentMonthSolarTermMap = HashMap()
            religiousDayMap = HashMap()
            refreshCalendarTask = RefreshCalendarTask()

            //region 持戒记录功能设置
            layoutRecord = headerView.findViewById(R.id.layout_record)
            tvChijie1 = headerView.findViewById(R.id.textView_chijie)
            tvChijie2 = headerView.findViewById(R.id.textView_chijie2)
            initRecordPart()


            //endregion

            iv_left.setOnClickListener {
                val selectedDay = selectedDate.getDay()
                var datetime = selectedDate.addMonths(-1)
                val maxDayOfMonth = datetime.getActualMaximum(Calendar.DAY_OF_MONTH)
                setSelectedDate(datetime.getYear(), datetime.getMonth(), if (maxDayOfMonth < selectedDay) maxDayOfMonth else selectedDay)
            }
            iv_right.setOnClickListener {
                val selectedDay = selectedDate.getDay()
                var datetime = selectedDate.addMonths(1)
                val maxDayOfMonth = datetime.getActualMaximum(Calendar.DAY_OF_MONTH)
                setSelectedDate(datetime.getYear(), datetime.getMonth(), if (maxDayOfMonth < selectedDay) maxDayOfMonth else selectedDay)
            }

            //region 左侧菜单操作
            layout_ygx = headerView.findViewById(R.id.layout_ygx)
            layout_ygx.setOnClickListener(View.OnClickListener {
                val intent = Intent(this@MainActivity, IntroduceActivity::class.java)
                intent.putExtra(IntroduceActivity.Companion.PARAM_NAME, ItemName.印光大师序.toString())
                startActivity(intent)
            })
            layoutJinJi = headerView.findViewById(R.id.layout_jinji)
            layoutJinJi.setOnClickListener(View.OnClickListener {
                val intent = Intent(this@MainActivity, IntroduceActivity::class.java)
                intent.putExtra(IntroduceActivity.Companion.PARAM_NAME, ItemName.天地人禁忌.toString())
                startActivity(intent)
            })
            layoutJyw = headerView.findViewById(R.id.layout_jyw)
            layoutJyw.setOnClickListener(View.OnClickListener {
                val intent = Intent(this@MainActivity, IntroduceActivity::class.java)
                intent.putExtra(IntroduceActivity.Companion.PARAM_NAME, ItemName.文昌帝君戒淫文.toString())
                startActivity(intent)
            })
            layoutAbout = headerView.findViewById(R.id.layout_about)
            layoutAbout.setOnClickListener {
                val intent = Intent(this, AboutActivity::class.java)
//                intent.putExtra("url","https://sahacloudmanager.azurewebsites.net")
                startActivity(intent)
            }
            //endregion

            progressBar_loading = findViewById(R.id.progressBar_loading) as ProgressBar

            //
            var itemPosition = 0
            itemPosition = dc.getSetting(Setting.KEYS.banner, itemPosition).getInt()
            if (itemPosition >= _Session.banners.size) {
                itemPosition = 0
                dc.editSetting(Setting.KEYS.banner, itemPosition)
            }

            // 加载include_main_banner
            imageView_banner.setImageResource(_Session.banners[itemPosition].getResId())
            imageView_banner.setOnClickListener { showPopupWindow() }
            rorateWan()




            ibSettting = headerView.findViewById(R.id.imageButton_setting)
            ib_leftMenu.setOnClickListener(leftMenuClick)
            ib_leftMenu.setOnLongClickListener(leftMenuLongClick)
            ibSettting.setOnClickListener(settingClick)
            ibSettting.setOnLongClickListener(OnLongClickListener { true })


            val mgr = assets //得到AssetManager
            fontHWZS = Typeface.createFromAsset(mgr, "fonts/STZHONGS.TTF")
            fontGF = Typeface.createFromAsset(mgr, "fonts/GONGFANG.ttf")
            //            textViewFo.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
//            ((TextView)findViewById(R.id.textView_banner_text)).setTypeface(fontHWZS);
//            textViewFo.getPaint().setFakeBoldText(true);
            //
            nianfo(tvfo)

            // selectedDate
            selectedDate = DateTime.getToday()
            currentYear = selectedDate.getYear()
            currentMonth = selectedDate.getMonth()

            // buttonToday
            btn_today.setTypeface(fontGF)
            btn_today.setOnClickListener(btnToday_OnClickListener)

//            textViewToday = (TextView) findViewById(R.id.textView_today);
//            textViewToday.setTypeface(fontGF);

            // 信息栏
//            yearMonth = (TextView) findViewById(R.id.tvYearMonth);
//            yangliBig = (TextView) findViewById(R.id.tvYangLiBig);
            button_month.setOnClickListener(btnCurrentMonth_OnClickListener)
            val button_quick_month = findViewById(R.id.button_quick_month) as Button
            button_quick_month.setText((currentMonth + 1).toString() + "月")
            button_quick_month.setOnClickListener {
                val selectedDay = selectedDate.getDay()
                var dateTime = DateTime(currentYear, currentMonth, selectedDay)
                dateTime = dateTime.addMonths(1)
                setSelectedDate(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay())
                button_quick_month.setText((currentMonth + 1).toString() + "月")
                //                    }else{
//                        dateTime = dateTime.addMonths(-1);
//                        setSelectedDate(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay());
//                        buttonQuickMonth.setText(currentMonth+1+"月");
//                    }
            }
            textView_year.setOnClickListener(btnCurrentMonth_OnClickListener)
            textView_year.setTypeface(fontGF)
            //            tvYear.getPaint().setFakeBoldText(true);
            button_month.setTypeface(fontGF)
            //            tvMonth.getPaint().setFakeBoldText(true);
            textView_ganZhi.setTypeface(fontHWZS)
            textView_ganZhi.paint.isFakeBoldText = true
            textView_nongLi.setTypeface(fontHWZS)
            textView_nongLi.paint.isFakeBoldText = true
            tvfo.setTypeface(fontGF)
            //            tvFo.getPaint().setFakeBoldText(true);

            // calendarAdapter
            calendarAdapter = CalenderGridAdapter()
            calendarItemsMap = HashMap()
            setYearMonthText()

            // btnCurrentMonth
//            btnCurrentMonth = (Button) findViewById(R.id.btnChangeMonth);
//            btnCurrentMonth.setOnClickListener(btnCurrentMonth_OnClickListener);

            // userCalender
            userCalender = findViewById(R.id.userCalender) as GridView
            userCalender.onItemClickListener = userCalender_OnItemClickListener
            val userCalenderHeader = findViewById(R.id.userCalenderHeader) as GridView
            calenderHeaderGridAdapter = CalenderHeaderGridAdapter()
            userCalenderHeader.adapter = calenderHeaderGridAdapter // 添加星期标头

            // 填充日历
            Thread { refreshCalendar() }.start()
        } catch (ex: Exception) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, ex)
        }
    }

    private fun setYearMonthText() {
        button_month.setText((currentMonth + 1).toString() + "月")
        textView_year.text = _String.concat(currentYear, "年")
        //        int month = new Lunar(selectedDate).getMonth();
//        String monthStr = "";
//        switch (month) {
//            case 1:
//                monthStr = "一";
//                break;
//            case 2:
//                monthStr = "二";
//                break;
//            case 3:
//                monthStr = "三";
//                break;
//            case 4:
//                monthStr = "四";
//                break;
//            case 5:
//                monthStr = "五";
//                break;
//            case 6:
//                monthStr = "六";
//                break;
//            case 7:
//                monthStr = "七";
//                break;
//            case 8:
//                monthStr = "八";
//                break;
//            case 9:
//                monthStr = "九";
//                break;
//            case 10:
//                monthStr = "十";
//                break;
//            case 11:
//                monthStr = "冬";
//                break;
//            case 12:
//                monthStr = "腊";
//                break;
//        }
//        buttonMonth.setText(monthStr);
    }

    private fun initRecordPart() {
        try {
            layoutRecord.setOnClickListener { showAddSexualDayDialog() }

            if (dc.getSetting(Setting.KEYS.targetAuto, true).getBoolean() == false) {
                dc.editSetting(Setting.KEYS.recordIsOpened, false)
                dc.editSetting(Setting.KEYS.targetAuto, true)
                dc.deleteSetting(Setting.KEYS.targetInHour)
                //                new AlertDialog.Builder(this).setMessage("系统移除了自定义行房周期功能，请到设置界面设置出生日期，再使用此功能。").setNegativeButton("知道了", null).show();
//                return;
            }
            isShowRecords = java.lang.Boolean.parseBoolean(dc.getSetting(Setting.KEYS.recordIsOpened, false).value)
            if (isShowRecords) {
                layoutRecord.visibility = View.VISIBLE
                val lastSexualDay = dc.getLastSexualDay()
                var targetInHour = 0
                if (lastSexualDay != null) {
                    val set = dc.getSetting(Setting.KEYS.birthday)
                    if (set != null) {
                        targetInHour = _Utils.getTargetInHour(set.getDateTime())
                        val haveInHour = ((System.currentTimeMillis() - lastSexualDay.dateTime.timeInMillis) / 3600000).toInt()
                        var leaveInHour = targetInHour - haveInHour
                        if (leaveInHour > 0) {
                            setTextForRecord(DateTime.toSpanString(haveInHour), DateTime.toSpanString(leaveInHour))
                        } else {
                            leaveInHour *= -1
                            setTextForRecord(DateTime.toSpanString(haveInHour), "+" + DateTime.toSpanString(leaveInHour))
                        }
                        tvChijie2.visibility = View.VISIBLE
                    }
                } else {
                    setTextForRecord("点击添加记录", "")
                    tvChijie2.visibility = View.GONE
                }
            } else {
                layoutRecord.visibility = View.GONE
            }
        } catch (e: Exception) {
            _Utils.printException(this@MainActivity, e)
        }
    }

    private fun showPopupWindow() {
        //设置contentView
        val contentView = LayoutInflater.from(this@MainActivity).inflate(R.layout.popup_window, null)
        mPopWindow = PopupWindow(contentView, DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT, true)
        mPopWindow.contentView = contentView
        //设置各个控件的点击响应
        val tv0 = contentView.findViewById<TextView>(R.id.item00)
        val tv1 = contentView.findViewById<TextView>(R.id.item01)
        val tv2 = contentView.findViewById<TextView>(R.id.item02)
        val tv3 = contentView.findViewById<TextView>(R.id.item03)
        val tv4 = contentView.findViewById<TextView>(R.id.item04)
        tv0.setOnClickListener(this)
        tv1.setOnClickListener(this)
        tv2.setOnClickListener(this)
        tv3.setOnClickListener(this)
        tv4.setOnClickListener(this)
        //显示PopupWindow
//        View rootview = (View)findViewById(R.id.layout_upper_banner);
//        mPopWindow.showAtLocation(rootview,);
        val rootview = LayoutInflater.from(this@MainActivity).inflate(R.layout.activity_main, null)
        mPopWindow.showAtLocation(rootview, Gravity.RIGHT or Gravity.TOP, 0, 0)
    }

    override fun onClick(v: View) {
        val id = v.id
        try {
            when (id) {
                R.id.item00 -> {
                    dc.editSetting(Setting.KEYS.banner, 0)
                    imageView_banner.setImageResource(_Session.banners[0].getResId())
                }
                R.id.item01 -> {
                    dc.editSetting(Setting.KEYS.banner, 1)
                    imageView_banner.setImageResource(_Session.banners[1].getResId())
                }
                R.id.item02 -> {
                    dc.editSetting(Setting.KEYS.banner, 2)
                    imageView_banner.setImageResource(_Session.banners[2].getResId())
                }
                R.id.item03 -> {
                    dc.editSetting(Setting.KEYS.banner, 3)
                    imageView_banner.setImageResource(_Session.banners[3].getResId())
                }
                R.id.item04 -> {
                    dc.editSetting(Setting.KEYS.banner, 4)
                    imageView_banner.setImageResource(_Session.banners[4].getResId())
                }
            }
            mPopWindow.dismiss()
        } catch (ex: Exception) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, ex)
        }
    }

    /**
     * 自定义日历标头适配器
     */
    inner class CalenderHeaderGridAdapter : BaseAdapter() {
        private var header: Array<String>
        override fun getCount(): Int {
            return 7
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val mTextView = TextView(applicationContext)
            mTextView.text = header[position]
            mTextView.gravity = Gravity.CENTER_HORIZONTAL
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            mTextView.paint.isFakeBoldText = true
            mTextView.typeface = Typeface.MONOSPACE
            mTextView.setTextColor(Color.parseColor("#000000"))
            mTextView.width = 60
            return mTextView
        }

        override fun notifyDataSetChanged() {
            if (isWeekendFirst) {
                header = arrayOf("日", "一", "二", "三", "四", "五", "六")
            } else {
                header = arrayOf("一", "二", "三", "四", "五", "六", "日")
            }
            super.notifyDataSetChanged()
        }

        init {
            if (isWeekendFirst) {
                header = arrayOf("日", "一", "二", "三", "四", "五", "六")
            } else {
                header = arrayOf("一", "二", "三", "四", "五", "六", "日")
            }
        }
    }
    // FIXME: 2020/5/30 周六排序错误的问题，日历排版有没有BUG？
    /**
     * 自定义日历适配器
     */
    protected inner class CalenderGridAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return calendarItemLength
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
//            convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.inflat_calender_item, null);
            var cv = convertView
            try {
                cv = View.inflate(this@MainActivity, R.layout.inflat_calender_item, null)
                val textViewYangLi = cv.findViewById<TextView>(R.id.calenderItem_tv_YangLiDay)
                val textViewNongLi = cv.findViewById<TextView>(R.id.calendarItem_tv_NongLiDay)
                val imageIsToday = cv.findViewById<ImageView>(R.id.calendarItem_cvIsToday)
                val imageIsSelected = cv.findViewById<ImageView>(R.id.calendarItem_cvIsSelected)
                if (calendarItemsMap.containsKey(position)) {
                    val calendarItem = calendarItemsMap[position]
                    val today = DateTime.getToday()
                    textViewYangLi.text = calendarItem!!.yangLi[Calendar.DAY_OF_MONTH].toString() + ""

                    // 农历月初，字体设置。
                    if (calendarItem!!.nongLi.getDay() == 1) {
                        if (calendarItem!!.nongLi.isLeap())
                            textViewNongLi.text = "闰" + calendarItem!!.nongLi.getMonthStr()
                        else
                            textViewNongLi.text = calendarItem!!.nongLi.getMonthStr()
                        textViewNongLi.setTextColor(Color.BLACK)
                        textViewNongLi.paint.isFakeBoldText = true
                    } else {
                        textViewNongLi.text = calendarItem!!.nongLi.getDayStr()
                    }

                    // 今天
                    if (today.compareTo(calendarItem!!.yangLi.getDate()) == 0) {
                        imageIsToday.visibility = View.VISIBLE
                        textViewYangLi.setTextColor(Color.WHITE)
                        textViewNongLi.setTextColor(Color.WHITE)
                        todayPosition = position
                    }

                    // 选中的日期
                    if (CalendarHelper.isSameDate(calendarItem!!.yangLi, selectedDate) && !CalendarHelper.isSameDate(calendarItem!!.yangLi, today)) {
                        imageIsSelected.visibility = View.VISIBLE
                        preSelectedPosition = position
                    }
                    calendarItemViewsMap[calendarItem!!.yangLi] = cv
                } else {
                    textViewYangLi.text = ""
                    textViewNongLi.text = ""
                }
            } catch (e: Exception) {
                _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
            }
            return cv
        }
    }

    private fun findReligiousKeyWord(religiousList: List<ReligiousInfo>): Boolean {
        religiousList.forEach {
            if (it.religious.contains("俱亡")
                    || it.religious.contains("奇祸")
                    || it.religious.contains("大祸")
                    || it.religious.contains("促寿")
                    || it.religious.contains("恶疾")
                    || it.religious.contains("大凶")
                    || it.religious.contains("绝嗣")
                    || it.religious.contains("男死")
                    || it.religious.contains("女死")
                    || it.religious.contains("血死")
                    || it.religious.contains("一年内死")
                    || it.religious.contains("危疾")
                    || it.religious.contains("水厄")
                    || it.religious.contains("贫夭")
                    || it.religious.contains("暴亡")
                    || it.religious.contains("失瘏夭胎")
                    || it.religious.contains("损寿子带疾")
                    || it.religious.contains("阴错日")
                    || it.religious.contains("十恶大败日")
                    || it.religious.contains("一年内亡")
                    || it.religious.contains("必得急疾")
                    || it.religious.contains("生子五官四肢不全。父母有灾")
                    || it.religious.contains("减寿五年")
                    || it.religious.contains("恶胎")
                    || it.religious.contains("夺纪"))
                return true
        }
        return false
    }

    private fun findReligiousKeyWord(religious: String): Boolean {
        if (religious.contains("俱亡")
                || religious.contains("奇祸")
                || religious.contains("大祸")
                || religious.contains("促寿")
                || religious.contains("恶疾")
                || religious.contains("大凶")
                || religious.contains("绝嗣")
                || religious.contains("男死")
                || religious.contains("女死")
                || religious.contains("血死")
                || religious.contains("一年内死")
                || religious.contains("危疾")
                || religious.contains("水厄")
                || religious.contains("贫夭")
                || religious.contains("暴亡")
                || religious.contains("失瘏夭胎")
                || religious.contains("损寿子带疾")
                || religious.contains("阴错日")
                || religious.contains("十恶大败日")
                || religious.contains("一年内亡")
                || religious.contains("必得急疾")
                || religious.contains("生子五官四肢不全。父母有灾")
                || religious.contains("减寿五年")
                || religious.contains("恶胎")
                || religious.contains("夺纪"))
            return true
        return false
    }

    /**
     * 以星期日为1，星期一为2，以此类推。
     *
     * @param day
     * @return
     */
    @Throws(Exception::class)
    private fun Convert2WeekDay(day: Int): String {
        when (day) {
            1 -> return "星期日"
            2 -> return "星期一"
            3 -> return "星期二"
            4 -> return "星期三"
            5 -> return "星期四"
            6 -> return "星期五"
            7 -> return "星期六"
        }
        return ""
    }

    /**
     * 更新信息栏（农历，干支，戒期信息），一定要在当前月份的日历界面载入完毕后在引用此方法。
     * 因为此方法数据调用calendarItemsMap，而calendarItemsMap是在形成当月数据时形成。
     *
     * @param seletedDateTime 当前选中的日期
     */
    private fun refreshInfoLayout(seletedDateTime: DateTime) {
        try {
            if (calendarItemsMap.size == 0) return
            var calendarItem: CalendarItem? = null
            for ((_, value) in calendarItemsMap) {
                if (CalendarHelper.isSameDate(value.yangLi, seletedDateTime)) {
                    calendarItem = value
                }
            }
            if (calendarItem == null) return

            try {
                val gz = GanZhi(calendarItem.yangLi, solarTermMap)
                textView_ganZhi.text = _String.concat("[", gz.zodiac, "]", gz.tianGanYear, gz.diZhiYear, "年",
                        gz.tianGanMonth, gz.diZhiMonth, "月",
                        gz.tianGanDay, gz.diZhiDay, "日")
                textView_nongLi.text = _String.concat(calendarItem.nongLi.getMonthStr(), calendarItem.nongLi.getDayStr())
            } catch (ex: Exception) {
                _Utils.printExceptionSycn(this@MainActivity, uiHandler, ex)
            }
            linearReligious.removeAllViews()

            calendarItem.religious.forEach {
                val view = View.inflate(this@MainActivity, R.layout.inflate_targ_religious, null)
                val tv = view.findViewById<TextView>(R.id.textView_religious)
                val iv = view.findViewById<ImageView>(R.id.iv_targ)
                when (it.type) {
                    1 -> iv.setImageResource(R.drawable.targ_round_red)
                    2 -> iv.setImageResource(R.drawable.targ_round_green)
                    3 -> iv.setImageResource(R.drawable.targ_square)
                }
                tv.text = it.religious
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, INFO_TEXT_SIZE.toFloat())
                tv.paint.isFakeBoldText = true
                tv.typeface = fontHWZS
                if (findReligiousKeyWord(it.religious)) {
                    tv.setTextColor(resources.getColor(R.color.month_text_color))
                }
                linearReligious.addView(view)
            }

        } catch (e: Exception) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
        }
    }

    /**
     * 得到指定日期在日历中的position。
     *
     * @param dateTime
     * @return
     */
    private fun dateTimeToPosition(dateTime: DateTime, tag: Boolean): Int {
        /**
         * DAY_OF_WEEK：当前周的第几天。从 1 开始。
         * WEEK_OF_MONTH：当前月的第几个星期。从 1 开始。
         *
         * 周日1；  周一2； 周二3；  周三4；  周四5；  周五6；  周六7；
         * 1       2
         * 3       4       5       6       7       8       9
         * 10      11      12      13      14      15      16
         * 17      18      19      20      21      22      23
         * 24      25      26      27      28      29      30
         * 31
         */
        var tag = tag
        return if (isWeekendFirst) {
            val week = dateTime[Calendar.WEEK_OF_MONTH]
            val day_week = dateTime[Calendar.DAY_OF_WEEK]
            (week - 1) * 7 + day_week - 1
        } else {
            // FIXME: 2020/5/29 周六偏差
            /**
             * DAY_OF_WEEK：当前周的第几天。从1开始。
             * WEEK_OF_MONTH：当前月的第几个星期。从1开始。
             *
             * 周一1；  周二2； 周三3；  周四4；  周五5；  周六6；  周日7；
             * 1       2       3
             * 4      5       6       7       8       9       10
             * 11     12      13      14      15      16      17
             * 18     19      20      21      22      23      24
             * 25     26      27      28      29      30      31
             */
            var week = dateTime[Calendar.WEEK_OF_MONTH]
            var day_week = dateTime[Calendar.DAY_OF_WEEK] - 1
            if (day_week == 0) {
                day_week = 7
                week--
                if (week == 0) {
                    tag = true
                }
            }
            if (tag) {
                week++
            }
            (week - 1) * 7 + day_week - 1
        }
    }

    /**
     * 刷新日历界面，使用此方法必须标明forAsynch变量。
     *
     * @throws Exception
     */
    private fun refreshCalendar() {
        try {
            refreshCalendarTask.cancel(true)
            calendarItemsMap.clear()
            calendarItemViewsMap.clear()
            currentMonthSolarTermMap.clear()
            religiousDayMap.clear()
            var maxDayInMonth = 0
            val tmpToday = DateTime(currentYear, currentMonth, 1)
            maxDayInMonth = tmpToday.getActualMaximum(Calendar.DAY_OF_MONTH)
            calendarItemLength = maxDayInMonth
            /**
             * calendarItemLength首先由上面的语句设置为当月天数。然后再由下面的语句，把每个月日历前面空白的几天加上。
             */
            if (isWeekendFirst) {
                val day_week = tmpToday[Calendar.DAY_OF_WEEK]
                calendarItemLength += day_week - 1
            } else {
                var day_week = tmpToday[Calendar.DAY_OF_WEEK] - 1
                if (day_week == 0) day_week = 7
                calendarItemLength += day_week - 1
            }

            // “今”按钮是否显示
            val today = DateTime.getToday()
            if (selectedDate.compareTo(today) == 0) {
                setTodayEnable(false)
            } else {
                setTodayEnable(true)
            }

            // FIXME: 2020/5/29 周六偏差
            // 得到填充日历控件所需要的数据
            var tag = false
            for (i in 1..maxDayInMonth) {
                var week = tmpToday[Calendar.WEEK_OF_MONTH]
                if (isWeekendFirst) {
                    /**
                     * DAY_OF_WEEK：当前周的第几天。从1开始。
                     * WEEK_OF_MONTH：当前月的第几个星期。从1开始。
                     *
                     * 周日1；  周一2； 周二3；  周三4；  周四5；  周五6；  周六7；
                     * 1       2
                     * 3       4       5       6       7       8       9
                     * 10      11      12      13      14      15      16
                     * 17      18      19      20      21      22      23
                     * 24      25      26      27      28      29      30
                     * 31
                     */
                    val day_week = tmpToday[Calendar.DAY_OF_WEEK]
                    val key = (week - 1) * 7 + day_week - 1
                    val newCalendar = DateTime()
                    newCalendar.timeInMillis = tmpToday.timeInMillis
                    val item = CalendarItem(newCalendar)
                    calendarItemsMap[key] = item
                } else {
                    /**
                     * DAY_OF_WEEK：当前周的第几天。从1开始。
                     * WEEK_OF_MONTH：当前月的第几个星期。从1开始。
                     *
                     * 周一1；  周二2； 周三3；  周四4；  周五5；  周六6；  周日7；
                     * 1       2       3
                     * 4      5       6       7       8       9       10
                     * 11     12      13      14      15      16      17
                     * 18     19      20      21      22      23      24
                     * 25     26      27      28      29      30      31
                     */
                    var day_week = tmpToday[Calendar.DAY_OF_WEEK] - 1
                    if (day_week == 0) {
                        day_week = 7
                        week--
                        if (week == 0) {
                            tag = true
                        }
                    }
                    if (tag) {
                        week++
                    }
                    val key = (week - 1) * 7 + day_week - 1
                    val newCalendar = DateTime()
                    newCalendar.timeInMillis = tmpToday.timeInMillis
                    val item = CalendarItem(newCalendar)
                    calendarItemsMap[key] = item
                }
                tmpToday.add(Calendar.DAY_OF_MONTH, 1)
            }

            // 填充日历控件
            todayPosition = -1
            preSelectedPosition = -1
            uiHandler.post {
                try {
                    userCalender.adapter = calendarAdapter
                    refreshCalendarTask = RefreshCalendarTask()
                    refreshCalendarTask.execute()
                    calenderHeaderGridAdapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
                }
            }
            if (isFirstTime && welcomeDurationIndex != 0) {
                val duration = _Session.duration[dc.getSetting(Setting.KEYS.welcome_duration, 1).getInt()]
                Log.i("wangsc", "duration: $duration")
                val span = duration - (System.currentTimeMillis() - xxxTimeMillis)
                if (span > 0) {
                    try {
                        Thread.sleep(span)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                isFirstTime = false
            }
            uiHandler.post {
                try {
                    layout_welcome.visibility = View.INVISIBLE
                } catch (e: Exception) {
                    _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
                }
            }
        } catch (e: NumberFormatException) {
            _Utils.printExceptionSycn(this, uiHandler, e)
        }
    }

    var progres = 0
    private fun disableButton() {
//        imageLeft.setEnabled(false);
//        imageRight.setEnabled(false);
//        imageLeft.setColorFilter(Color.GRAY);
//        imageRight.setColorFilter(Color.GRAY);
        btn_today.setTextColor(Color.GRAY)
    }

    private fun enableButton() {
//        imageLeft.setEnabled(true);
//        imageRight.setEnabled(true);
////        buttonMonth.setEnabled(true);
////        buttonToday.setEnabled(true);
//        imageLeft.setColorFilter(Color.TRANSPARENT);
//        imageRight.setColorFilter(Color.TRANSPARENT);
//        buttonMonth.setTextColor(getResources().getColor(R.color.month_text_color));
        btn_today.setTextColor(resources.getColor(R.color.month_text_color))
    }

    private inner class RefreshCalendarTask : AsyncTask<Any?, Any?, Any?>() {
        /**
         * doInBackground方法内部执行后台任务,不可在此方法内修改UI
         *
         * @param params
         * @return
         */
        override fun doInBackground(params: Array<Any?>): Any? {
            try {
                progres = 0
                publishProgress(progres)
                // 得到本月节气
                for ((key, value) in solarTermMap) {
                    if (key.getYear() == currentYear && key.getMonth() == currentMonth) {
                        currentMonthSolarTermMap[key] = value
                    }
                }
                publishProgress(progres++)

                // 获得当月戒期信息
                try {
                    val religious = Religious(this@MainActivity, currentYear, currentMonth, solarTermMap, object : ReligiousCallBack {
                        override fun execute() {
                            publishProgress(progres++)
                        }
                    })

                    religiousDayMap = religious.religiousDays
                    publishProgress(progres++)
                } catch (e: InterruptedException) {
                } catch (ex: Exception) {
                    religiousDayMap = HashMap()
                    _Utils.printExceptionSycn(this@MainActivity, uiHandler, ex)
                }
                publishProgress(progres++)
            } catch (e: Exception) {
                _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
            }
            return null
        }

        /**
         * onPreExecute方法用于在执行后台任务前做一些UI操作
         */
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar_loading.visibility = View.VISIBLE
            progressBar_loading.progress = 0
            progressBar_loading.max = selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        /**
         * onProgressUpdate方法用于更新进度信息
         *
         * @param values
         */
        override fun onProgressUpdate(values: Array<Any?>) {
            super.onProgressUpdate(*values)
            progressBar_loading.progress = values[0] as Int
            if (values[0] as Int == 0) {
                disableButton()
            }
        }

        /**
         * onPostExecute方法用于在执行完后台任务后更新UI,显示结果
         *
         * @param o
         */
        override fun onPostExecute(o: Any?) {
            super.onPostExecute(o)
            try {
                var tag = false
                val dateTime = DateTime(selectedDate.getYear(), selectedDate.getMonth(), 1)
                if (dateTime[Calendar.DAY_OF_WEEK] - 1 == 0) {
                    tag = true
                }
                for ((key, convertView) in calendarItemViewsMap) {
                    val calendarItem = calendarItemsMap[dateTimeToPosition(key, tag)]
                    if (calendarItem != null) {
                        val textViewNongLi = convertView.findViewById<View>(R.id.calendarItem_tv_NongLiDay) as TextView
                        val imageIsUnReligious = convertView.findViewById<View>(R.id.calendarItem_cvIsUnReligious) as ImageView

                        // 节气
                        for ((key1, value) in currentMonthSolarTermMap) {
                            val today = DateTime(key1.getYear(), key1.getMonth(), key1[Calendar.DAY_OF_MONTH], 0, 0, 0)
                            if (CalendarHelper.isSameDate(today, calendarItem.yangLi)) {
                                textViewNongLi.text = value.toString()
                                //                            textViewNongLi.setTextColor(Color.CYAN);
                                break
                            }
                        }

                        //
                        val currentDate = key.getDate()
                        if (religiousDayMap.containsKey(currentDate)) {
                            calendarItem.religious = religiousDayMap[currentDate] ?: ArrayList()
                        }

                        // 非戒期日
                        if (calendarItem.religious.size == 0) {
                            imageIsUnReligious.visibility = View.VISIBLE
                        } else if (findReligiousKeyWord(calendarItem.religious)) {
                            textViewNongLi.setTextColor(resources.getColor(R.color.month_text_color))
                        }
                        refreshInfoLayout(selectedDate)
                    }
                }

                enableButton()
            } catch (e: Exception) {
                _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
            } finally {
                progressBar_loading.visibility = View.GONE
            }
        }

        /**
         * onCancelled方法用于在取消执行中的任务时更改UI
         */
        override fun onCancelled() {
            super.onCancelled()
            progressBar_loading.visibility = View.VISIBLE
            progressBar_loading.progress = 0
        }
    }

    private fun log(log: String) {
        Log.e("wangsc", log)
    }

    /**
     * 事件 - 改变月份按钮
     */
    var btnCurrentMonth_OnClickListener = View.OnClickListener {
        try {
            showMonthPickerDialog(currentYear, currentMonth)
        } catch (ex: Exception) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, ex)
        }
    }

    fun showMonthPickerDialog(year: Int, month: Int) {
        val view = View.inflate(this@MainActivity, R.layout.inflate_date_picker_dialog, null)
        val dialog = AlertDialog.Builder(this@MainActivity).setView(view).create()
        dialog.setTitle("选择月份")
        val npYear = view.findViewById<View>(R.id.npYear) as NumberPicker
        val npMonth = view.findViewById<View>(R.id.npMonth) as NumberPicker
        val yearValues = arrayOfNulls<String>(Lunar.MaxYear - Lunar.MinYear + 1)
        for (i in yearValues.indices) {
            yearValues[i] = "${i + Lunar.MinYear}年"
        }
        val monthValues = arrayOfNulls<String>(12)
        for (i in monthValues.indices) {
            monthValues[i] = "${i + 1}月"
        }
        npYear.minValue = Lunar.MinYear
        npYear.maxValue = Lunar.MaxYear
        npYear.displayedValues = yearValues
        npYear.value = year
        npYear.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 禁止对话框打开后数字选择框被选中
        npMonth.minValue = 1
        npMonth.maxValue = 12
        npMonth.displayedValues = monthValues
        npMonth.value = month + 1
        npMonth.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 禁止对话框打开后数字选择框被选中
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "选择") { dialog, which ->
            val year = npYear.value
            val month = npMonth.value - 1
            val dateTime = DateTime()
            dateTime[year, month] = 1
            val maxDayOfMonth = dateTime.getActualMaximum(Calendar.DAY_OF_MONTH)
            val selectedDay = selectedDate.getDay()
            setSelectedDate(year, month, if (maxDayOfMonth < selectedDay) maxDayOfMonth else selectedDay)
            //                    refreshCalendarWithDialog();
            dialog.dismiss()
        }
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消") { dialog, which -> dialog.dismiss() }
        dialog.show()
    }

    /**
     * 事件 - 点击日历某天
     */
    private val userCalender_OnItemClickListener = OnItemClickListener { parent, view, position, id ->
        try {
            if (!calendarItemsMap.containsKey(position)) return@OnItemClickListener
            val calendarItem = calendarItemsMap[position]
            if (calendarItem != null) {
                if (todayPosition != -1) {
                    if (preSelectedPosition != -1 && preSelectedPosition != todayPosition) {
                        parent.getChildAt(preSelectedPosition).findViewById<View>(R.id.calendarItem_cvIsSelected).visibility = View.INVISIBLE
                    }
                    if (position != todayPosition) {
                        view.findViewById<View>(R.id.calendarItem_cvIsSelected).visibility = View.VISIBLE
                    }
                } else {
                    if (preSelectedPosition != -1) {
                        parent.getChildAt(preSelectedPosition).findViewById<View>(R.id.calendarItem_cvIsSelected).visibility = View.INVISIBLE
                    }
                    view.findViewById<View>(R.id.calendarItem_cvIsSelected).visibility = View.VISIBLE
                }
                preSelectedPosition = position

                //
                setSelectedDate(calendarItem.yangLi.getYear(), calendarItem.yangLi.getMonth(), calendarItem.yangLi.getDay())
            }
        } catch (e: Exception) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
        }
    }

    /**
     * 事件 - 返回今天
     */
    private val btnToday_OnClickListener = View.OnClickListener {
        try {
            if (preSelectedPosition != -1) {
                userCalender.getChildAt(preSelectedPosition).findViewById<View>(R.id.calendarItem_cvIsSelected).visibility = View.INVISIBLE
            }
            val today = DateTime.getToday()
            setSelectedDate(today.getYear(), today.getMonth(), today.getDay())
        } catch (e: Exception) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
        }
    }

    /**
     * 设置SelectedDate，并在修改该属性之后，重载自定义日历区域数据。
     *
     * @param year
     * @param month
     * @param day
     */
    fun setSelectedDate(year: Int, month: Int, day: Int) {
        try {
            var monthHasChanged = false

            // 如果新选中日期与当前月份不再同一月份，则刷新日历。
            if (year != currentYear || month != currentMonth) {
                monthHasChanged = true
            }
            selectedDate[year, month] = day

            // 判断是否刷新自定义日历区域
            if (monthHasChanged) {
                currentYear = year
                currentMonth = month
                refreshCalendar()
                //                refreshCalendarWithDialog(_String.concat("正在加载", currentYear, "年", currentMonth + 1, "月份", "戒期信息。"));
            }
            setYearMonthText()

            // “今”按钮是否显示
            val today = DateTime.getToday()
            if (year == today.getYear() && month == today.getMonth() && day == today.getDay()) {
                setTodayEnable(false)
            } else {
                setTodayEnable(true)
            }


            //
            refreshInfoLayout(selectedDate)
        } catch (e: Exception) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
        }
    }

    /**
     * 设置“回到今天”按钮是否可用。
     *
     * @param enable
     */
    private fun setTodayEnable(enable: Boolean) {
        try {
            uiHandler.post {
                if (enable) {
                    btn_today.visibility = View.VISIBLE
                    ib_leftMenu.visibility = View.INVISIBLE
                    stopRorateWan()
                } else {
                    btn_today.visibility = View.INVISIBLE
                    ib_leftMenu.visibility = View.VISIBLE
                    rorateWan()
                }
            }
        } catch (e: Exception) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
        }
    }

    private fun rorateWan() {
        AnimationUtils.setRorateAnimation(this@MainActivity, ib_leftMenu, 7000)
    }

    private fun stopRorateWan() {
        ib_leftMenu.clearAnimation()
    }

    /**
     * 小于10前追加‘0’
     *
     * @param x
     * @return
     */
    private fun format(x: Int): String {
        var s = "" + x
        if (s.length == 1) s = "0$s"
        return s
    }

    /**
     * 读取JAVA结构的二进制节气数据文件。
     *
     * @param resId 待读取的JAVA二进制文件。
     * @return
     * @throws IOException
     * @throws Exception
     */
    @Throws(IOException::class, Exception::class)
    private fun loadJavaSolarTerms(resId: Int): TreeMap<DateTime, SolarTerm> {
        val result = TreeMap<DateTime, SolarTerm>()
        try {
            val dis = DataInputStream(resources.openRawResource(resId))
            var date = dis.readLong()
            var solar = dis.readInt()
            try {
                while (true) {
                    val dt = DateTime()
                    dt.timeInMillis = date
                    val solarTerm = SolarTerm.Int2SolarTerm(solar)
                    result[dt] = solarTerm!!
                    date = dis.readLong()
                    solar = dis.readInt()
                }
            } catch (ex: EOFException) {
                dis.close()
            }
        } catch (e: NotFoundException) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
        } catch (e: Exception) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
        }
        // 按照KEY排序TreeMap
//        TreeMap<DateTime, SolarTerm> result = new TreeMap<DateTime, SolarTerm>(new Comparator<DateTime>() {
//            @Override
//            public int compare(DateTime lhs, DateTime rhs) {
//                return lhs.compareTo(rhs);
//            }
//        });
        return result
    }

    /**
     * 将C#导出的二进制文件转化为JAVA数据结构存储的二进制文件，并保存为/mnt/sdcard/solar300.dat。
     *
     * @param resId 待转化的C#二进制文件资源ID。
     * @throws IOException
     * @throws Exception
     */
    @Throws(IOException::class, Exception::class)
    private fun convertToJavafile(resId: Int) {
        try {
            val solarTermMap = loadCsharpSolarTerms(resId)
            val file = File("/mnt/sdcard/solar300.dat")
            if (!file.exists()) {
                file.createNewFile()
            }
            val fos = FileOutputStream(file)
            val dos = DataOutputStream(fos)
            val set: Set<*> = solarTermMap.entries
            val i = set.iterator()
            while (i.hasNext()) {
                val solar = i.next() as Map.Entry<DateTime, SolarTerm>
                dos.writeLong(solar.key.timeInMillis)
                dos.writeInt(solar.value.value)
            }
            dos.flush()
            dos.close()
            fos.close()
        } catch (e: Exception) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
        }
    }

    /**
     * 从C#导出的二进制文件获取节气数据。
     *
     * @param resId 资源文件ID
     * @return
     */
    @Throws(Exception::class)
    private fun loadCsharpSolarTerms(resId: Int): Map<DateTime, SolarTerm> {
        val solarTermMap: MutableMap<DateTime, SolarTerm> = HashMap()
        try {
            val stream = resources.openRawResource(resId)
            val longBt = ByteArray(8)
            val intBt = ByteArray(4)
            val nullBt = ByteArray(4)
            stream.read(longBt)
            stream.read(intBt)
            var cursor = stream.read(nullBt)
            while (cursor != -1) {
                val cal = DateTime()
                cal.timeInMillis = bytesToLong(longBt)
                val solar = bytesToInt(intBt)
                val solarTerm = SolarTerm.Int2SolarTerm(solar)
                solarTermMap[cal] = solarTerm!!
                stream.read(longBt)
                stream.read(intBt)
                cursor = stream.read(nullBt)
            }
            stream.close()
        } catch (e: NotFoundException) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
        } catch (e: Exception) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
        }
        return solarTermMap
    }

    /**
     * byte[] 转化为int。
     *
     * @param bytes 需要转化的byte[]
     * @return
     */
    fun bytesToInt(bytes: ByteArray): Int {
        return (bytes[0] and 0xFF.toByte()).toInt() or (bytes[1] and 0xFF.toByte()).toInt() shl (8) or (bytes[2] and 0xFF.toByte()).toInt() shl (16) or (bytes[3] and 0xFF.toByte()).toInt() shl (24)
    }

    private lateinit var buffer: ByteBuffer

    /**
     * byte[] 转化为long。
     *
     * @param bytes 需要转化的byte[]
     * @return
     */
    fun bytesToLong(bytes: ByteArray): Long {
        buffer = ByteBuffer.allocate(8)
        for (i in bytes.indices.reversed()) {
            buffer.put(bytes[i])
        }
        buffer.flip() //need flip
        return buffer.getLong()
    }

    override fun onBackPressed() {
        try {
            if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        } catch (ex: Exception) {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        menuItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    private fun menuItemSelected(menuItem: MenuItem): Boolean {
        val id = menuItem.itemId
        if (id == R.id.menu_settings) {
            startActivityForResult(Intent(this@MainActivity, SettingActivity::class.java), TO_SETTING_ACTIVITY)
        }
        //        else if (id == R.id.menu_select) {
//            try {
//                MonthPickerDialog monthPickerDialog = new MonthPickerDialog(currentYear, currentMonth);
//                monthPickerDialog.show();
//            } catch (Exception ex) {
//
//            }
//        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            when (requestCode) {
                TO_SETTING_ACTIVITY -> {
                    if (SettingActivity.isCalenderChanged) {
//                        refreshCalendarWithDialog("配置已更改，正在重新加载...");
                        isWeekendFirst = dc.getSetting(Setting.KEYS.is_weekend_first, true).getBoolean()
                        refreshCalendar()
                    }
                    if (SettingActivity.isRecordSetChanged) {
                        initRecordPart()
                    }
                }
                TO_SEXUAL_RECORD_ACTIVITY -> initRecordPart()
            }
        } catch (e: NumberFormatException) {
            _Utils.printExceptionSycn(this@MainActivity, uiHandler, e)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun check(npYear: NumberPicker, npMonth: NumberPicker, npDay: NumberPicker, npHour: NumberPicker) {

        val now = DateTime()
        if (npYear.value == now.getYear()) {
            npMonth.maxValue = now.getMonth() + 1
        } else {
            npMonth.maxValue = 12
        }
        if (npYear.value == now.getYear() && npMonth.value == now.getMonth() + 1) {
            npDay.maxValue = now.getDay()
        } else {
            val selected = DateTime(npYear.value, npMonth.value - 1, 1)
            val max = selected.getActualMaximum(Calendar.DAY_OF_MONTH)
            val day = npDay.value
            npDay.maxValue = max
            if (day > max) {
                npDay.value = 1
            } else {
                npDay.value = day
            }
        }
        if (npYear.value == now.getYear() && npMonth.value == now.getMonth() + 1 && npDay.value == now.getDay()) {
            npHour.maxValue = now.getHour()
        } else {
            npHour.maxValue = 23
        }
    }

    fun showAddSexualDayDialog() {
        val view = View.inflate(this@MainActivity, R.layout.inflate_dialog_date_picker, null)
        val dialog = AlertDialog.Builder(this@MainActivity).setView(view).create()
        dialog.setTitle("上一次行房日期")
        val now = DateTime()
        val year = now.getYear()
        val month = now.getMonth()
        //        int maxDay = dateTime.getActualMaximum(Calendar.DAY_OF_MONTH);
        val day = now.getDay()
        val hour = now.getHour()
        val yearNumbers = arrayOfNulls<String>(3)
        for (i in year - 2..year) {
            yearNumbers[i - year + 2] = "${i}年"
        }
        val monthNumbers = arrayOfNulls<String>(12)
        for (i in 0..11) {
            monthNumbers[i] = "${i + 1}月"
        }
        val dayNumbers = arrayOfNulls<String>(31)
        for (i in 0..30) {
            dayNumbers[i] = "${i + 1}日"
        }
        val hourNumbers = arrayOfNulls<String>(24)
        for (i in 0..23) {
            hourNumbers[i] = "${i}点"
        }
        val npYear = view.findViewById<View>(R.id.npYear) as NumberPicker
        val npMonth = view.findViewById<View>(R.id.npMonth) as NumberPicker
        val npDay = view.findViewById<View>(R.id.npDay) as NumberPicker
        val npHour = view.findViewById<View>(R.id.npHour) as NumberPicker
        npYear.minValue = year - 2
        npYear.maxValue = year
        npYear.value = year
        npYear.displayedValues = yearNumbers
        npYear.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 禁止对话框打开后数字选择框被选中
//        npYear.wrapSelectorWheel=false
        npMonth.minValue = 1
        npMonth.maxValue = 12
        npMonth.displayedValues = monthNumbers
        npMonth.value = month + 1
        npMonth.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 禁止对话框打开后数字选择框被选中
//        npMonth.wrapSelectorWheel=false
        npDay.minValue = 1
        npDay.maxValue = 31
        npDay.displayedValues = dayNumbers
        npDay.value = day
        npDay.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 禁止对话框打开后数字选择框被选中
//        npDay.wrapSelectorWheel=false
        npHour.minValue = 0
        npHour.maxValue = 23
        npHour.displayedValues = hourNumbers
        npHour.value = hour
        npHour.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 禁止对话框打开后数字选择框被选中
//        npHour.wrapSelectorWheel=false


        npMonth.maxValue = now.getMonth() + 1
        npDay.maxValue = now.getDay()
        npHour.maxValue = now.getHour()

        npYear.setOnValueChangedListener { picker, oldVal, newVal ->
            check(npYear, npMonth, npDay, npHour)
//            if(npYear.value==now.getYear()){
//                npMonth.maxValue = now.getMonth()+1
//            }else{
//                npMonth.maxValue = 12
//            }
        }
        npMonth.setOnValueChangedListener { picker, oldVal, newVal ->
            check(npYear, npMonth, npDay, npHour)
        }
        npDay.setOnValueChangedListener { picker, oldVal, newVal ->
            check(npYear, npMonth, npDay, npHour)
        }
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定") { dialog, which ->
            try {
                val year = npYear.value
                val month = npMonth.value - 1
                val day = npDay.value
                val hour = npHour.value
                val selectedDateTime = DateTime(year, month, day, hour, 0, 0)
                val sexualDay = SexualDay(selectedDateTime, "", "")
                dc.addSexualDay(sexualDay)
                initRecordPart()
                dialog.dismiss()
            } catch (e: Exception) {
                _Utils.printException(this@MainActivity, e)
            }
        }
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消") { dialog, which ->
            try {
                dialog.dismiss()
            } catch (e: Exception) {
                _Utils.printException(this@MainActivity, e)
            }
        }
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "记录页面") { dialog, which ->
            try {
                if (dc.getLastSexualDay() != null) {
                    startActivityForResult(Intent(this@MainActivity, SexualDayRecordActivity::class.java), TO_SEXUAL_RECORD_ACTIVITY)
                } else {
                    AlertDialog.Builder(this@MainActivity).setMessage("当前没有记录！").show()
                }
            } catch (e: Exception) {
                _Utils.printException(this@MainActivity, e)
            }
        }
        dialog.show()
    }

    companion object {
        private const val INFO_TEXT_SIZE = 14
        private const val TO_SEXUAL_RECORD_ACTIVITY = 298
        const val TO_SETTING_ACTIVITY = 1
    }
}
