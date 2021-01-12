package com.wang17.religiouscalendar.activity;

import android.animation.AnimatorInflater;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wang17.religiouscalendar.R;
import com.wang17.religiouscalendar.emnu.SolarTerm;
import com.wang17.religiouscalendar.util.AnimationUtils;
import com.wang17.religiouscalendar.util.CalendarHelper;
import com.wang17.religiouscalendar.util.GanZhi;
import com.wang17.religiouscalendar.util.Lunar;
import com.wang17.religiouscalendar.util.Religious;
import com.wang17.religiouscalendar.util._Utils;
import com.wang17.religiouscalendar.util._Session;
import com.wang17.religiouscalendar.util._String;
import com.wang17.religiouscalendar.model.CalendarItem;
import com.wang17.religiouscalendar.model.DataContext;
import com.wang17.religiouscalendar.model.DateTime;
import com.wang17.religiouscalendar.model.ReligiousCallBack;
import com.wang17.religiouscalendar.model.Setting;
import com.wang17.religiouscalendar.model.SexualDay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int INFO_TEXT_SIZE = 14;
    // 视图变量
    private View headerView;
    private TextView tvGanzhi, tvNongLi, tvYear, tvFo, tvToday, tvMonth, tvChijie1, tvChijie2;
    private CalenderGridAdapter calendarAdapter;
    private ImageButton ibLeftMenu, ibSettting;
    private ImageView ivBanner, ivWelcome;
    private DrawerLayout drawer;
    private LinearLayout layout_religious;
    private GridView userCalender;
    private PopupWindow mPopWindow;
    private LinearLayout layoutJinJi, layoutJyw, layoutYgx, layoutRecord, layoutWelcome;
    private ProgressBar pbLoading;
    // 类变量
    private DataContext dataContext;
    private Typeface fontHWZS, fontGF;
    private DateTime selectedDate;
    private RefreshCalendarTask refreshCalendarTask;
    // 值变量
    Animation animation;
    private int calendarItemLength, preSelectedPosition, todayPosition, currentYear, currentMonth;
    private long xxxTimeMillis;
    private boolean isFirstTime, isShowRecords;
    private Map<Integer, CalendarItem> calendarItemsMap;
    private TreeMap<DateTime, SolarTerm> solarTermMap;
    private Map<DateTime, SolarTerm> currentMonthSolarTermMap;
    private Map<DateTime, View> calendarItemViewsMap;
    private HashMap<DateTime, String> religiousDayMap, remarkMap;
    private Handler uiHandler;
    private static final int TO_SEXUAL_RECORD_ACTIVITY = 298;
    public static final int TO_SETTING_ACTIVITY = 1;

    public CalenderHeaderGridAdapter calenderHeaderGridAdapter;
    private boolean isWeekendFirst;

    private int welcomeDurationIndex;

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            DataContext context = new DataContext(this);


            isWeekendFirst = context.getSetting(Setting.KEYS.is_weekend_first, true).getBoolean();
            ArrayList<Integer> pics = new ArrayList<>();

            xxxTimeMillis = System.currentTimeMillis();
            uiHandler = new Handler();
            dataContext = new DataContext(MainActivity.this);
            isFirstTime = true;


            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();


            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            headerView = navigationView.getHeaderView(0);
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {

                    menuItemSelected(item);
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
            });

            //region 启动界面

            layoutWelcome = (LinearLayout) findViewById(R.id.layout_welcome);
            ivWelcome = (ImageView) findViewById(R.id.imageView_welcome);
            welcomeDurationIndex = dataContext.getSetting(Setting.KEYS.welcome_duration, 1).getInt();
            if (welcomeDurationIndex == 0) {
                ivWelcome.setVisibility(View.INVISIBLE);
            } else {
                ivWelcome.setVisibility(View.VISIBLE);
                int itemPosition = Integer.parseInt(dataContext.getSetting(Setting.KEYS.welcome, 0).getValue());
                if (itemPosition >= _Session.welcomes.size()) {
                    itemPosition = 0;
                    dataContext.editSetting(Setting.KEYS.welcome, itemPosition + "");
                }
                ivWelcome.setImageResource(_Session.welcomes.get(itemPosition).getResId());
            }

            //endregion


            //
            solarTermMap = loadJavaSolarTerms(R.raw.solar_java_50);

            //
            initViews();

        } catch (Exception ex) {
            _Utils.printExceptionSycn(MainActivity.this, uiHandler, ex);
        }
    }

    //region 事件
    View.OnClickListener leftMenuClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
        }
    };
    View.OnLongClickListener leftMenuLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
            startActivityForResult(new Intent(MainActivity.this, SettingActivity.class), TO_SETTING_ACTIVITY);
            return true;
        }
    };
    View.OnClickListener settingClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
            startActivityForResult(new Intent(MainActivity.this, SettingActivity.class), TO_SETTING_ACTIVITY);
        }
    };
    //endregion

    /**
     * 六字名号呼吸效果。
     */
    private void nianfo(View target) throws Exception {
        ObjectAnimator objectAnimator = (ObjectAnimator) AnimatorInflater.loadAnimator(MainActivity.this, R.animator.color_animator);
        objectAnimator.setEvaluator(new ArgbEvaluator());
        objectAnimator.setTarget(target);
        objectAnimator.start();
    }

    private void setTextForRecord(String text1, String text2) {
        if (!text2.isEmpty()) {
            text1 = "已持戒：" + text1;
            text2 = text2 + "后，元气恢复。";
        }
        tvChijie1.setText(text1);
        tvChijie2.setText(text2);
    }

    /**
     * 方法 - 初始化所有变量
     */
    private void initViews() {
        try {
            calendarItemViewsMap = new HashMap<>();
            currentMonthSolarTermMap = new HashMap<>();
            religiousDayMap = new HashMap<>();
            remarkMap = new HashMap<>();
            refreshCalendarTask = new RefreshCalendarTask();

            //region 持戒记录功能设置
            layoutRecord = headerView.findViewById(R.id.layout_record);
            tvChijie1 = headerView.findViewById(R.id.textView_chijie);
            tvChijie2 = headerView.findViewById(R.id.textView_chijie2);

            initRecordPart();


            //endregion


            //region 左侧菜单操作
            layoutYgx = headerView.findViewById(R.id.layout_ygx);
            layoutYgx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, IntroduceActivity.class);
                    intent.putExtra(IntroduceActivity.PARAM_NAME, IntroduceActivity.ItemName.印光大师序.toString());
                    startActivity(intent);
                }
            });
            layoutJinJi = headerView.findViewById(R.id.layout_jinji);
            layoutJinJi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, IntroduceActivity.class);
                    intent.putExtra(IntroduceActivity.PARAM_NAME, IntroduceActivity.ItemName.天地人禁忌.toString());
                    startActivity(intent);
                }
            });
            layoutJyw = headerView.findViewById(R.id.layout_jyw);
            layoutJyw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, IntroduceActivity.class);
                    intent.putExtra(IntroduceActivity.PARAM_NAME, IntroduceActivity.ItemName.文昌帝君戒淫文.toString());
                    startActivity(intent);
                }
            });
            //endregion


