package com.wang17.religiouscalendar.activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.wang17.religiouscalendar.R
import com.wang17.religiouscalendar.e
import com.wang17.religiouscalendar.emnu.MDrelation
import com.wang17.religiouscalendar.emnu.MDtype
import com.wang17.religiouscalendar.emnu.Zodiac
import com.wang17.religiouscalendar.fragment.ActionBarFragment
import com.wang17.religiouscalendar.fragment.ActionBarFragment.OnActionFragmentBackListener
import com.wang17.religiouscalendar.model.*
import com.wang17.religiouscalendar.util._Session
import com.wang17.religiouscalendar.util._String
import com.wang17.religiouscalendar.util._Utils
import kotlinx.android.synthetic.main.include_setting_part1.*
import kotlinx.android.synthetic.main.include_setting_part2.*
import kotlinx.android.synthetic.main.include_setting_part3.*
import kotlinx.android.synthetic.main.include_setting_part4.*
import kotlinx.android.synthetic.main.include_setting_part5.*
import kotlinx.android.synthetic.main.include_setting_part7.*
import java.util.*

class SettingActivity : AppCompatActivity(), OnActionFragmentBackListener {
    private lateinit var dataContext: DataContext
    private lateinit var mdListAdapter: MDlistdAdapter
    private lateinit var mdListItems: MutableList<HashMap<String, String>>
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("wangsc", "SettingActivity is loading ...")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        try {
            initializeFields()
            initializeEvents()
            Log.i("wangsc", "SettingActivity have loaded ...")
        } catch (ex: Exception) {
            _Utils.printExceptionSycn(this@SettingActivity, uiHandler, ex)
        }
    }

    private fun initializeFields() {
        try {
            dataContext = DataContext(this@SettingActivity)
            isCalenderChanged = false
            isRecordSetChanged = false

//        if (UpdateManager.isUpdate())
//            textView_update.setVisibility(View.VISIBLE);
            if (dataContext.getSetting(Setting.KEYS.recordIsOpened, false).getBoolean() == true) {
                button_recordStatus.setImageResource(R.mipmap.on)
            } else {
                button_recordStatus.setImageResource(R.mipmap.off)
            }
            autoWayDataInit()
            /**
             * 开关记录
             */
            button_recordStatus.setOnClickListener {
                if (dataContext.getSetting(Setting.KEYS.recordIsOpened, false).getBoolean() == false) {
                    if (dataContext.getSetting(Setting.KEYS.birthday) == null) {
                        showBirthdayDialog(this@SettingActivity, "设定生日", object : CallBack {
                            override fun execute() {
                                button_recordStatus.setImageResource(R.mipmap.on)
                                dataContext.editSetting(Setting.KEYS.recordIsOpened, true)
                            }
                        })
                    } else {
                        button_recordStatus.setImageResource(R.mipmap.on)
                        dataContext.editSetting(Setting.KEYS.recordIsOpened, true)
                    }
                } else {
                    button_recordStatus.setImageResource(R.mipmap.off)
                    dataContext.editSetting(Setting.KEYS.recordIsOpened, false)
                }
                isRecordSetChanged = true
            }
            /**
             * 设置生日按钮
             */
            button_birthday.setOnClickListener { if (dataContext.getSetting(Setting.KEYS.targetAuto, true).getBoolean() == true) showBirthdayDialog(this@SettingActivity, "设定生日", null) }
            /**
             * 十斋日、六斋日、观音斋
             */
            val szr = dataContext.getSetting(Setting.KEYS.szr, false)
            checkBox_szr.isChecked = szr.getBoolean()
            val lzr = dataContext.getSetting(Setting.KEYS.lzr, false)
            checkBox_lzr.isChecked = lzr.getBoolean()
            val gyz = dataContext.getSetting(Setting.KEYS.gyz, false)
            checkBox_gyz.isChecked = gyz.getBoolean()
            checkBox_weekend_first.isChecked = dataContext.getSetting(Setting.KEYS.is_weekend_first, true).getBoolean()
            /**
             * 太岁日
             */
            initializeZodiac(spinner_zodiac1)
            initializeZodiac(spinner_zodiac2)
            val zodiac1 = dataContext.getSetting(Setting.KEYS.zodiac1)
            val zodiac2 = dataContext.getSetting(Setting.KEYS.zodiac2)

            if (zodiac1 != null) {
                spinner_zodiac1.setSelection(Zodiac.fromString(zodiac1.toString())?.toInt()?:0, true)
            }
            if (zodiac2 != null) {
                spinner_zodiac2.setSelection(Zodiac.fromString(zodiac2.toString())?.toInt()?:0, true)
            }
            /**
             * 纪念日
             */
            val list: MutableList<String> = ArrayList()
            for (i in 0 until MDtype.count()) {
                list.add(MDtype.fromInt(i).toString())
            }
            fillSpinner(spinner_mdtype, list)
            val list2: MutableList<String> = ArrayList()
            for (i in 0 until MDrelation.count()) {
                list2.add(MDrelation.fromInt(i).toString())
            }
            fillSpinner(spinner_mdrelation, list2)
            initializeLunarMonth(spinner_month)
            initializeLunarDay(spinner_day)
            val memorialDays = dataContext.getMemorialDays()
            mdListItems = ArrayList()
            for (md in memorialDays) {
                addListItem(md)
            }
            refreshMdList()
            /**
             * 欢迎界面
             */
            initializeWelcome()
            spinner_welcome.setSelection(dataContext.getSetting(Setting.KEYS.welcome, 0).getInt(), true)
            initializeDuration()
            spinner_duration.setSelection(dataContext.getSetting(Setting.KEYS.welcome_duration, 1).getInt(), true)
        } catch (e: Exception) {
            _Utils.printExceptionSycn(this, uiHandler, e)
        }
        //        mdListAdapter = new MDlistdAdapter();
//        listViewMD.setAdapter(mdListAdapter);
    }

    private fun autoWayDataInit() {
        try {
            val settingBirthday = dataContext.getSetting(Setting.KEYS.birthday)
            if (settingBirthday != null) {
                val birthday = settingBirthday.getDateTime()
                button_birthday.text = birthday.toShortDateString()
                val aa = (_Utils.getTargetInMillis(birthday)/ 3600000).toInt()
                button_customTarget.text = DateTime.toSpanString(aa)
            } else {
                button_birthday.text = "设定生日"
            }
        } catch (e: Exception) {
            _Utils.printException(this, e)
        }
    }

    private fun addListItem(md: MemorialDay) {
        val map = HashMap<String, String>()
        map["id"] = md.id.toString()
        map["relation"] = md.relation.toString()
        map["type"] = md.type.toString()
        map["lunarDate"] = md.lunarDate.toString()
        mdListItems.add(map)
    }

    private fun refreshMdList() {
        val linearLayout = findViewById(R.id.mdList) as LinearLayout
        linearLayout.removeAllViews()
        for (position in mdListItems.indices) {
            val convertView = View.inflate(this@SettingActivity, R.layout.inflate_md_list_item, null)
            val map = mdListItems[position]
            val textViewRelation = convertView.findViewById<View>(R.id.textView_relation) as TextView
            val textViewType = convertView.findViewById<View>(R.id.textView_type) as TextView
            val textViewLunarDate = convertView.findViewById<View>(R.id.textView_lunarDate) as TextView
            val btnDel = convertView.findViewById<View>(R.id.linear_delete) as LinearLayout
            val relation = map["relation"]
            val type = map["type"]
            val lunarDate = map["lunarDate"]
            textViewRelation.text = relation
            textViewType.text = type
            textViewLunarDate.text = lunarDate
            btnDel.setOnClickListener {
                val builder = AlertDialog.Builder(this@SettingActivity)
                builder.setTitle("删除确认")
                builder.setMessage(_String.concat("是否要删除【", relation, "\t", type, "】?"))
                builder.setPositiveButton("确认") { dialog, which ->
                    dataContext.deleteMemorialDay(UUID.fromString(mdListItems[position]["id"]))
                    mdListItems.removeAt(position)
                    refreshMdList()
                    isCalenderChanged = true
                    dialog.cancel()
                    snackbar("删除成功")
                }
                builder.setNegativeButton("取消") { dialog, which -> dialog.cancel() }
                builder.create().show()
            }
            linearLayout.addView(convertView, 0)
        }
    }

    private val uiHandler = Handler()
    private fun initializeEvents() {
//        textView_update.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                UpdateManager manager = new UpdateManager(SettingActivity.this);
//                manager.startDownload();
//            }
//        });
        checkBox_weekend_first.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                dataContext.editSetting(Setting.KEYS.is_weekend_first, isChecked)
                isCalenderChanged = true
                snackbarSaved()
            }
        }
        checkBox_szr.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                dataContext.editSetting(Setting.KEYS.szr, isChecked)
                isCalenderChanged = true
                snackbarSaved()
            }
        }
        checkBox_lzr.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                dataContext.editSetting(Setting.KEYS.lzr, isChecked)
                isCalenderChanged = true
                snackbarSaved()
            }
        }
        checkBox_gyz.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                dataContext.editSetting(Setting.KEYS.gyz, isChecked)
                isCalenderChanged = true
                snackbarSaved()
            }
        }
        button_addMD.setOnClickListener {
            try {
                val relation = spinner_mdrelation.selectedItem.toString()
                val type = spinner_mdtype.selectedItem.toString()
                val month = spinner_month.selectedItem.toString()
                val day = spinner_day.selectedItem.toString()
                val md = MemorialDay(MDtype.fromString(type),MDrelation.fromString(relation),LunarDate(month, day))
                dataContext.addMemorialDay(md)

                //
                addListItem(md)
                refreshMdList()
                isCalenderChanged = true
                snackbar("添加成功")
            } catch (ex: Exception) {
                _Utils.printExceptionSycn(this@SettingActivity, uiHandler, ex)
            }
        }
        spinner_zodiac1.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                val setting = dataContext.getSetting(Setting.KEYS.zodiac1)
                val zodiac = spinner_zodiac1.getItemAtPosition(position).toString()
                if (setting != null) {
                    if (setting.value != zodiac) {
                        dataContext.editSetting(Setting.KEYS.zodiac1, zodiac)
                        isCalenderChanged = true
                        snackbarSaved()
                    }
                } else {
                    dataContext.editSetting(Setting.KEYS.zodiac1, zodiac)
                    isCalenderChanged = true
                    snackbarSaved()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinner_zodiac2.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                val setting = dataContext.getSetting(Setting.KEYS.zodiac2)
                val zodiac = spinner_zodiac2.getItemAtPosition(position).toString()
                if (setting != null) {
                    if (setting.value != zodiac) {
                        dataContext.editSetting(Setting.KEYS.zodiac2, zodiac)
                        isCalenderChanged = true
                        snackbarSaved()
                    }
                } else {
                    dataContext.editSetting(Setting.KEYS.zodiac2, zodiac)
                    isCalenderChanged = true
                    snackbarSaved()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinner_welcome.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                val setting = dataContext.getSetting(Setting.KEYS.welcome, 0)
                if (setting.getInt() != position) {
                    dataContext.editSetting(Setting.KEYS.welcome, spinner_welcome.selectedItemPosition)
                    snackbarSaved()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinner_duration.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                val setting = dataContext.getSetting(Setting.KEYS.welcome_duration, 1)
                if (setting.getInt() != position) {
                    dataContext.editSetting(Setting.KEYS.welcome_duration, spinner_duration.selectedItemPosition)
                    snackbarSaved()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initializeDuration() {
        val list: MutableList<String> = ArrayList()
        list.add("0秒")
        list.add("3秒")
        list.add("4秒")
        list.add("5秒")
        list.add("6秒")
        list.add("7秒")
        fillSpinner(spinner_duration, list)
    }

    private fun initializeWelcome() {
        val list: MutableList<String> = ArrayList()
        for (i in _Session.welcomes.indices) {
            list.add(_Session.welcomes[i].getListItemString())
        }
        fillSpinner(spinner_welcome, list)
    }

    private fun initializeZodiac(spinner: Spinner) {
        val mItems: MutableList<String> = ArrayList()
        for (i in 0 until Zodiac.count()) {
            mItems.add(Zodiac.fromInt(i).toString())
        }
        fillSpinner(spinner, mItems)
    }

    private fun initializeLunarMonth(spinner: Spinner) {
        fillSpinner(spinner, LunarDate.Months)
    }

    private fun initializeLunarDay(spinner: Spinner) {
        fillSpinner(spinner, LunarDate.Days)
    }

    private fun fillSpinner(spinner: Spinner, values: MutableList<String>) {
        val aspn = ArrayAdapter(this@SettingActivity, R.layout.inflate_spinner, values)
        aspn.setDropDownViewResource(R.layout.inflate_spinner_dropdown)
        spinner.adapter = aspn
    }

    override fun onBackListener() {
        finish()
    }

    /**
     *
     */
    protected inner class MDlistdAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return mdListItems.size
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            var convertView = convertView
            val vew = convertView
            applicationContext.resources
            convertView = View.inflate(this@SettingActivity, R.layout.inflate_md_list_item, null)
            val map = mdListItems[position]
            val textViewRelation = convertView.findViewById<View>(R.id.textView_relation) as TextView
            val textViewType = convertView.findViewById<View>(R.id.textView_type) as TextView
            val textViewLunarDate = convertView.findViewById<View>(R.id.textView_lunarDate) as TextView
            val btnDel = convertView.findViewById<View>(R.id.imageView_Del) as ImageView
            btnDel.setOnClickListener {
                val builder = AlertDialog.Builder(this@SettingActivity)
                builder.setTitle("删除确认")
                builder.setMessage("是否要删除此纪念日?")
                builder.setPositiveButton("确认") { dialog, which ->
                    dataContext.deleteMemorialDay(UUID.fromString(mdListItems[position]["id"]))
                    mdListItems.removeAt(position)
                    mdListAdapter.notifyDataSetChanged()
                    isCalenderChanged = true
                    dialog.cancel()
                    snackbar("删除成功")
                    //                            Toast.makeText(SettingActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                }
                builder.setNegativeButton("取消") { dialog, which -> dialog.cancel() }
                builder.create().show()
            }
            textViewRelation.text = map["relation"]
            textViewType.text = map["type"]
            textViewLunarDate.text = map["lunarDate"]
            return convertView
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun snackbarSaved() {
        snackbar("设置已保存")
    }

    private fun snackbar(message: String) {
        val root = findViewById(R.id.layout_setting_root) as RelativeLayout
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show()
    }

    fun showBirthdayDialog(context: Context, title: String?, callBack: CallBack?) {
        val view = View.inflate(context, R.layout.inflate_dialog_date_picker, null)
        val dialog = AlertDialog.Builder(context).setView(view).create()
        dialog.setTitle(title)
        val npYear = view.findViewById<View>(R.id.npYear) as NumberPicker
        val npMonth = view.findViewById<View>(R.id.npMonth) as NumberPicker
        val npDay = view.findViewById<View>(R.id.npDay) as NumberPicker
        val npHour = view.findViewById<View>(R.id.npHour) as NumberPicker
        val setting = dataContext.getSetting(Setting.KEYS.birthday)
        var date: DateTime? = null
        if (setting == null) {
            date = DateTime()
            date.add(Calendar.YEAR, -20)
        } else {
            date = setting.getDateTime()
        }
        val cyear = DateTime().getYear()
        val year = date.getYear()
        val month = date.getMonth()
        val day = date.getDay()
        try {
            val yearNumbers = arrayOfNulls<String>(100)
            for (i in cyear - 99..cyear) {
                yearNumbers[i - cyear + 99] = i.toString() + "年"
            }
            val monthNumbers = arrayOfNulls<String>(12)
            for (i in 0..11) {
                monthNumbers[i] =  "${i+1}月"
            }
            val dayNumbers = arrayOfNulls<String>(31)
            for (i in 0..30) {
                dayNumbers[i] =  "${i+1}日"
            }
            npHour.visibility = View.GONE
            npYear.minValue = cyear - 99
            npYear.maxValue = cyear
            npYear.value = year
            npYear.displayedValues = yearNumbers
            npYear.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 禁止对话框打开后数字选择框被选中
            npMonth.minValue = 1
            npMonth.maxValue = 12
            npMonth.displayedValues = monthNumbers
            npMonth.value = month + 1
            npMonth.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 禁止对话框打开后数字选择框被选中
            npDay.minValue = 1
            npDay.maxValue = 31
            npDay.displayedValues = dayNumbers
            npDay.value = day
            npDay.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 禁止对话框打开后数字选择框被选中
        } catch (e: Exception) {
            _Utils.printException(this@SettingActivity, e)
        }
        npMonth.setOnValueChangedListener { picker, oldVal, newVal ->
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
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定") { dialog, which ->
            try {
                val year = npYear.value
                val month = npMonth.value - 1
                val day = npDay.value
                val selectedDateTime = DateTime(year, month, day, 0, 0, 0)
                dataContext.editSetting(Setting.KEYS.birthday, selectedDateTime.timeInMillis)
                isRecordSetChanged = true
                autoWayDataInit()
                callBack?.execute()
                dialog.dismiss()
            } catch (e: Exception) {
                _Utils.printException(context, e)
            }
        }
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消") { dialog, which ->
            try {
                dialog.dismiss()
            } catch (e: Exception) {
                _Utils.printException(context, e)
            }
        }
        dialog.show()
    }

    interface CallBack {
        fun execute()
    }

    /*
    public void showTargetDialog(final Context context) {

        try {
            View view = View.inflate(context, R.layout.inflate_dialog_date_picker, null);
            android.support.v7.app.AlertDialog dialog = new android.support.v7.app.AlertDialog.Builder(context).setView(view).create();
            dialog.setTitle("自定义间隔");

            int targetInHour = 5 * 24;
            Setting setting = dataContext.getSetting(Setting.KEYS.targetInHour);
            if (setting != null) {
                targetInHour = setting.getInt();
            }
            int aaa = (int) (targetInHour / 24);
            int bbb = (int) (targetInHour % 24);
            String[] dayNumbers = new String[99];
            for (int i = 0; i < 99; i++) {
                dayNumbers[i] = i + 2 + "天";
            }
            String[] hourNumbers = new String[24];
            for (int i = 0; i < 24; i++) {
                hourNumbers[i] = i + "小时";
            }
            final NumberPicker npYear = (NumberPicker) view.findViewById(R.id.npYear);
            npYear.setVisibility(View.GONE);
            final NumberPicker npMonth = (NumberPicker) view.findViewById(R.id.npMonth);
            npMonth.setVisibility(View.GONE);
            final NumberPicker npDays = (NumberPicker) view.findViewById(R.id.npDay);
            final NumberPicker npHours = (NumberPicker) view.findViewById(R.id.npHour);
            npDays.setMinValue(2);
            npDays.setMaxValue(100);
            npDays.setDisplayedValues(dayNumbers);
            npDays.setValue(aaa);
            npDays.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 禁止对话框打开后数字选择框被选中
            npHours.setMinValue(0);
            npHours.setMaxValue(23);
            npHours.setDisplayedValues(hourNumbers);
            npHours.setValue(bbb);
            npHours.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // 禁止对话框打开后数字选择框被选中

            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        int days = npDays.getValue();
                        int hours = npHours.getValue();
                        long target = days * 24 + hours;
                        dataContext.editSetting(Setting.KEYS.targetInHour, target);
                        isRecordSetChanged = true;
                        customWayDataInit();
                        dialog.dismiss();
                    } catch (Exception e) {
                        _Utils.printException(context, e);
                    }
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        dialog.dismiss();
                        if (dataContext.getSetting(Setting.KEYS.recordIsOpened, false).getBoolean() == true
                                && dataContext.getSetting(Setting.KEYS.targetAuto, true).getBoolean() == false
                                && dataContext.getSetting(Setting.KEYS.targetInHour) == null) {
                            dataContext.editSetting(Setting.KEYS.recordIsOpened, false);
                            btnRecordStatus.setImageResource(R.drawable.off);
                        }
                    } catch (Exception e) {
                        _Utils.printException(context, e);
                    }
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    companion object {
        var isCalenderChanged = false
        var isRecordSetChanged = false
    }
}