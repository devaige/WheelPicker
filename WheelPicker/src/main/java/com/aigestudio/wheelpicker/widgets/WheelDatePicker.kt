package com.aigestudio.wheelpicker.widgets

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.aigestudio.wheelpicker.IDebug
import com.aigestudio.wheelpicker.IWheelPicker
import com.aigestudio.wheelpicker.R
import com.aigestudio.wheelpicker.WheelPicker
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class WheelDatePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs), WheelPicker.OnItemSelectedListener, IDebug, IWheelPicker,
    IWheelDatePicker, IWheelYearPicker, IWheelMonthPicker, IWheelDayPicker {

    companion object {
        private val SDF = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
    }

    private val mPickerYear: WheelYearPicker
    private val mPickerMonth: WheelMonthPicker
    private val mPickerDay: WheelDayPicker

    private var mListener: OnDateSelectedListener? = null

    private val mTVYear: TextView
    private val mTVMonth: TextView
    private val mTVDay: TextView

    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0

    override val wheelYearPicker: WheelYearPicker
        get() = mPickerYear

    override val wheelMonthPicker: WheelMonthPicker
        get() = mPickerMonth

    override val wheelDayPicker: WheelDayPicker
        get() = mPickerDay

    override val textViewYear: TextView
        get() = mTVYear

    override val textViewMonth: TextView
        get() = mTVMonth

    override val textViewDay: TextView
        get() = mTVDay

    init {
        LayoutInflater.from(context).inflate(R.layout.view_wheel_date_picker, this)

        mPickerYear = findViewById(R.id.wheel_date_picker_year)
        mPickerMonth = findViewById(R.id.wheel_date_picker_month)
        mPickerDay = findViewById(R.id.wheel_date_picker_day)
        mPickerYear.setOnItemSelectedListener(this)
        mPickerMonth.setOnItemSelectedListener(this)
        mPickerDay.setOnItemSelectedListener(this)

        setMaximumWidthTextYear()
        mPickerMonth.maximumWidthText = "00"
        mPickerDay.maximumWidthText = "00"

        mTVYear = findViewById(R.id.wheel_date_picker_year_tv)
        mTVMonth = findViewById(R.id.wheel_date_picker_month_tv)
        mTVDay = findViewById(R.id.wheel_date_picker_day_tv)

        mYear = mPickerYear.currentYear
        mMonth = mPickerMonth.currentMonth
        mDay = mPickerDay.currentDay
    }

    private fun setMaximumWidthTextYear() {
        val years = mPickerYear.data ?: return
        if (years.isEmpty()) return
        val lastYear = years.last().toString()
        val sb = StringBuilder()
        for (i in lastYear.indices) sb.append("0")
        mPickerYear.maximumWidthText = sb.toString()
    }

    override fun onItemSelected(picker: WheelPicker, data: Any?, position: Int) {
        if (picker.id == R.id.wheel_date_picker_year) {
            mYear = data.toString().toInt()
            mPickerDay.year = mYear
        } else if (picker.id == R.id.wheel_date_picker_month) {
            mMonth = data.toString().toInt()
            mPickerDay.month = mMonth
        }
        mDay = mPickerDay.currentDay
        val date = "$mYear-$mMonth-$mDay"
        try {
            val parsedDate = SDF.parse(date)
            if (parsedDate != null) {
                mListener?.onDateSelected(this, parsedDate)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    override fun setDebug(isDebug: Boolean) {
        mPickerYear.setDebug(isDebug)
        mPickerMonth.setDebug(isDebug)
        mPickerDay.setDebug(isDebug)
    }

    override var visibleItemCount: Int
        get() {
            if (mPickerYear.visibleItemCount == mPickerMonth.visibleItemCount &&
                mPickerMonth.visibleItemCount == mPickerDay.visibleItemCount
            ) {
                return mPickerYear.visibleItemCount
            }
            throw ArithmeticException("Can not get visible item count correctly from WheelDatePicker!")
        }
        set(count) {
            mPickerYear.visibleItemCount = count
            mPickerMonth.visibleItemCount = count
            mPickerDay.visibleItemCount = count
        }

    override var isCyclic: Boolean
        get() = mPickerYear.isCyclic && mPickerMonth.isCyclic && mPickerDay.isCyclic
        set(isCyclic) {
            mPickerYear.isCyclic = isCyclic
            mPickerMonth.isCyclic = isCyclic
            mPickerDay.isCyclic = isCyclic
        }

    @Deprecated("Unsupported", ReplaceWith("nothing"))
    override fun setOnItemSelectedListener(listener: WheelPicker.OnItemSelectedListener?) {
        throw UnsupportedOperationException("You can not set OnItemSelectedListener for WheelDatePicker")
    }

    @Deprecated("Unsupported", ReplaceWith("nothing"))
    override var selectedItemPosition: Int
        get() = throw UnsupportedOperationException("You can not get position of selected item from WheelDatePicker")
        set(_) {
            throw UnsupportedOperationException("You can not set position of selected item for WheelDatePicker")
        }

    @Deprecated("Unsupported", ReplaceWith("nothing"))
    override val currentItemPosition: Int
        get() = throw UnsupportedOperationException("You can not get position of current item from WheelDatePicker")

    @Deprecated("Unsupported", ReplaceWith("nothing"))
    override var data: List<*>?
        get() = throw UnsupportedOperationException("You can not get data source from WheelDatePicker")
        set(_) {
            throw UnsupportedOperationException("You don't need to set data source for WheelDatePicker")
        }


    @Deprecated("Unsupported", ReplaceWith("nothing"))
    override fun setSameWidth(hasSameSize: Boolean) {
        throw UnsupportedOperationException("You don't need to set same width for WheelDatePicker")
    }

    @Deprecated("Unsupported", ReplaceWith("nothing"))
    override fun hasSameWidth(): Boolean {
        throw UnsupportedOperationException("You don't need to set same width for WheelDatePicker")
    }

    @Deprecated("Unsupported", ReplaceWith("nothing"))
    override fun setOnWheelChangeListener(listener: WheelPicker.OnWheelChangeListener?) {
        throw UnsupportedOperationException("WheelDatePicker unsupport set OnWheelChangeListener")
    }

    @Deprecated("Unsupported", ReplaceWith("nothing"))
    override var maximumWidthText: String?
        get() = throw UnsupportedOperationException("You can not get maximum width text from WheelDatePicker")
        set(_) {
            throw UnsupportedOperationException("You don't need to set maximum width text for WheelDatePicker")
        }

    @Deprecated("Unsupported", ReplaceWith("nothing"))
    override var maximumWidthTextPosition: Int
        get() = throw UnsupportedOperationException("You can not get maximum width text position from WheelDatePicker")
        set(_) {
            throw UnsupportedOperationException("You don't need to set maximum width text position for WheelDatePicker")
        }

    override var selectedItemTextColor: Int
        get() {
            if (mPickerYear.selectedItemTextColor == mPickerMonth.selectedItemTextColor &&
                mPickerMonth.selectedItemTextColor == mPickerDay.selectedItemTextColor
            ) {
                return mPickerYear.selectedItemTextColor
            }
            throw RuntimeException("Can not get color of selected item text correctly from WheelDatePicker!")
        }
        set(color) {
            mPickerYear.selectedItemTextColor = color
            mPickerMonth.selectedItemTextColor = color
            mPickerDay.selectedItemTextColor = color
        }

    override var itemTextColor: Int
        get() {
            if (mPickerYear.itemTextColor == mPickerMonth.itemTextColor &&
                mPickerMonth.itemTextColor == mPickerDay.itemTextColor
            ) {
                return mPickerYear.itemTextColor
            }
            throw RuntimeException("Can not get color of item text correctly from WheelDatePicker!")
        }
        set(color) {
            mPickerYear.itemTextColor = color
            mPickerMonth.itemTextColor = color
            mPickerDay.itemTextColor = color
        }

    override var itemTextSize: Int
        get() {
            if (mPickerYear.itemTextSize == mPickerMonth.itemTextSize &&
                mPickerMonth.itemTextSize == mPickerDay.itemTextSize
            ) {
                return mPickerYear.itemTextSize
            }
            throw RuntimeException("Can not get size of item text correctly from WheelDatePicker!")
        }
        set(size) {
            mPickerYear.itemTextSize = size
            mPickerMonth.itemTextSize = size
            mPickerDay.itemTextSize = size
        }

    override var itemSpace: Int
        get() {
            if (mPickerYear.itemSpace == mPickerMonth.itemSpace &&
                mPickerMonth.itemSpace == mPickerDay.itemSpace
            ) {
                return mPickerYear.itemSpace
            }
            throw RuntimeException("Can not get item space correctly from WheelDatePicker!")
        }
        set(space) {
            mPickerYear.itemSpace = space
            mPickerMonth.itemSpace = space
            mPickerDay.itemSpace = space
        }

    override var isIndicator: Boolean
        get() = mPickerYear.isIndicator && mPickerMonth.isIndicator && mPickerDay.isIndicator
        set(hasIndicator) {
            mPickerYear.isIndicator = hasIndicator
            mPickerMonth.isIndicator = hasIndicator
            mPickerDay.isIndicator = hasIndicator
        }

    override var indicatorSize: Int
        get() {
            if (mPickerYear.indicatorSize == mPickerMonth.indicatorSize &&
                mPickerMonth.indicatorSize == mPickerDay.indicatorSize
            ) {
                return mPickerYear.indicatorSize
            }
            throw RuntimeException("Can not get indicator size correctly from WheelDatePicker!")
        }
        set(size) {
            mPickerYear.indicatorSize = size
            mPickerMonth.indicatorSize = size
            mPickerDay.indicatorSize = size
        }

    override var indicatorColor: Int
        get() {
            if (mPickerYear.indicatorColor == mPickerMonth.indicatorColor &&
                mPickerMonth.indicatorColor == mPickerDay.indicatorColor
            ) {
                return mPickerYear.indicatorColor
            }
            throw RuntimeException("Can not get indicator color correctly from WheelDatePicker!")
        }
        set(color) {
            mPickerYear.indicatorColor = color
            mPickerMonth.indicatorColor = color
            mPickerDay.indicatorColor = color
        }

    override var isCurtain: Boolean
        get() = mPickerYear.isCurtain && mPickerMonth.isCurtain && mPickerDay.isCurtain
        set(hasCurtain) {
            mPickerYear.isCurtain = hasCurtain
            mPickerMonth.isCurtain = hasCurtain
            mPickerDay.isCurtain = hasCurtain
        }

    override var curtainColor: Int
        get() {
            if (mPickerYear.curtainColor == mPickerMonth.curtainColor &&
                mPickerMonth.curtainColor == mPickerDay.curtainColor
            ) {
                return mPickerYear.curtainColor
            }
            throw RuntimeException("Can not get curtain color correctly from WheelDatePicker!")
        }
        set(color) {
            mPickerYear.curtainColor = color
            mPickerMonth.curtainColor = color
            mPickerDay.curtainColor = color
        }

    override var isAtmospheric: Boolean
        get() = mPickerYear.isAtmospheric && mPickerMonth.isAtmospheric && mPickerDay.isAtmospheric
        set(hasAtmospheric) {
            mPickerYear.isAtmospheric = hasAtmospheric
            mPickerMonth.isAtmospheric = hasAtmospheric
            mPickerDay.isAtmospheric = hasAtmospheric
        }

    override var isCurved: Boolean
        get() = mPickerYear.isCurved && mPickerMonth.isCurved && mPickerDay.isCurved
        set(isCurved) {
            mPickerYear.isCurved = isCurved
            mPickerMonth.isCurved = isCurved
            mPickerDay.isCurved = isCurved
        }

    @Deprecated("Unsupported", ReplaceWith("nothing"))
    override var itemAlign: Int
        get() = throw UnsupportedOperationException("You can not get item align from WheelDatePicker")
        set(_) {
            throw UnsupportedOperationException("You don't need to set item align for WheelDatePicker")
        }

    override var typeface: Typeface?
        get() {
            if (mPickerYear.typeface == mPickerMonth.typeface &&
                mPickerMonth.typeface == mPickerDay.typeface
            ) {
                return mPickerYear.typeface
            }
            throw RuntimeException("Can not get typeface correctly from WheelDatePicker!")
        }
        set(tf) {
            mPickerYear.typeface = tf
            mPickerMonth.typeface = tf
            mPickerDay.typeface = tf
        }

    override fun setOnDateSelectedListener(listener: OnDateSelectedListener?) {
        mListener = listener
    }

    override val currentDate: Date
        get() {
            val date = "$mYear-$mMonth-$mDay"
            return try {
                SDF.parse(date) ?: Date()
            } catch (e: ParseException) {
                e.printStackTrace()
                Date()
            }
        }

    override var itemAlignYear: Int
        get() = mPickerYear.itemAlign
        set(align) {
            mPickerYear.itemAlign = align
        }

    override var itemAlignMonth: Int
        get() = mPickerMonth.itemAlign
        set(align) {
            mPickerMonth.itemAlign = align
        }

    override var itemAlignDay: Int
        get() = mPickerDay.itemAlign
        set(align) {
            mPickerDay.itemAlign = align
        }

    override fun setYearFrame(start: Int, end: Int) {
        mPickerYear.setYearFrame(start, end)
    }

    override var yearStart: Int
        get() = mPickerYear.yearStart
        set(start) {
            mPickerYear.yearStart = start
        }

    override var yearEnd: Int
        get() = mPickerYear.yearEnd
        set(end) {
            mPickerYear.yearEnd = end
        }

    override var selectedYear: Int
        get() = mPickerYear.selectedYear
        set(year) {
            mYear = year
            mPickerYear.selectedYear = year
            mPickerDay.year = year
        }

    override val currentYear: Int
        get() = mPickerYear.currentYear

    override var selectedMonth: Int
        get() = mPickerMonth.selectedMonth
        set(month) {
            mMonth = month
            mPickerMonth.selectedMonth = month
            mPickerDay.month = month
        }

    override val currentMonth: Int
        get() = mPickerMonth.currentMonth

    override var selectedDay: Int
        get() = mPickerDay.selectedDay
        set(day) {
            mDay = day
            mPickerDay.selectedDay = day
        }

    override val currentDay: Int
        get() = mPickerDay.currentDay

    override fun setYearAndMonth(year: Int, month: Int) {
        mYear = year
        mMonth = month
        mPickerYear.selectedYear = year
        mPickerMonth.selectedMonth = month
        mPickerDay.setYearAndMonth(year, month)
    }

    override var year: Int
        get() = selectedYear
        set(year) {
            mYear = year
            mPickerYear.selectedYear = year
            mPickerDay.year = year
        }

    override var month: Int
        get() = selectedMonth
        set(month) {
            mMonth = month
            mPickerMonth.selectedMonth = month
            mPickerDay.month = month
        }

    interface OnDateSelectedListener {
        fun onDateSelected(picker: WheelDatePicker, date: Date)
    }
}
