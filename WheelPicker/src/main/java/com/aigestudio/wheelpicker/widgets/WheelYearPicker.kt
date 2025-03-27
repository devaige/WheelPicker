package com.aigestudio.wheelpicker.widgets

import android.content.Context
import android.util.AttributeSet
import com.aigestudio.wheelpicker.WheelPicker
import java.util.*

/**
 * 年份选择器
 *
 * Picker for Years
 *
 * @author AigeStudio 2016-07-12
 * @version 1
 */
class WheelYearPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WheelPicker(context, attrs), IWheelYearPicker {

    private var _yearStart = 1000
    private var _yearEnd = 3000
    private var _selectedYear = Calendar.getInstance().get(Calendar.YEAR)

    override var yearStart: Int
        get() = _yearStart
        set(value) {
            _yearStart = value
            _selectedYear = currentYear
            updateYears()
            updateSelectedYear()
        }

    override var yearEnd: Int
        get() = _yearEnd
        set(value) {
            _yearEnd = value
            updateYears()
        }

    override var selectedYear: Int
        get() = _selectedYear
        set(value) {
            _selectedYear = value
            updateSelectedYear()
        }

    override val currentYear: Int
        get() = Integer.valueOf(data!![currentItemPosition].toString())

    override var data: List<*>?
        get() = super.data
        set(_) {
            throw UnsupportedOperationException("You can not invoke setData in WheelYearPicker")
        }

    init {
        updateYears()
        updateSelectedYear()
    }

    private fun updateYears() {
        val dataList = ArrayList<Int>()
        for (i in _yearStart.._yearEnd) {
            dataList.add(i)
        }
        super.data = dataList
    }

    private fun updateSelectedYear() {
        selectedItemPosition = _selectedYear - _yearStart
    }

    override fun setYearFrame(start: Int, end: Int) {
        _yearStart = start
        _yearEnd = end
        _selectedYear = currentYear
        updateYears()
        updateSelectedYear()
    }
}
