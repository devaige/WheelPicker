package com.aigestudio.wheelpicker

/**
 * 调试模式方法接口
 *
 * @author AigeStudio
 * @since 2011-04-11
 */
interface IDebug {
    /**
     * 设置调试模式
     * 开启调试模式有可能在一定程度上降低代码执行效率，请务必在正式发布时关闭调试模式
     *
     * @param isDebug 是否为调试模式
     */
    fun setDebug(isDebug: Boolean)
}
