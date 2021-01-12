package com.wang17.religiouscalendar.emnu

/**
 * Created by 阿弥陀佛 on 2015/6/21.
 */
enum class SolarTerm(val value: Int) {
    立春(24), 雨水(1), 惊蛰(2), 春分(3), 清明(4), 谷雨(5), 立夏(6), 小满(7), 芒种(8), 夏至(9), 小暑(10), 大暑(11), 立秋(12), 处暑(13), 白露(14), 秋分(15), 寒露(16), 霜降(17), 立冬(18), 小雪(19), 大雪(20), 冬至(21), 小寒(22), 大寒(23);

    companion object {
        fun Int2SolarTerm(solar: Int): SolarTerm? {
            when (solar) {
                24 -> return 立春
                1 -> return 雨水
                2 -> return 惊蛰
                3 -> return 春分
                4 -> return 清明
                5 -> return 谷雨
                6 -> return 立夏
                7 -> return 小满
                8 -> return 芒种
                9 -> return 夏至
                10 -> return 小暑
                11 -> return 大暑
                12 -> return 立秋
                13 -> return 处暑
                14 -> return 白露
                15 -> return 秋分
                16 -> return 寒露
                17 -> return 霜降
                18 -> return 立冬
                19 -> return 小雪
                20 -> return 大雪
                21 -> return 冬至
                22 -> return 小寒
                23 -> return 大寒
            }
            return null
        }
    }
}