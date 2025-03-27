package com.aigestudio.wheelpicker.widgets

/**
 * 日期选择器方法接口
 *
 * Interface of WheelDayPicker
 *
 * @author AigeStudio 2016-07-12
 * @version 1
 */
interface IWheelDayPicker {
    /**
     * 获取或设置日期选择器初始化时选择的日期
     *
     * Get or set selected day when init
     */
    var selectedDay: Int

    /**
     * 获取当前选择的日期
     *
     * Get current selected day
     */
    val currentDay: Int

    /**
     * 设置年份和月份
     *
     * @param year  年份
     * @param month 月份
     */
    fun setYearAndMonth(year: Int, month: Int)

    /**
     * 获取或设置年份
     *
     * Get or set year
     */
    var year: Int

    /**
     * 获取或设置月份
     *
     * Get or set month
     */
    var month: Int
}
