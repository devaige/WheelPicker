package com.aigestudio.wheelpicker.widgets

/**
 * 年份选择器方法接口
 *
 * Interface of WheelYearPicker
 *
 * @author AigeStudio 2016-07-12
 * @version 1
 */
interface IWheelYearPicker {
    /**
     * 设置年份范围
     *
     * @param start 开始的年份
     * @param end   结束的年份
     */
    fun setYearFrame(start: Int, end: Int)

    /**
     * 获取或设置开始的年份
     *
     * Get or set start year
     */
    var yearStart: Int

    /**
     * 获取或设置结束的年份
     *
     * Get or set end year
     */
    var yearEnd: Int

    /**
     * 获取或设置年份选择器初始化时选中的年份
     *
     * Get or set selected year when init
     */
    var selectedYear: Int

    /**
     * 获取当前选中的年份
     *
     * Get current selected year
     */
    val currentYear: Int
}
