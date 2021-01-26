package com.wang17.religiouscalendar.activity

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.widget.TextView
import com.wang17.religiouscalendar.R
import com.wang17.religiouscalendar.fragment.ActionBarFragment
import com.wang17.religiouscalendar.fragment.ActionBarFragment.OnActionFragmentBackListener
import kotlinx.android.synthetic.main.activity_introduce.*

class IntroduceActivity : AppCompatActivity(), OnActionFragmentBackListener {
    private lateinit var fontHWZS: Typeface
    private lateinit var fontGF: Typeface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduce)
        val mgr = assets //得到AssetManager
        fontHWZS = Typeface.createFromAsset(mgr, "fonts/STZHONGS.TTF")
        fontGF = Typeface.createFromAsset(mgr, "fonts/GONGFANG.ttf")
        val itemName = intent.getStringExtra(PARAM_NAME)
        textView_introduce.typeface = fontHWZS
        textView_introduce.setLineSpacing(1f, 1.2f)
        textView_introduce.paint.isFakeBoldText = true
        textView_introduce.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
        if (itemName == ItemName.天地人禁忌.toString()) {
            textView_introduce.setText(R.string.tdr)
        } else if (itemName == ItemName.文昌帝君戒淫文.toString()) {
            textView_introduce.setText(R.string.jyw)
        } else if (itemName == ItemName.印光大师序.toString()) {
            textView_introduce.setText(R.string.ygx)
        } else if (itemName == ItemName.升级详细.toString()) {
            textView_introduce.setText(R.string.update_introduce)
        }
    }

    override fun onBackListener() {
        finish()
    }

    enum class ItemName {
        天地人禁忌, 文昌帝君戒淫文, 印光大师序, 升级详细
    }

    companion object {
        const val PARAM_NAME = "ItemName"
    }
}