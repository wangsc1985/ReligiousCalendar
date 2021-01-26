package com.wang17.religiouscalendar.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import com.wang17.religiouscalendar.R
import com.wang17.religiouscalendar.model.DataContext
import com.wang17.religiouscalendar.model.DateTime
import com.wang17.religiouscalendar.model.SexualDay
import com.wang17.religiouscalendar.util._String
import com.wang17.religiouscalendar.util._Utils
import kotlinx.android.synthetic.main.fragment_action_bar.*
import java.util.*

class ActionBarFragment : Fragment() {
    // 类变量
    private var backListener: OnActionFragmentBackListener?=null
    private lateinit var dataContext: DataContext
    override fun onCreate(savedInstanceState: Bundle?) {
        // 初始化类变量和值变量
        super.onCreate(savedInstanceState)
        try {
            dataContext = DataContext(activity)
        } catch (e: Exception) {
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //
        imageView_back.setOnClickListener {
                backListener?.onBackListener()
        }
        if (backListener == null) {
            imageView_back.visibility = View.INVISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_action_bar, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            if (context is OnActionFragmentBackListener) {
                backListener = context
            }
        } catch (e: Exception) {
        }
    }

    private fun setTextViewSexualText() {
        try {
            val lastSexualDay = dataContext.getLastSexualDay()
            if (lastSexualDay == null) {
                textView_title.text = "无记录"
            } else {
                val span = DateTime().timeInMillis - lastSexualDay.dateTime.timeInMillis
                val day = (span / 60000 / 60 / 24).toInt()
                val hour = (span / 60000 / 60 % 24).toInt()
                textView_title.text = _String.concat(if (day > 0) day.toString() + "天" else "", hour.toString() + "小时")
            }
        } catch (e: Exception) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == TO_LIST) {
//            if(SexualDayRecordActivity.lastDateTimeChanged){
            setTextViewSexualText()
            //            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 如果要使用返回按钮，需实现此接口
     */
    interface OnActionFragmentBackListener {
        // 更新参数的类型和名字
        fun onBackListener()
    }

    fun setDateTimeDialog(dateTime: DateTime) {
        try {
            val view = View.inflate(context, R.layout.inflate_dialog_date_picker, null)
            val dialog = AlertDialog.Builder(context).setView(view).create()
            dialog.setTitle("设定时间")
            val year = dateTime.getYear()
            val month = dateTime.getMonth()
            val maxDay = dateTime.getActualMaximum(Calendar.DAY_OF_MONTH)
            val day = dateTime.getDay()
            val hour = dateTime.getHour()
            val yearNumbers = arrayOfNulls<String>(3)
            for (i in year - 2..year) {
                yearNumbers[i - year + 2] = i.toString() + "年"
            }
            val monthNumbers = arrayOfNulls<String>(12)
            for (i in 0..11) {
                monthNumbers[i] = "${i+1}月"
            }
            val dayNumbers = arrayOfNulls<String>(31)
            for (i in 0..30) {
                dayNumbers[i] ="${i+1}日"
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
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定") { dialog, which ->
                try {
                    val y = npYear.value
                    val m = npMonth.value - 1
                    val d = npDay.value
                    val h = npHour.value
                    val selectedDateTime = DateTime(y, m, d, h, 0, 0)
                    val sexualDay = SexualDay(selectedDateTime,"","")
                    dataContext.addSexualDay(sexualDay)
                    setTextViewSexualText()
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        // 值变量
        const val TO_LIST = 0
        fun newInstance(): Fragment {
            return ActionBarFragment()
        }
    }
}