//            imageLeft = (ImageView) findViewById(R.id.image_left);
//            imageLeft.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    DateTime now = selectedDate.addMonths(-1);
//                    DateTime dateTime = new DateTime();
//                    dateTime.set(now.getYear(), now.getMonth(), 1);
//                    int maxDayOfMonth = dateTime.getActualMaximum(Calendar.DAY_OF_MONTH);
//                    int selectedDay = MainActivity.this.selectedDate.getDay();
//                    setSelectedDate(now.getYear(), now.getMonth(), maxDayOfMonth < selectedDay ? maxDayOfMonth : selectedDay);
//                }
//            });
//
//            imageRight = (ImageView) findViewById(R.id.image_right);
//            imageRight.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    DateTime now = selectedDate.addMonths(1);
//                    DateTime dateTime = new DateTime();
//                    dateTime.set(now.getYear(), now.getMonth(), 1);
//                    int maxDayOfMonth = dateTime.getActualMaximum(Calendar.DAY_OF_MONTH);
//                    int selectedDay = MainActivity.this.selectedDate.getDay();
//                    setSelectedDate(now.getYear(), now.getMonth(), maxDayOfMonth < selectedDay ? maxDayOfMonth : selectedDay);
//                }
//            });


            pbLoading = (ProgressBar) findViewById(R.id.progressBar_loading);

            //
            int itemPosition = 0;
            itemPosition = Integer.parseInt(dataContext.getSetting(Setting.KEYS.banner, itemPosition).getValue());
            if (itemPosition >= _Session.banners.size()) {
                itemPosition = 0;
                dataContext.editSetting(Setting.KEYS.banner, itemPosition);
            }

            // 加载include_main_banner
            ivBanner = (ImageView) findViewById(R.id.imageView_banner);
            ivBanner.setImageResource(_Session.banners.get(itemPosition).getResId());
            ivBanner.setOnClickListener(new View.OnClickListener() {//2130903043
                @Override
                public void onClick(View v) {
                    showPopupWindow();
                }
            });

            ibLeftMenu = (ImageButton) findViewById(R.id.ib_leftMenu);
            rorateWan();
            ibSettting = headerView.findViewById(R.id.imageButton_setting);

            ibLeftMenu.setOnClickListener(leftMenuClick);
            ibLeftMenu.setOnLongClickListener(leftMenuLongClick);
            ibSettting.setOnClickListener(settingClick);
            ibSettting.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return true;
                }
            });


            AssetManager mgr = getAssets();//得到AssetManager
            fontHWZS = Typeface.createFromAsset(mgr, "fonts/STZHONGS.TTF");
            fontGF = Typeface.createFromAsset(mgr, "fonts/GONGFANG.ttf");


            tvFo = (TextView) findViewById(R.id.tvfo);
//            textViewFo.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
//            ((TextView)findViewById(R.id.textView_banner_text)).setTypeface(fontHWZS);
//            textViewFo.getPaint().setFakeBoldText(true);
            //
            nianfo(tvFo);

            // selectedDate
            selectedDate = DateTime.getToday();
            currentYear = selectedDate.getYear();
            currentMonth = selectedDate.getMonth();

            // buttonToday
            tvToday = (TextView) findViewById(R.id.btn_today);
            tvToday.setTypeface(fontGF);
            tvToday.setOnClickListener(btnToday_OnClickListener);

//            textViewToday = (TextView) findViewById(R.id.textView_today);
//            textViewToday.setTypeface(fontGF);

            // 信息栏
//            yearMonth = (TextView) findViewById(R.id.tvYearMonth);
//            yangliBig = (TextView) findViewById(R.id.tvYangLiBig);
            tvMonth = (TextView) findViewById(R.id.button_month);
            tvMonth.setOnClickListener(btnCurrentMonth_OnClickListener);


            final Button buttonQuickMonth = (Button) findViewById(R.id.button_quick_month);
            buttonQuickMonth.setText(currentMonth + 1 + "月");
            buttonQuickMonth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectedDay = selectedDate.getDay();
                    DateTime dateTime = new DateTime(currentYear, currentMonth, selectedDay);
                    dateTime = dateTime.addMonths(1);
                    setSelectedDate(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay());
                    buttonQuickMonth.setText(currentMonth + 1 + "月");
//                    }else{
//                        dateTime = dateTime.addMonths(-1);
//                        setSelectedDate(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay());
//                        buttonQuickMonth.setText(currentMonth+1+"月");
//                    }
                }
            });

            tvYear = (TextView) findViewById(R.id.textView_year);
            tvYear.setOnClickListener(btnCurrentMonth_OnClickListener);
            tvGanzhi = (TextView) findViewById(R.id.textView_ganZhi);

            tvNongLi = (TextView) findViewById(R.id.textView_nongLi);
            layout_religious = (LinearLayout) findViewById(R.id.linearReligious);


            tvYear.setTypeface(fontGF);
//            tvYear.getPaint().setFakeBoldText(true);
            tvMonth.setTypeface(fontGF);
//            tvMonth.getPaint().setFakeBoldText(true);
            tvGanzhi.setTypeface(fontHWZS);
            tvGanzhi.getPaint().setFakeBoldText(true);
            tvNongLi.setTypeface(fontHWZS);
            tvNongLi.getPaint().setFakeBoldText(true);
            tvFo.setTypeface(fontGF);
//            tvFo.getPaint().setFakeBoldText(true);

            // calendarAdapter
            calendarAdapter = new CalenderGridAdapter();
            calendarItemsMap = new HashMap<Integer, CalendarItem>();

            setYearMonthText();

            // btnCurrentMonth
