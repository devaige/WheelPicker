package com.aigestudio.wheelpicker.widgets

import android.content.Context
import android.util.AttributeSet
import com.aigestudio.wheelpicker.WheelPicker
import java.util.*

/**
 * 月份选择器
 *
 * Picker for Months
 *
 * @author AigeStudio 2016-07-12
 * @version 1
 */
class WheelMonthPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WheelPicker(context, attrs), IWheelMonthPicker {

    private var _selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    override var selectedMonth: Int
        get() = _selectedMonth
        set(value) {
            _selectedMonth = value
            updateSelectedYear()
        }

    override val currentMonth: Int
        get() = Integer.valueOf(data!![currentItemPosition].toString())

    override var data: List<*>?
        get() = super.data
        set(_) {
            throw UnsupportedOperationException("You can not invoke setData in WheelMonthPicker")
        }

    init {
        val dataList = ArrayList<Int>()
        for (i in 1..12) {
            dataList.add(i)
        }
        super.data = dataList
        updateSelectedYear()
    }

    private fun updateSelectedYear() {
        selectedItemPosition = _selectedMonth - 1
    }
}
