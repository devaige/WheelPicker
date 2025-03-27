package com.aigestudio.wheelpicker.widgets

import android.widget.TextView
import java.util.Date

interface IWheelDatePicker {
    fun setOnDateSelectedListener(listener: WheelDatePicker.OnDateSelectedListener?)

    val currentDate: Date

    var itemAlignYear: Int

    var itemAlignMonth: Int

    var itemAlignDay: Int

    val wheelYearPicker: WheelYearPicker?

    val wheelMonthPicker: WheelMonthPicker?

    val wheelDayPicker: WheelDayPicker?

    val textViewYear: TextView?

    val textViewMonth: TextView?

    val textViewDay: TextView?
}
