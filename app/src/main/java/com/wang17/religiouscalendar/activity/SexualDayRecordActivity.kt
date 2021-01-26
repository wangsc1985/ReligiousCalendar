package com.wang17.religiouscalendar.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.wang17.religiouscalendar.R
import com.wang17.religiouscalendar.fragment.ActionBarFragment
import com.wang17.religiouscalendar.fragment.ActionBarFragment.OnActionFragmentBackListener
import com.wang17.religiouscalendar.model.*
import com.wang17.religiouscalendar.util._Utils
import kotlinx.android.synthetic.main.activity_sexual_day_record.*
import kotlinx.android.synthetic.main.include_setting_part5.*
import java.util.*

class SexualDayRecordActivity : AppCompatActivity(), OnActionFragmentBackListener {
    // 类变量
    private lateinit var dataContext: DataContext
    private lateinit var sexualDays: MutableList<SexualDay>
    private var recordListdAdapter: SexualDayListdAdapter = SexualDayListdAdapter()

    // 值变量
    private var max = 0

    /**
     * 20 4
     * 21 4.4
     * 22 4.8
     * 23 5.2
     * 24 5.6
     * 25 6
     * 26 6.4
     * 27 6.8
     * 28 7.2
     * 29 7.6
     * 30 8
     *
     *
     * 31 8.8
     * 32 9.6
     * 33 10.4
     * 34 11.2
     * 35 12
     * 36 12.8
     * 37 13.6
     * 38 14.4
     * 39 15.2
     * 40 16
     *
     *
     * 41 16.5
     * 42 17
     * 43 17.5
     * 44 18
     * 45 18.5
     * 46 19
     * 47 19.5
     * 48 20
     * 49 20.5
     * 50 21
     *
     *
     * 51 21.9
     * 52 22.8
     * 53 23.7
     * 54 24.6
     * 55 25.5
     * 56 26.4
     * 57 27.3
     * 58 28.2
     * 59 29.1
     * 60 30
     *
     *
     * 20 4
     * 30 8
     * 40 16
     * 50 21
     * 60 30
     * 年二十者四日一泄;年三十者，
     * 八日一泄;年四十者，
     * 十六日一泄;
     * 年五十者，
     * 二十一日一泄;
     * 年六十者，毕，闭精勿复泄也。
     * 若体力犹壮者，一月一泄。
     * 凡人气力，自相有强盛过人，亦不可抑忍。
     * 久而不泄，至生痈疽。
     * 若年过六十，而有数旬不得交接，意中平平者，可闭精勿泄也。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_sexual_day_record)
            isDataChanged = false
            dataContext = DataContext(this)
            sexualDays = dataContext.getSexualDays(true)
            max = if (dataContext.getSetting(Setting.KEYS.targetAuto, true).getBoolean() == true) {
                val set = dataContext.getSetting(Setting.KEYS.birthday)
                if ( set!= null) {
                    (_Utils.getTargetInMillis(DateTime(set.getLong())) / 3600000).toInt()
                } else {
                    0
                }
            } else {
                val setting2 = dataContext.getSetting(Setting.KEYS.targetInHour)
                setting2?.getInt() ?: 0
            }
            initSummary()
            val listView_sexualDays = findViewById(R.id.listView_sexualDays) as ListView
            listView_sexualDays.onItemClickListener = OnItemClickListener { parent, view, position, id ->
                if (view.findViewById<View>(R.id.layout_operate).visibility == View.VISIBLE) {
                    view.findViewById<View>(R.id.layout_operate).visibility = View.INVISIBLE
                } else {
                    for (i in 0 until parent.childCount) {
                        parent.getChildAt(i).findViewById<View>(R.id.layout_operate).visibility = View.INVISIBLE
                    }
                    view.findViewById<View>(R.id.layout_operate).visibility = View.VISIBLE
                }
            }
            listView_sexualDays.adapter = recordListdAdapter
        } catch (e: Exception) {
            _Utils.printException(this, e)
        }
    }

    private fun initSummary() {
        val lastSexualDay = dataContext.getLastSexualDay()
        var targetInHour = 0
        targetInHour = if (dataContext.getSetting(Setting.KEYS.targetAuto, true).getBoolean() == true) {
            _Utils.getTargetInHour(dataContext.getSetting(Setting.KEYS.birthday,System.currentTimeMillis()).getDateTime())
        } else {
            dataContext.getSetting(Setting.KEYS.targetInHour,0)?.getInt()
        }
        if (max > 0 && lastSexualDay != null) {
            val haveInHour = ((System.currentTimeMillis() - lastSexualDay.dateTime.timeInMillis) / 3600000).toInt()
            var leaveInHour = targetInHour - haveInHour
            if (leaveInHour > 0) {
                textView_time1.text = DateTime.toSpanString(haveInHour)
                textView_time2.text = DateTime.toSpanString(leaveInHour)
            } else {
                leaveInHour *= -1
                textView_time1.text = DateTime.toSpanString(haveInHour)
                textView_time2.text = "+" + DateTime.toSpanString(leaveInHour)
            }
        }
    }

    override fun onBackListener() {
        finish()
    }

    protected inner class SexualDayListdAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return sexualDays.size
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            try {
                convertView = View.inflate(this@SexualDayRecordActivity, R.layout.inflate_list_item_sexual_day, null)
                val sexualDay = sexualDays[position]
                var nextDateTime = DateTime()
                if (position > 0) {
                    nextDateTime = sexualDays[position - 1].dateTime
                }
                val interval = nextDateTime.timeInMillis - sexualDay.dateTime.timeInMillis
                val layoutRoot = convertView.findViewById<View>(R.id.layout_root) as RelativeLayout
                val textViewStartDay = convertView.findViewById<View>(R.id.textView_startDay) as TextView
                val textViewStartTime = convertView.findViewById<View>(R.id.textView_startTime) as TextView
                val textViewInterval = convertView.findViewById<View>(R.id.textView_interval) as TextView
                val layoutOperate = convertView.findViewById<View>(R.id.layout_operate) as LinearLayout
                val layoutEdit = convertView.findViewById<View>(R.id.layout_edit) as FrameLayout
                val layoutDel = convertView.findViewById<View>(R.id.layout_del) as FrameLayout
                val progressBar = convertView.findViewById<View>(R.id.progressBar) as ProgressBar
                layoutEdit.setOnClickListener { showEditSexualDayDialog(sexualDay) }
                layoutDel.setOnClickListener {
                    try {
                        AlertDialog.Builder(this@SexualDayRecordActivity).setTitle("删除确认").setMessage("是否要删除当前记录?").setPositiveButton("确认") { dialog, which ->
                            dataContext.deleteSexualDay(sexualDays[position].id)
                            sexualDays.removeAt(position)
                            recordListdAdapter.notifyDataSetChanged()
                            isDataChanged = true
                            dialog.cancel()
                            initSummary()
                            snackbar("删除成功")
                        }.setNegativeButton("取消") { dialog, which -> dialog.cancel() }.show()
                    } catch (e: Exception) {
                        _Utils.printException(this@SexualDayRecordActivity, e)
                    }
                }
                val date = sexualDay.dateTime
                textViewStartDay.text = date.getMonthStr() + "月" + date.getDayStr() + "日"
                textViewStartTime.text = date.getHour().toString() + ":" + date.getMiniteStr()
                val aa = (interval/ 3600000).toInt()
                textViewInterval.text = DateTime.toSpanString(aa)
                //
                if (max > 0) {
                    progressBar.max = max
                    progressBar.progress = (interval / 3600000).toInt()
                } else {
                    progressBar.max = 100
                    progressBar.progress = 0
                }
            } catch (e: Exception) {
                _Utils.printException(this@SexualDayRecordActivity, e)
            }
            return convertView
        }
    }

    fun showEditSexualDayDialog(sexualDay: SexualDay) {
        val view = View.inflate(this@SexualDayRecordActivity, R.layout.inflate_dialog_date_picker, null)
        val dialog = AlertDialog.Builder(this@SexualDayRecordActivity).setView(view).create()
        dialog.setTitle("设定时间")
        val dateTime = sexualDay.dateTime
        val year = dateTime.getYear()
        val month = dateTime.getMonth()
        //        int maxDay = dateTime.getActualMaximum(Calendar.DAY_OF_MONTH);
        val day = dateTime.getDay()
        val hour = dateTime.getHour()
        val yearNumbers = arrayOfNulls<String>(3)
        for (i in year - 2..year) {
            yearNumbers[i - year + 2] = i.toString() + "年"
        }
        val monthNumbers = arrayOfNulls<String>(12)
        for (i in 0..11) {
            monthNumbers[i] ="${i + 1}月"
        }
        val dayNumbers = arrayOfNulls<String>(31)
        for (i in 0..30) {
            dayNumbers[i] = "${i + 1}日"
        }
        val hourNumbers = arrayOfNulls<String>(24)
        for (i in 0..23) {
            hourNumbers[i] = i.toString() + "点"
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
        npHour.minValue = 0
        npHour.maxValue = 23
        npHour.displayedValues = hourNumbers
        npHour.value = hour
        npHour.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 禁止对话框打开后数字选择框被选中
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
                val hour = npHour.value
                val selectedDateTime = DateTime(year, month, day, hour, 0, 0)
                sexualDay.dateTime = selectedDateTime
                dataContext.updateSexualDay(sexualDay)
                recordListdAdapter.notifyDataSetChanged()
                isDataChanged = true
                initSummary()
                dialog.dismiss()
            } catch (e: Exception) {
                _Utils.printException(this@SexualDayRecordActivity, e)
            }
        }
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消") { dialog, which ->
            try {
                dialog.dismiss()
            } catch (e: Exception) {
                _Utils.printException(this@SexualDayRecordActivity, e)
            }
        }
        dialog.show()
    }

    private fun snackbar(message: String) {
        try {
            Snackbar.make(textView_time1, message, Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("wangsc", e.message!!)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    companion object {
        var isDataChanged: Boolean? = null
    }
}