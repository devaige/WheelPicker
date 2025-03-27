package com.aigestudio.wheelpicker

import android.graphics.Typeface

/**
 * 滚轮选择器方法接口
 *
 * Interface of WheelPicker
 *
 * @author AigeStudio 2015-12-03
 * @author AigeStudio 2015-12-08
 * @author AigeStudio 2015-12-12
 * @author AigeStudio 2016-06-17
 * 更新项目结构
 *
 * New project structure
 * @version 1.1.0
 */
interface IWheelPicker {
    companion object {
        const val ALIGN_CENTER = 0
        const val ALIGN_LEFT = 1
        const val ALIGN_RIGHT = 2
    }

    /**
     * 获取或设置滚轮选择器可见数据项的数量
     * 滚轮选择器的可见数据项数量必须为大于1的整数
     * 这里需要注意的是，滚轮选择器会始终显示奇数个数据项，即便你为其设置偶数个数据项，最终也会被转换为奇数
     * 默认情况下滚轮选择器可见数据项数量为7
     *
     * Get or set the count of current visible items in WheelPicker
     * The count of current visible items in WheelPicker must greater than 1
     * Notice:count of current visible items in WheelPicker will always is an odd number, even you
     * can set an even number for it, it will be change to an odd number eventually
     * By default, the count of current visible items in WheelPicker is 7
     */
    var visibleItemCount: Int

    /**
     * 获取或设置滚轮选择器数据项是否为循环状态
     * 开启数据循环会使滚轮选择器上下滚动不再有边界，会呈现数据首尾相接无限循环的效果
     *
     * Get or set whether WheelPicker is cyclic or not
     * WheelPicker's items will be end to end and in an infinite loop if setCyclic true, and there
     * is no border whit scroll when WheelPicker in cyclic state
     */
    var isCyclic: Boolean

    /**
     * 设置滚轮Item选中监听器
     *
     * @param listener 滚轮Item选中监听器[WheelPicker.OnItemSelectedListener]
     */
    fun setOnItemSelectedListener(listener: WheelPicker.OnItemSelectedListener?)

    /**
     * 获取或设置当前被选中的数据项所显示的数据在数据源中的位置
     *
     * Get or set the position of current selected item in data source
     */
    var selectedItemPosition: Int

    /**
     * 获取当前被选中的数据项所显示的数据在数据源中的位置
     * 与[selectedItemPosition]不同的是，该方法所返回的结果会因为滚轮选择器的改变而改变
     *
     * Get the position of current selected item in data source
     * The difference between [selectedItemPosition], the value this method return will
     * change by WheelPicker scrolled
     */
    val currentItemPosition: Int

    /**
     * 获取或设置数据列表
     *
     * Get or set data source of WheelPicker
     */
    var data: List<*>?

    /**
     * 设置数据项是否有相同的宽度
     *
     * Set items of WheelPicker if has same width
     */
    fun setSameWidth(hasSameSize: Boolean)

    /**
     * 数据项是否有相同宽度
     *
     * Whether items has same width or not
     */
    fun hasSameWidth(): Boolean

    /**
     * 设置滚轮滚动状态改变监听器
     *
     * @param listener 滚轮滚动状态改变监听器
     * @see com.aigestudio.wheelpicker.WheelPicker.OnWheelChangeListener
     */
    fun setOnWheelChangeListener(listener: WheelPicker.OnWheelChangeListener?)

    /**
     * 获取或设置最宽的文本
     *
     * Get or set maximum width text
     */
    var maximumWidthText: String?

    /**
     * 获取或设置最宽的文本在数据源中的位置
     *
     * Get or set the position of maximum width text in data source
     */
    var maximumWidthTextPosition: Int

    /**
     * 获取或设置当前选中的数据项文本颜色
     *
     * Get or set text color of current selected item
     */
    var selectedItemTextColor: Int

    /**
     * 获取或设置数据项文本颜色
     *
     * Get or set text color of items
     */
    var itemTextColor: Int

    /**
     * 获取或设置数据项文本尺寸大小
     *
     * Get or set text size of items
     * Unit in px
     */
    var itemTextSize: Int

    /**
     * 获取或设置滚轮选择器数据项之间间距
     *
     * Get or set space between items
     * Unit in px
     */
    var itemSpace: Int

    /**
     * 获取或设置滚轮选择器是否显示指示器
     *
     * Get or set whether WheelPicker display indicator or not
     */
    var isIndicator: Boolean

    /**
     * 获取或设置滚轮选择器指示器尺寸
     *
     * Get or set size of indicator
     * Unit in px
     */
    var indicatorSize: Int

    /**
     * 获取或设置滚轮选择器指示器颜色
     *
     * Get or set color of indicator
     */
    var indicatorColor: Int

    /**
     * 获取或设置滚轮选择器是否显示幕布
     *
     * Get or set whether WheelPicker display curtain or not
     */
    var isCurtain: Boolean

    /**
     * 获取或设置滚轮选择器幕布颜色
     *
     * Get or set color of curtain
     */
    var curtainColor: Int

    /**
     * 获取或设置滚轮选择器是否有空气感
     *
     * Get or set whether WheelPicker has atmospheric or not
     */
    var isAtmospheric: Boolean

    /**
     * 获取或设置滚轮选择器是否开启卷曲效果
     *
     * Get or set whether WheelPicker enable curved effect or not
     */
    var isCurved: Boolean

    /**
     * 获取或设置滚轮选择器数据项的对齐方式
     *
     * Get or set alignment of WheelPicker
     */
    var itemAlign: Int

    /**
     * 获取或设置数据项文本字体对象
     *
     * Get or set typeface of item text
     */
    var typeface: Typeface?
}
