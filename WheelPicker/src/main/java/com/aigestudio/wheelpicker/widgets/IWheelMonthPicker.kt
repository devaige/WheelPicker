package com.aigestudio.wheelpicker.widgets

/**
 * 月份选择器方法接口
 *
 * Interface of WheelMonthPicker
 *
 * @author AigeStudio 2016-07-12
 * @version 1
 */
interface IWheelMonthPicker {
    /**
     * 获取或设置月份选择器初始化时选择的月份
     *
     * Get or set selected month when init
     */
    var selectedMonth: Int

    /**
     * 获取当前选择的月份
     *
     * Get current selected month
     */
    val currentMonth: Int
}