//            btnCurrentMonth = (Button) findViewById(R.id.btnChangeMonth);
//            btnCurrentMonth.setOnClickListener(btnCurrentMonth_OnClickListener);

            // userCalender
            userCalender = (GridView) findViewById(R.id.userCalender);
            userCalender.setOnItemClickListener(userCalender_OnItemClickListener);
            GridView calendarHeader = (GridView) findViewById(R.id.userCalenderHeader);
            calenderHeaderGridAdapter = new CalenderHeaderGridAdapter();
            calendarHeader.setAdapter(calenderHeaderGridAdapter); // 添加星期标头

            // 填充日历
            new Thread(new Runnable() {
                @Override
                public void run() {
                    refreshCalendar();
                }
            }).start();
        } catch (Exception ex) {
            _Utils.printExceptionSycn(MainActivity.this, uiHandler, ex);
        }
    }

    private void setYearMonthText() {
        tvMonth.setText(currentMonth + 1 + "月");
        tvYear.setText(_String.concat(currentYear, "年"));
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

    private void initRecordPart() {

        try {
            layoutRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddSexualDayDialog();
                }
            });

            layoutRecord.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    try {
                        if (dataContext.getLastSexualDay() != null) {
                            startActivityForResult(new Intent(MainActivity.this, SexualDayRecordActivity.class), TO_SEXUAL_RECORD_ACTIVITY);
                        } else {
                            new AlertDialog.Builder(MainActivity.this).setMessage("当前没有记录！").show();
                        }
                    } catch (Exception e) {
                        _Utils.printException(MainActivity.this, e);
                    }
                    return true;
                }
            });

            if (dataContext.getSetting(Setting.KEYS.targetAuto, true).getBoolean() == false) {
                dataContext.editSetting(Setting.KEYS.recordIsOpened, false);
                dataContext.editSetting(Setting.KEYS.targetAuto, true);
                dataContext.deleteSetting(Setting.KEYS.targetInHour);
//                new AlertDialog.Builder(this).setMessage("系统移除了自定义行房周期功能，请到设置界面设置出生日期，再使用此功能。").setNegativeButton("知道了", null).show();
//                return;
            }
            isShowRecords = Boolean.parseBoolean(dataContext.getSetting(Setting.KEYS.recordIsOpened, false).getValue());
            if (isShowRecords) {
                layoutRecord.setVisibility(View.VISIBLE);
                SexualDay lastSexualDay = dataContext.getLastSexualDay();

                int targetInHour = 0;
                if (lastSexualDay != null) {
                    targetInHour = _Utils.getTargetInHour(dataContext.getSetting(Setting.KEYS.birthday).getDateTime());
                    int haveInHour = (int) ((System.currentTimeMillis() - lastSexualDay.getDateTime().getTimeInMillis()) / 3600000);
                    int leaveInHour = targetInHour - haveInHour;

                    if (leaveInHour > 0) {
                        setTextForRecord(DateTime.toSpanString(haveInHour), DateTime.toSpanString(leaveInHour));
                    } else {
                        leaveInHour *= -1;
                        setTextForRecord(DateTime.toSpanString(haveInHour), "+" + DateTime.toSpanString(leaveInHour));
                    }
                    tvChijie2.setVisibility(View.VISIBLE);
                } else {
                    setTextForRecord("点击添加记录", "");
                    tvChijie2.setVisibility(View.GONE);
                }

            } else {
                layoutRecord.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            _Utils.printException(MainActivity.this, e);
        }
    }


    private void showPopupWindow() {
        //设置contentView
        View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popup_window, null);
        mPopWindow = new PopupWindow(contentView, DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(contentView);
        //设置各个控件的点击响应
        TextView tv0 = contentView.findViewById(R.id.item00);
        TextView tv1 = contentView.findViewById(R.id.item01);
        TextView tv2 = contentView.findViewById(R.id.item02);
        TextView tv3 = contentView.findViewById(R.id.item03);
        TextView tv4 = contentView.findViewById(R.id.item04);
        tv0.setOnClickListener(this);
        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        tv3.setOnClickListener(this);
        tv4.setOnClickListener(this);
        //显示PopupWindow
//        View rootview = (View)findViewById(R.id.layout_upper_banner);
//        mPopWindow.showAtLocation(rootview,);

        View rootview = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main, null);
        mPopWindow.showAtLocation(rootview, Gravity.RIGHT | Gravity.TOP, 0, 0);

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        try {
            switch (id) {
                case R.id.item00: {
                    dataContext.editSetting(Setting.KEYS.banner, 0);
                    ivBanner.setImageResource(_Session.banners.get(0).getResId());
                }
                break;
                case R.id.item01: {
                    dataContext.editSetting(Setting.KEYS.banner, 1);
                    ivBanner.setImageResource(_Session.banners.get(1).getResId());
                }
                break;
                case R.id.item02: {
                    dataContext.editSetting(Setting.KEYS.banner, 2);
                    ivBanner.setImageResource(_Session.banners.get(2).getResId());
                }
                break;
                case R.id.item03: {
                    dataContext.editSetting(Setting.KEYS.banner, 3);
                    ivBanner.setImageResource(_Session.banners.get(3).getResId());
                }
                break;
                case R.id.item04: {
                    dataContext.editSetting(Setting.KEYS.banner, 4);
                    ivBanner.setImageResource(_Session.banners.get(4).getResId());
                }
                break;
            }
            mPopWindow.dismiss();
        } catch (Exception ex) {
            _Utils.printExceptionSycn(MainActivity.this, uiHandler, ex);
        }
    }

    /**
     * 自定义日历标头适配器
     */
    protected class CalenderHeaderGridAdapter extends BaseAdapter {
        private String[] header;

        public CalenderHeaderGridAdapter() {
            if (isWeekendFirst) {
                this.header = new String[]{"日", "一", "二", "三", "四", "五", "六"};
            } else {
                this.header = new String[]{"一", "二", "三", "四", "五", "六", "日"};
            }
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView mTextView = new TextView(getApplicationContext());
            mTextView.setText(header[position]);
            mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            mTextView.getPaint().setFakeBoldText(true);
            mTextView.setTypeface(Typeface.MONOSPACE);
            mTextView.setTextColor(Color.parseColor("#000000"));
            mTextView.setWidth(60);
            return mTextView;
        }

        @Override
        public void notifyDataSetChanged() {
            if (isWeekendFirst) {
                this.header = new String[]{"日", "一", "二", "三", "四", "五", "六"};
            } else {
                this.header = new String[]{"一", "二", "三", "四", "五", "六", "日"};
            }
            super.notifyDataSetChanged();
        }
    }

    // FIXME: 2020/5/30 周六排序错误的问题，日历排版有没有BUG？
    /**
     * 自定义日历适配器
     */
    protected class CalenderGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return calendarItemLength;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.inflat_calender_item, null);
            try {
                convertView = View.inflate(MainActivity.this, R.layout.inflat_calender_item, null);
                TextView textViewYangLi = convertView.findViewById(R.id.calenderItem_tv_YangLiDay);
                TextView textViewNongLi = convertView.findViewById(R.id.calendarItem_tv_NongLiDay);
                ImageView imageIsToday = convertView.findViewById(R.id.calendarItem_cvIsToday);
                ImageView imageIsSelected = convertView.findViewById(R.id.calendarItem_cvIsSelected);
                if (calendarItemsMap.containsKey(position)) {
                    CalendarItem calendarItem = calendarItemsMap.get(position);
                    DateTime today = DateTime.getToday();
                    textViewYangLi.setText(calendarItem.getYangLi().get(DateTime.DAY_OF_MONTH) + "");

                    // 农历月初，字体设置。
                    if (calendarItem.getNongLi().getDay() == 1) {
                        if (calendarItem.getNongLi().isLeap())
                            textViewNongLi.setText("闰" + calendarItem.getNongLi().getMonthStr());
                        else
                            textViewNongLi.setText(calendarItem.getNongLi().getMonthStr());

                        textViewNongLi.setTextColor(Color.BLACK);
                        textViewNongLi.getPaint().setFakeBoldText(true);
                    } else {
                        textViewNongLi.setText(calendarItem.getNongLi().getDayStr());
                    }

                    // 今天
                    if (today.compareTo(calendarItem.getYangLi().getDate()) == 0) {
                        imageIsToday.setVisibility(View.VISIBLE);
                        textViewYangLi.setTextColor(Color.WHITE);
                        textViewNongLi.setTextColor(Color.WHITE);
                        todayPosition = position;
                    }

                    // 选中的日期
                    if (CalendarHelper.isSameDate(calendarItem.getYangLi(), selectedDate) && !CalendarHelper.isSameDate(calendarItem.getYangLi(), today)) {
                        imageIsSelected.setVisibility(View.VISIBLE);
                        preSelectedPosition = position;
                    }
                    calendarItemViewsMap.put(calendarItem.getYangLi(), convertView);
                } else {
                    textViewYangLi.setText("");
                    textViewNongLi.setText("");
                }
            } catch (Exception e) {
                _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
            }
            return convertView;
        }
    }


    private int findReligiousKeyWord(String religious) {
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
                || religious.contains("夺纪")) {
            return 1;
        }
        return 0;
    }

    /**
     * 以星期日为1，星期一为2，以此类推。
     *
     * @param day
     * @return
     */
    private String Convert2WeekDay(int day) throws Exception {
        switch (day) {
            case 1:
                return "星期日";
            case 2:
                return "星期一";
            case 3:
                return "星期二";
            case 4:
                return "星期三";
            case 5:
                return "星期四";
            case 6:
                return "星期五";
            case 7:
                return "星期六";
        }
        return "";
    }

    /**
     * 更新信息栏（农历，干支，戒期信息），一定要在当前月份的日历界面载入完毕后在引用此方法。
     * 因为此方法数据调用calendarItemsMap，而calendarItemsMap是在形成当月数据时形成。
     *
     * @param seletedDateTime 当前选中的日期
     */
    private void refreshInfoLayout(DateTime seletedDateTime) {
        try {
            Log.e("wangsc", "刷新信息板：" + seletedDateTime.toLongDateString());
            if (calendarItemsMap.size() == 0) return;

            CalendarItem calendarItem = null;
            for (Map.Entry<Integer, CalendarItem> entity : calendarItemsMap.entrySet()) {
                if (CalendarHelper.isSameDate(entity.getValue().getYangLi(), seletedDateTime)) {
                    calendarItem = entity.getValue();
                }
            }


            if (calendarItem == null) return;
//        yearMonth.setText(currentYear + "." + format(currentMonth + 1));
//        yangliBig.setText(seletedDateTime.getDay() + "");
//            textViewYear.setText(_String.concat(calendarItem.getYangLi().getYear(), "年"));

            try {
                GanZhi gz = new GanZhi(calendarItem.getYangLi(), this.solarTermMap);
                tvGanzhi.setText(_String.concat("[", gz.getZodiac(), "]",gz.getTianGanYear(), gz.getDiZhiYear(), "年",
                        gz.getTianGanMonth(), gz.getDiZhiMonth(), "月",
                        gz.getTianGanDay(), gz.getDiZhiDay(), "日"));
                tvNongLi.setText(_String.concat(calendarItem.getNongLi().getMonthStr(), calendarItem.getNongLi().getDayStr()));
            } catch (Exception ex) {
                _Utils.printExceptionSycn(MainActivity.this, uiHandler, ex);
            }

            layout_religious.removeAllViews();

            boolean haveReligious = calendarItem.getReligious() != null;
            boolean haveRemarks = calendarItem.getRemarks() != null;
            if (haveReligious) {
                String[] religious = calendarItem.getReligious().split("\n");
                int i = 1;
                for (String str : religious) {
                    View view = View.inflate(MainActivity.this, R.layout.inflate_targ_religious, null);


                    TextView tv = (TextView) view.findViewById(R.id.textView_religious);
                    tv.setText(str);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,INFO_TEXT_SIZE);
                    tv.getPaint().setFakeBoldText(true);
                    tv.setTypeface(fontHWZS);
                    if (findReligiousKeyWord(str) == 1) {
                        tv.setTextColor(getResources().getColor(R.color.month_text_color));
                    }

                    layout_religious.addView(view);
                }
            }
            if (haveRemarks) {
                String[] remarks = calendarItem.getRemarks().split("\n");
                int i = 1;

                for (String str : remarks) {
                    View view = View.inflate(MainActivity.this, R.layout.inflate_targ_note, null);

                    TextView tv = (TextView) view.findViewById(R.id.textView_note);
                    tv.setText(str);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,INFO_TEXT_SIZE);
                    tv.getPaint().setFakeBoldText(true);
                    tv.setTypeface(fontHWZS);

                    layout_religious.addView(view);
                }
            }
        } catch (Exception e) {
            _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
        }
    }

    private static final String _TAG = "wangsc";

    /**
     * 得到指定日期在日历中的position。
     *
     * @param dateTime
     * @return
     */
    private int dateTimeToPosition(DateTime dateTime, boolean tag) {
        /**
         * DAY_OF_WEEK：当前周的第几天。从 1 开始。
         * WEEK_OF_MONTH：当前月的第几个星期。从 1 开始。
         *
         * 周日1；  周一2； 周二3；  周三4；  周四5；  周五6；  周六7；
         *                                          1       2
         *  3       4       5       6       7       8       9
         *  10      11      12      13      14      15      16
         *  17      18      19      20      21      22      23
         *  24      25      26      27      28      29      30
         *  31
         */
        if (isWeekendFirst) {
            int week = dateTime.get(DateTime.WEEK_OF_MONTH);
            int day_week = dateTime.get(DateTime.DAY_OF_WEEK);
            return (week - 1) * 7 + day_week - 1;
        } else {
            // FIXME: 2020/5/29 周六偏差
            /**
             * DAY_OF_WEEK：当前周的第几天。从1开始。
             * WEEK_OF_MONTH：当前月的第几个星期。从1开始。
             *
             *   周一1；  周二2； 周三3；  周四4；  周五5；  周六6；  周日7；
             *                                    1       2       3
             *     4      5       6       7       8       9       10
             *     11     12      13      14      15      16      17
             *     18     19      20      21      22      23      24
             *     25     26      27      28      29      30      31
             */
            int week = dateTime.get(DateTime.WEEK_OF_MONTH);
            int day_week = dateTime.get(DateTime.DAY_OF_WEEK) - 1;
            if (day_week == 0) {
                day_week = 7;
                week--;
                if (week == 0) {
                    tag = true;
                }
            }
            if (tag) {
                week++;
            }
            return (week - 1) * 7 + day_week - 1;
        }
    }

    /**
     * 刷新日历界面，使用此方法必须标明forAsynch变量。
     *
     * @throws Exception
     */
    private void refreshCalendar() {
        try {
            refreshCalendarTask.cancel(true);
            calendarItemsMap.clear();
            calendarItemViewsMap.clear();
            currentMonthSolarTermMap.clear();
            religiousDayMap.clear();
            remarkMap.clear();

            int maxDayInMonth = 0;
            DateTime tmpToday = new DateTime(currentYear, currentMonth, 1);
            calendarItemLength = maxDayInMonth = tmpToday.getActualMaximum(DateTime.DAY_OF_MONTH);

            /**
             * calendarItemLength首先由上面的语句设置为当月天数。然后再由下面的语句，把每个月日历前面空白的几天加上。
             */
            if (isWeekendFirst) {
                int day_week = tmpToday.get(DateTime.DAY_OF_WEEK);
                calendarItemLength += day_week - 1;
            } else {
                int day_week = tmpToday.get(DateTime.DAY_OF_WEEK) - 1;
                if (day_week == 0)
                    day_week = 7;
                calendarItemLength += day_week - 1;
            }

            // “今”按钮是否显示
            DateTime today = DateTime.getToday();
            if (selectedDate.compareTo(today) == 0) {
                setTodayEnable(false);
            } else {
                setTodayEnable(true);
            }

            // FIXME: 2020/5/29 周六偏差
            // 得到填充日历控件所需要的数据
            boolean tag = false;
            for (int i = 1; i <= maxDayInMonth; i++) {
                int week = tmpToday.get(DateTime.WEEK_OF_MONTH);
                if (isWeekendFirst) {
                    /**
                     * DAY_OF_WEEK：当前周的第几天。从1开始。
                     * WEEK_OF_MONTH：当前月的第几个星期。从1开始。
                     *
                     * 周日1；  周一2； 周二3；  周三4；  周四5；  周五6；  周六7；
                     *                                          1       2
                     *  3       4       5       6       7       8       9
                     *  10      11      12      13      14      15      16
                     *  17      18      19      20      21      22      23
                     *  24      25      26      27      28      29      30
                     *  31
                     */
                    int day_week = tmpToday.get(DateTime.DAY_OF_WEEK);
                    int key = (week - 1) * 7 + day_week - 1;
                    DateTime newCalendar = new DateTime();
                    newCalendar.setTimeInMillis(tmpToday.getTimeInMillis());
                    CalendarItem item = new CalendarItem(newCalendar);
                    calendarItemsMap.put(key, item);
                } else {
                    /**
                     * DAY_OF_WEEK：当前周的第几天。从1开始。
                     * WEEK_OF_MONTH：当前月的第几个星期。从1开始。
                     *
                     *   周一1；  周二2； 周三3；  周四4；  周五5；  周六6；  周日7；
                     *                                    1       2       3
                     *     4      5       6       7       8       9       10
                     *     11     12      13      14      15      16      17
                     *     18     19      20      21      22      23      24
                     *     25     26      27      28      29      30      31
                     */
                    int day_week = tmpToday.get(DateTime.DAY_OF_WEEK) - 1;
                    if (day_week == 0) {
                        day_week = 7;
                        week--;
                        if (week == 0) {
                            tag = true;
                        }
                    }
                    if (tag) {
                        week++;
                    }
                    int key = (week - 1) * 7 + day_week - 1;
                    DateTime newCalendar = new DateTime();
                    newCalendar.setTimeInMillis(tmpToday.getTimeInMillis());
                    CalendarItem item = new CalendarItem(newCalendar);
                    calendarItemsMap.put(key, item);
                }
                tmpToday.add(DateTime.DAY_OF_MONTH, 1);
            }

            // 填充日历控件
            todayPosition = -1;
            preSelectedPosition = -1;


            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        userCalender.setAdapter(calendarAdapter);
                        refreshCalendarTask = new RefreshCalendarTask();
                        refreshCalendarTask.execute();
                        calenderHeaderGridAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
                    }
                }
            });
            if (isFirstTime && welcomeDurationIndex != 0) {
                int duration = _Session.duration[dataContext.getSetting(Setting.KEYS.welcome_duration, 1).getInt()];
                Log.i("wangsc", "duration: " + duration);
                long span = duration - (System.currentTimeMillis() - xxxTimeMillis);
                if (span > 0) {
                    try {
                        Thread.sleep(span);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isFirstTime = false;
            }
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        layoutWelcome.setVisibility(View.INVISIBLE);
                    } catch (Exception e) {
                        _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
                    }
                }
            });
        } catch (NumberFormatException e) {
            _Utils.printExceptionSycn(this, uiHandler, e);
        }
    }

    int progress;

    private void disableButton() {
//        imageLeft.setEnabled(false);
//        imageRight.setEnabled(false);
//        imageLeft.setColorFilter(Color.GRAY);
//        imageRight.setColorFilter(Color.GRAY);
        tvToday.setTextColor(Color.GRAY);
    }

    private void enableButton() {
//        imageLeft.setEnabled(true);
//        imageRight.setEnabled(true);
////        buttonMonth.setEnabled(true);
////        buttonToday.setEnabled(true);
//        imageLeft.setColorFilter(Color.TRANSPARENT);
//        imageRight.setColorFilter(Color.TRANSPARENT);
//        buttonMonth.setTextColor(getResources().getColor(R.color.month_text_color));
        tvToday.setTextColor(getResources().getColor(R.color.month_text_color));
    }

    private class RefreshCalendarTask extends AsyncTask {

        /**
         * doInBackground方法内部执行后台任务,不可在此方法内修改UI
         *
         * @param params
         * @return
         */
        @Override
        protected Object doInBackground(Object[] params) {
            try {

                progress = 0;
                publishProgress(progress);
                // 得到本月节气
                for (Map.Entry<DateTime, SolarTerm> entry : solarTermMap.entrySet()) {
                    if (entry.getKey().getYear() == currentYear && entry.getKey().getMonth() == currentMonth) {
                        currentMonthSolarTermMap.put(entry.getKey(), entry.getValue());
                    }
                }

                publishProgress(progress++);

                // 获得当月戒期信息
                try {
                    Religious religious = new Religious(MainActivity.this, currentYear, currentMonth, solarTermMap, new ReligiousCallBack() {
                        @Override
                        public void execute() {
                            publishProgress(progress++);
                        }
                    });
                    religiousDayMap = religious.getReligiousDays();

                    publishProgress(progress++);
                    remarkMap = religious.getRemarks();
                } catch (InterruptedException e) {
                } catch (Exception ex) {
                    religiousDayMap = new HashMap<>();
                    remarkMap = new HashMap<>();
                    _Utils.printExceptionSycn(MainActivity.this, uiHandler, ex);
                }
                publishProgress(progress++);
            } catch (Exception e) {
                _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
            }
            return null;
        }

        /**
         * onPreExecute方法用于在执行后台任务前做一些UI操作
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbLoading.setVisibility(View.VISIBLE);
            pbLoading.setProgress(0);
            pbLoading.setMax(selectedDate.getActualMaximum(DateTime.DAY_OF_MONTH));
        }

        /**
         * onProgressUpdate方法用于更新进度信息
         *
         * @param values
         */
        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
            pbLoading.setProgress((int) values[0]);
            if ((int) values[0] == 0) {
                disableButton();
            }
        }

        /**
         * onPostExecute方法用于在执行完后台任务后更新UI,显示结果
         *
         * @param o
         */
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            try {
                Boolean tag = false;
                DateTime dateTime = new DateTime(selectedDate.getYear(), selectedDate.getMonth(), 1);
                if (dateTime.get(DateTime.DAY_OF_WEEK) - 1 == 0) {
                    tag = true;
                }

                for (Map.Entry<DateTime, View> entry : calendarItemViewsMap.entrySet()) {
                    CalendarItem calendarItem = calendarItemsMap.get(dateTimeToPosition(entry.getKey(), tag));
                    if (calendarItem != null) {
                        View convertView = entry.getValue();
                        TextView textViewNongLi = (TextView) convertView.findViewById(R.id.calendarItem_tv_NongLiDay);
                        ImageView imageIsUnReligious = (ImageView) convertView.findViewById(R.id.calendarItem_cvIsUnReligious);

                        // 节气
                        for (Map.Entry<DateTime, SolarTerm> termEntry : currentMonthSolarTermMap.entrySet()) {
                            DateTime today = new DateTime(termEntry.getKey().getYear(), termEntry.getKey().getMonth(), termEntry.getKey().get(DateTime.DAY_OF_MONTH), 0, 0, 0);
                            if (CalendarHelper.isSameDate(today, calendarItem.getYangLi())) {
                                textViewNongLi.setText(termEntry.getValue().toString());
                                //                            textViewNongLi.setTextColor(Color.CYAN);
                                break;
                            }
                        }

                        //
                        DateTime currentDate = entry.getKey().getDate();
                        if (religiousDayMap.containsKey(currentDate)) {
                            calendarItem.setReligious(religiousDayMap.get(currentDate));
                        }
                        if (remarkMap.containsKey(currentDate)) {
                            calendarItem.setRemarks(remarkMap.get(currentDate));
                        }

                        // 非戒期日
                        if (calendarItem.getReligious() == null) {
                            imageIsUnReligious.setVisibility(View.VISIBLE);
                        }
                        // 戒期日，找到警示关键字。
                        else if (findReligiousKeyWord(calendarItem.getReligious()) == 1) {
                            textViewNongLi.setTextColor(getResources().getColor(R.color.month_text_color));
                        }
                        refreshInfoLayout(selectedDate);
                    }
                }

                //region test
                for (Map.Entry<Integer, CalendarItem> entity : calendarItemsMap.entrySet()) {
                    log("\n序号：" + entity.getKey() + "，阳历：" + entity.getValue().getYangLi().toShortDateString() + "，农历：" + entity.getValue().getNongLi().getDayStr() + "\n" + entity.getValue().getReligious());
                }
                //endregion
                enableButton();
            } catch (Exception e) {
                _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
            } finally {
                pbLoading.setVisibility(View.GONE);
            }
        }

        /**
         * onCancelled方法用于在取消执行中的任务时更改UI
         */
        @Override
        protected void onCancelled() {
            super.onCancelled();

            pbLoading.setVisibility(View.VISIBLE);
            pbLoading.setProgress(0);
        }
    }

    private void log(String log) {
        Log.e("wangsc", log);
    }


    /**
     * 事件 - 改变月份按钮
     */
    View.OnClickListener btnCurrentMonth_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                showMonthPickerDialog(currentYear, currentMonth);
            } catch (Exception ex) {
                _Utils.printExceptionSycn(MainActivity.this, uiHandler, ex);
            }
        }
    };


    public void showMonthPickerDialog(int year, int month) {
        View view = View.inflate(MainActivity.this, R.layout.inflate_date_picker_dialog, null);
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setView(view).create();
        dialog.setTitle("选择月份");


        final NumberPicker npYear = (NumberPicker) view.findViewById(R.id.npYear);
        final NumberPicker npMonth = (NumberPicker) view.findViewById(R.id.npMonth);


        String[] yearValues = new String[Lunar.MaxYear - Lunar.MinYear + 1];
        for (int i = 0; i < yearValues.length; i++) {
            yearValues[i] = i + Lunar.MinYear + "年";
        }

        String[] monthValues = new String[12];
        for (int i = 0; i < monthValues.length; i++) {
            monthValues[i] = i + 1 + "月";
        }


        npYear.setMinValue(Lunar.MinYear);
        npYear.setMaxValue(Lunar.MaxYear);
        npYear.setDisplayedValues(yearValues);
        npYear.setValue(year);
        npYear.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 禁止对话框打开后数字选择框被选中
        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npMonth.setDisplayedValues(monthValues);
        npMonth.setValue(month + 1);
        npMonth.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 禁止对话框打开后数字选择框被选中


        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "选择", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int year = npYear.getValue();
                int month = npMonth.getValue() - 1;
                DateTime dateTime = new DateTime();
                dateTime.set(year, month, 1);
                int maxDayOfMonth = dateTime.getActualMaximum(Calendar.DAY_OF_MONTH);
                int selectedDay = MainActivity.this.selectedDate.getDay();
                setSelectedDate(year, month, maxDayOfMonth < selectedDay ? maxDayOfMonth : selectedDay);
                //                    refreshCalendarWithDialog();
                dialog.dismiss();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

//    public class MonthPickerDialog {
//        Dialog dialog;
//
//        /**
//         * @param year  1900 - 2049
//         * @param month 0 - 11
//         */
//        public MonthPickerDialog(int year, int month) {
//            try {
//                dialog = new Dialog(MainActivity.this);
//                dialog.setContentView(R.layout.inflate_date_picker_dialog);
//                dialog.setTitle("选择月份");
//
//                final NumberPicker npYear = (NumberPicker) dialog.findViewById(R.id.npYear);
//                final NumberPicker npMonth = (NumberPicker) dialog.findViewById(R.id.npMonth);
//                Button btnOK = (Button) dialog.findViewById(R.id.btnOK);
//                Button btnCancle = (Button) dialog.findViewById(R.id.btnCancel);
//
//
//                npYear.setMinValue(Lunar.MinYear);
//                npYear.setMaxValue(Lunar.MaxYear);
//                npYear.setValue(year);
//                npYear.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 禁止对话框打开后数字选择框被选中
//                npMonth.setMinValue(1);
//                npMonth.setMaxValue(12);
//                npMonth.setValue(month + 1);
//                npMonth.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 禁止对话框打开后数字选择框被选中
//                btnOK.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        int year = npYear.getValue();
//                        int month = npMonth.getValue() - 1;
//                        DateTime dateTime = new DateTime();
//                        dateTime.set(year, month, 1);
//                        int maxDayOfMonth = dateTime.getActualMaximum(Calendar.DAY_OF_MONTH);
//                        int selectedDay = MainActivity.this.selectedDate.getDay();
//                        setSelectedDate(year, month, maxDayOfMonth < selectedDay ? maxDayOfMonth : selectedDay);
//                        //                    refreshCalendarWithDialog();
//                        dialog.cancel();
//                    }
//                });
//                btnCancle.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialog.cancel();
//                    }
//                });
//            } catch (Exception e) {
//                _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
//            }
//        }
//
//        public void show() {
//            dialog.show();
//        }
//    }

    /**
     * 事件 - 点击日历某天
     */
    private AdapterView.OnItemClickListener userCalender_OnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {
                if (!calendarItemsMap.containsKey(position)) return;

                CalendarItem calendarItem = calendarItemsMap.get(position);
                if (todayPosition != -1) {
                    if (preSelectedPosition != -1 && preSelectedPosition != todayPosition) {
                        parent.getChildAt(preSelectedPosition).findViewById(R.id.calendarItem_cvIsSelected).setVisibility(View.INVISIBLE);
                    }
                    if (position != todayPosition) {
                        view.findViewById(R.id.calendarItem_cvIsSelected).setVisibility(View.VISIBLE);
                    }
                } else {
                    if (preSelectedPosition != -1) {
                        parent.getChildAt(preSelectedPosition).findViewById(R.id.calendarItem_cvIsSelected).setVisibility(View.INVISIBLE);
                    }
                    view.findViewById(R.id.calendarItem_cvIsSelected).setVisibility(View.VISIBLE);
                }
                preSelectedPosition = position;

                //
                setSelectedDate(calendarItem.getYangLi().getYear(), calendarItem.getYangLi().getMonth(), calendarItem.getYangLi().getDay());
            } catch (Exception e) {
                _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
            }
        }
    };

    /**
     * 事件 - 返回今天
     */
    private View.OnClickListener btnToday_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                if (preSelectedPosition != -1) {
                    userCalender.getChildAt(preSelectedPosition).findViewById(R.id.calendarItem_cvIsSelected).setVisibility(View.INVISIBLE);
                }
                DateTime today = DateTime.getToday();
                setSelectedDate(today.getYear(), today.getMonth(), today.getDay());
            } catch (Exception e) {
                _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
            }
        }
    };

    /**
     * 设置SelectedDate，并在修改该属性之后，重载自定义日历区域数据。
     *
     * @param year
     * @param month
     * @param day
     */
    public void setSelectedDate(int year, int month, int day) {
        try {
            boolean monthHasChanged = false;

            // 如果新选中日期与当前月份不再同一月份，则刷新日历。
            if (year != currentYear || month != currentMonth) {
                monthHasChanged = true;
            }
            this.selectedDate.set(year, month, day);

            // 判断是否刷新自定义日历区域
            if (monthHasChanged) {
                currentYear = year;
                currentMonth = month;
                refreshCalendar();
//                refreshCalendarWithDialog(_String.concat("正在加载", currentYear, "年", currentMonth + 1, "月份", "戒期信息。"));
            }
            setYearMonthText();

            // “今”按钮是否显示
            DateTime today = DateTime.getToday();
            if (year == today.getYear() && month == today.getMonth() && day == today.getDay()) {
                log("today");
                setTodayEnable(false);
            } else {
                log("not today");
                setTodayEnable(true);
            }


            //
            refreshInfoLayout(selectedDate);
        } catch (Exception e) {
            _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
        }
    }

    /**
     * 设置“回到今天”按钮是否可用。
     *
     * @param enable
     */
    private void setTodayEnable(Boolean enable) {
        try {
            final Boolean value = enable;
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    log("xxxxxxxxxxxxxxxxxxxxx : "+value.toString());
                    // TODO: 2021/1/5 asdfasdfsadfs
                    if (value) {
                        tvToday.setVisibility(View.VISIBLE);
                        ibLeftMenu.setVisibility(View.INVISIBLE);
                        stopRorateWan();
                    } else {
                        tvToday.setVisibility(View.INVISIBLE);
                        ibLeftMenu.setVisibility(View.VISIBLE);
                        rorateWan();
                    }
                }
            });
        } catch (Exception e) {
            _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
        }
    }

    private void rorateWan(){
        AnimationUtils.setRorateAnimation(MainActivity.this,ibLeftMenu,7000);
    }

    private void stopRorateWan(){
        ibLeftMenu.clearAnimation();
    }

    /**
     * 小于10前追加‘0’
     *
     * @param x
     * @return
     */
    private String format(int x) {
        String s = "" + x;
        if (s.length() == 1)
            s = "0" + s;
        return s;
    }

    /**
     * 读取JAVA结构的二进制节气数据文件。
     *
     * @param resId 待读取的JAVA二进制文件。
     * @return
     * @throws IOException
     * @throws Exception
     */
    private TreeMap<DateTime, SolarTerm> loadJavaSolarTerms(int resId) throws IOException, Exception {
        TreeMap<DateTime, SolarTerm> result = new TreeMap<DateTime, SolarTerm>();
        try {
            DataInputStream dis = new DataInputStream(getResources().openRawResource(resId));

            long date = dis.readLong();
            int solar = dis.readInt();
            try {
                while (true) {
                    DateTime cal = new DateTime();
                    cal.setTimeInMillis(date);
                    SolarTerm solarTerm = SolarTerm.Int2SolarTerm(solar);
                    result.put(cal, solarTerm);
                    date = dis.readLong();
                    solar = dis.readInt();
                }
            } catch (EOFException ex) {
                dis.close();
            }
        } catch (Resources.NotFoundException e) {
            _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
        } catch (Exception e) {
            _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
        }
        // 按照KEY排序TreeMap
//        TreeMap<DateTime, SolarTerm> result = new TreeMap<DateTime, SolarTerm>(new Comparator<DateTime>() {
//            @Override
//            public int compare(DateTime lhs, DateTime rhs) {
//                return lhs.compareTo(rhs);
//            }
//        });
        return result;
    }

    /**
     * 将C#导出的二进制文件转化为JAVA数据结构存储的二进制文件，并保存为/mnt/sdcard/solar300.dat。
     *
     * @param resId 待转化的C#二进制文件资源ID。
     * @throws IOException
     * @throws Exception
     */
    private void convertToJavafile(int resId) throws IOException, Exception {
        try {
            Map<DateTime, SolarTerm> solarTermMap = loadCsharpSolarTerms(resId);
            File file = new File("/mnt/sdcard/solar300.dat");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(fos);

            Set set = solarTermMap.entrySet();
            Iterator i = set.iterator();
            while (i.hasNext()) {
                Map.Entry<DateTime, SolarTerm> solar = (Map.Entry<DateTime, SolarTerm>) i.next();
                dos.writeLong(solar.getKey().getTimeInMillis());
                dos.writeInt(solar.getValue().getValue());
            }
            dos.flush();
            dos.close();
            fos.close();
        } catch (Exception e) {
            _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
        }
    }

    /**
     * 从C#导出的二进制文件获取节气数据。
     *
     * @param resId 资源文件ID
     * @return
     */
    private Map<DateTime, SolarTerm> loadCsharpSolarTerms(int resId) throws Exception {
        Map<DateTime, SolarTerm> solarTermMap = new HashMap<DateTime, SolarTerm>();
        try {
            InputStream stream = getResources().openRawResource(resId);
            byte[] longBt = new byte[8];
            byte[] intBt = new byte[4];
            byte[] nullBt = new byte[4];
            stream.read(longBt);
            stream.read(intBt);
            int cursor = stream.read(nullBt);

            while (cursor != -1) {
                DateTime cal = new DateTime();
                cal.setTimeInMillis(bytesToLong(longBt));
                int solar = bytesToInt(intBt);
                SolarTerm solarTerm = SolarTerm.Int2SolarTerm(solar);
                solarTermMap.put(cal, solarTerm);
                stream.read(longBt);
                stream.read(intBt);
                cursor = stream.read(nullBt);
            }
            stream.close();
        } catch (Resources.NotFoundException e) {
            _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
        } catch (Exception e) {
            _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
        }
        return solarTermMap;
    }

    /**
     * byte[] 转化为int。
     *
     * @param bytes 需要转化的byte[]
     * @return
     */
    public int bytesToInt(byte[] bytes) {
        return bytes[0] & 0xFF | (bytes[1] & 0xFF) << 8 | (bytes[2] & 0xFF) << 16 | (bytes[3] & 0xFF) << 24;
    }

    private ByteBuffer buffer;

    /**
     * byte[] 转化为long。
     *
     * @param bytes 需要转化的byte[]
     * @return
     */
    public long bytesToLong(byte[] bytes) {
        buffer = ByteBuffer.allocate(8);
        for (int i = bytes.length - 1; i >= 0; i--) {
            buffer.put(bytes[i]);
        }
        buffer.flip();//need flip
        return buffer.getLong();
    }

    @Override
    public void onBackPressed() {
        try {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        } catch (Exception ex) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        menuItemSelected(item);
        return super.onOptionsItemSelected(item);
    }


    private boolean menuItemSelected(MenuItem menuItem) {

        int id = menuItem.getItemId();
        if (id == R.id.menu_settings) {
            startActivityForResult(new Intent(MainActivity.this, SettingActivity.class), TO_SETTING_ACTIVITY);
        }
//        else if (id == R.id.menu_select) {
//            try {
//                MonthPickerDialog monthPickerDialog = new MonthPickerDialog(currentYear, currentMonth);
//                monthPickerDialog.show();
//            } catch (Exception ex) {
//
//            }
//        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode) {
                case TO_SETTING_ACTIVITY:
                    if (SettingActivity.isCalenderChanged) {
//                        refreshCalendarWithDialog("配置已更改，正在重新加载...");
                        isWeekendFirst = dataContext.getSetting(Setting.KEYS.is_weekend_first, true).getBoolean();
                        refreshCalendar();
                    }
                    if (SettingActivity.isRecordSetChanged) {
                        initRecordPart();
                    }
                    break;
                case TO_SEXUAL_RECORD_ACTIVITY:
                    initRecordPart();
                    break;
            }
        } catch (NumberFormatException e) {
            _Utils.printExceptionSycn(MainActivity.this, uiHandler, e);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showAddSexualDayDialog() {

        View view = View.inflate(MainActivity.this, R.layout.inflate_dialog_date_picker, null);
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setView(view).create();
        dialog.setTitle("最后一次行房日期");

        DateTime dateTime = new DateTime();
        final int year = dateTime.getYear();
        int month = dateTime.getMonth();
//        int maxDay = dateTime.getActualMaximum(Calendar.DAY_OF_MONTH);
        int day = dateTime.getDay();
        int hour = dateTime.getHour();

        String[] yearNumbers = new String[3];
        for (int i = year - 2; i <= year; i++) {
            yearNumbers[i - year + 2] = i + "年";
        }
        String[] monthNumbers = new String[12];
        for (int i = 0; i < 12; i++) {
            monthNumbers[i] = i + 1 + "月";
        }
        String[] dayNumbers = new String[31];
        for (int i = 0; i < 31; i++) {
            dayNumbers[i] = i + 1 + "日";
        }
        String[] hourNumbers = new String[24];
        for (int i = 0; i < 24; i++) {
            hourNumbers[i] = i + "点";
        }
        final NumberPicker npYear = (NumberPicker) view.findViewById(R.id.npYear);
        final NumberPicker npMonth = (NumberPicker) view.findViewById(R.id.npMonth);
        final NumberPicker npDay = (NumberPicker) view.findViewById(R.id.npDay);
        final NumberPicker npHour = (NumberPicker) view.findViewById(R.id.npHour);
        npYear.setMinValue(year - 2);
        npYear.setMaxValue(year);
        npYear.setValue(year);
        npYear.setDisplayedValues(yearNumbers);
        npYear.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 禁止对话框打开后数字选择框被选中
        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npMonth.setDisplayedValues(monthNumbers);
        npMonth.setValue(month + 1);
        npMonth.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 禁止对话框打开后数字选择框被选中
        npDay.setMinValue(1);
        npDay.setMaxValue(31);
        npDay.setDisplayedValues(dayNumbers);
        npDay.setValue(day);
        npDay.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 禁止对话框打开后数字选择框被选中
        npHour.setMinValue(0);
        npHour.setMaxValue(23);
        npHour.setDisplayedValues(hourNumbers);
        npHour.setValue(hour);
        npHour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 禁止对话框打开后数字选择框被选中


        npMonth.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                DateTime selected = new DateTime(npYear.getValue(), npMonth.getValue() - 1, 1);
                int max = selected.getActualMaximum(Calendar.DAY_OF_MONTH);

                int day = npDay.getValue();
                npDay.setMaxValue(max);
                if (day > max) {
                    npDay.setValue(1);
                } else {
                    npDay.setValue(day);
                }
            }
        });
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int year = npYear.getValue();
                    int month = npMonth.getValue() - 1;
                    int day = npDay.getValue();
                    int hour = npHour.getValue();
                    DateTime selectedDateTime = new DateTime(year, month, day, hour, 0, 0);
                    SexualDay sexualDay = new SexualDay(selectedDateTime);
                    dataContext.addSexualDay(sexualDay);
                    initRecordPart();
                    dialog.dismiss();
                } catch (Exception e) {
                    _Utils.printException(MainActivity.this, e);
                }
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    _Utils.printException(MainActivity.this, e);
                }
            }
        });
        dialog.show();
    }
}
