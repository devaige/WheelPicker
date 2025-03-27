package com.aigestudio.wheelpicker

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import com.aigestudio.wheelpicker.IWheelPicker.Companion.ALIGN_CENTER
import com.aigestudio.wheelpicker.IWheelPicker.Companion.ALIGN_LEFT
import com.aigestudio.wheelpicker.IWheelPicker.Companion.ALIGN_RIGHT
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * 滚轮选择器
 *
 * WheelPicker
 *
 * @author AigeStudio 2015-12-12
 * @author AigeStudio 2016-06-17
 * 更新项目结构
 *
 * New project structure
 * @version 1.1.0
 */
open class WheelPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), IDebug, IWheelPicker, Runnable {

    companion object {
        /**
         * 滚动状态标识值
         *
         * @see OnWheelChangeListener.onWheelScrollStateChanged
         */
        const val SCROLL_STATE_IDLE = 0
        const val SCROLL_STATE_DRAGGING = 1
        const val SCROLL_STATE_SCROLLING = 2

        /**
         * 数据项对齐方式标识值
         *
         * @see itemAlign
         */
        const val ALIGN_CENTER = 0
        const val ALIGN_LEFT = 1
        const val ALIGN_RIGHT = 2

        private val TAG = WheelPicker::class.java.simpleName
    }

    private val mHandler = Handler(Looper.getMainLooper())
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.LINEAR_TEXT_FLAG)
    private val mScroller: Scroller = Scroller(context)
    private var mTracker: VelocityTracker? = null

    /**
     * 相关监听器
     *
     * @see OnWheelChangeListener
     * @see OnItemSelectedListener
     */
    private var mOnItemSelectedListener: OnItemSelectedListener? = null
    private var mOnWheelChangeListener: OnWheelChangeListener? = null

    private val mRectDrawn = Rect()
    private val mRectIndicatorHead = Rect()
    private val mRectIndicatorFoot = Rect()
    private val mRectCurrentItem = Rect()

    private val mCamera = Camera()
    private val mMatrixRotate = Matrix()
    private val mMatrixDepth = Matrix()

    /**
     * 数据源
     */
    override var data: List<*>? = null
        set(value) {
            if (value == null) throw NullPointerException("WheelPicker's data can not be null!")
            field = value
            // 重置位置
            if (selectedItemPosition > value.size - 1 || currentItemPosition > value.size - 1) {
                selectedItemPosition = value.size - 1
                // currentItemPosition 会在 selectedItemPosition 的 setter 中更新
            } else {
                // 如果没有越界，也需要重置 currentItemPosition，以防之前的逻辑
                // 但原 Java 逻辑是 mSelectedItemPosition = mCurrentItemPosition
                // 这里的逻辑有点绕，参考 Java:
                // mSelectedItemPosition = mCurrentItemPosition; (in else block)
                // 实际上是保持当前位置
                selectedItemPosition = currentItemPosition
            }
            mScrollOffsetY = 0
            computeTextSize()
            computeFlingLimitY()
            requestLayout()
            invalidate()
        }

    /**
     * 最宽的文本
     *
     * @see maximumWidthText
     */
    override var maximumWidthText: String? = null
        set(value) {
            if (value == null) throw NullPointerException("Maximum width text can not be null!")
            field = value
            computeTextSize()
            requestLayout()
            invalidate()
        }

    /**
     * 滚轮选择器中可见的数据项数量
     *
     * @see visibleItemCount
     */
    override var visibleItemCount: Int = 7
        set(value) {
            field = value
            updateVisibleItemCount()
            requestLayout()
        }

    /**
     * 滚轮选择器将会绘制的数据项数量
     */
    private var mDrawnItemCount = 0

    /**
     * 滚轮选择器将会绘制的Item数量的一半
     */
    private var mHalfDrawnItemCount = 0

    /**
     * 单个文本最大宽高
     */
    private var mTextMaxWidth = 0
    private var mTextMaxHeight = 0

    /**
     * 数据项文本颜色以及被选中的数据项文本颜色
     *
     * @see itemTextColor
     * @see selectedItemTextColor
     */
    override var itemTextColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    override var selectedItemTextColor: Int = -1
        set(value) {
            field = value
            computeCurrentItemRect()
            invalidate()
        }

    /**
     * 数据项文本尺寸
     *
     * @see itemTextSize
     */
    override var itemTextSize: Int = 0
        set(value) {
            field = value
            mPaint.textSize = value.toFloat()
            computeTextSize()
            requestLayout()
            invalidate()
        }

    /**
     * 指示器尺寸
     *
     * @see indicatorSize
     */
    override var indicatorSize: Int = 0
        set(value) {
            field = value
            computeIndicatorRect()
            invalidate()
        }

    /**
     * 指示器颜色
     *
     * @see indicatorColor
     */
    override var indicatorColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 幕布颜色
     *
     * @see curtainColor
     */
    override var curtainColor: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 数据项之间间距
     *
     * @see itemSpace
     */
    override var itemSpace: Int = 0
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    /**
     * 数据项对齐方式
     *
     * @see itemAlign
     */
    override var itemAlign: Int = ALIGN_CENTER
        set(value) {
            field = value
            updateItemTextAlign()
            computeDrawnCenter()
            invalidate()
        }

    /**
     * 滚轮选择器单个数据项高度以及单个数据项一半的高度
     */
    private var mItemHeight = 0
    private var mHalfItemHeight = 0

    /**
     * 滚轮选择器内容区域高度的一半
     */
    private var mHalfWheelHeight = 0

    /**
     * 当前被选中的数据项所显示的数据在数据源中的位置
     *
     * @see selectedItemPosition
     */
    override var selectedItemPosition: Int = 0
        set(value) {
            var pos = value
            val size = data?.size ?: 0
            if (size > 0) {
                pos = min(pos, size - 1)
                pos = max(pos, 0)
            }
            field = pos
            currentItemPosition = pos
            mScrollOffsetY = 0
            computeFlingLimitY()
            requestLayout()
            invalidate()
        }

    /**
     * 当前被选中的数据项所显示的数据在数据源中的位置
     *
     * @see currentItemPosition
     */
    final override var currentItemPosition: Int = 0
        private set

    /**
     * 滚轮滑动时可以滑动到的最小/最大的Y坐标
     */
    private var mMinFlingY = 0
    private var mMaxFlingY = 0

    /**
     * 滚轮滑动时的最小/最大速度
     */
    private var mMinimumVelocity = 50
    private var mMaximumVelocity = 8000

    /**
     * 滚轮选择器中心坐标
     */
    private var mWheelCenterX = 0
    private var mWheelCenterY = 0

    /**
     * 滚轮选择器绘制中心坐标
     */
    private var mDrawnCenterX = 0
    private var mDrawnCenterY = 0

    /**
     * 滚轮选择器视图区域在Y轴方向上的偏移值
     */
    private var mScrollOffsetY = 0

    /**
     * 滚轮选择器中最宽或最高的文本在数据源中的位置
     */
    override var maximumWidthTextPosition: Int = -1
        set(value) {
            if (!isPosInRang(value)) {
                throw ArrayIndexOutOfBoundsException(
                    "Maximum width text Position must in [0, ${data?.size ?: 0}), but current is $value"
                )
            }
            field = value
            computeTextSize()
            requestLayout()
            invalidate()
        }

    /**
     * 用户手指上一次触摸事件发生时事件Y坐标
     */
    private var mLastPointY = 0

    /**
     * 手指触摸屏幕时事件点的Y坐标
     */
    private var mDownPointY = 0

    /**
     * 点击与触摸的切换阀值
     */
    private var mTouchSlop = 8

    /**
     * 滚轮选择器的每一个数据项文本是否拥有相同的宽度
     *
     * @see setSameWidth
     */
    private var hasSameWidth = false

    /**
     * 是否显示指示器
     *
     * @see isIndicator
     */
    override var isIndicator: Boolean = false
        set(value) {
            field = value
            computeIndicatorRect()
            invalidate()
        }

    /**
     * 是否显示幕布
     *
     * @see isCurtain
     */
    override var isCurtain: Boolean = false
        set(value) {
            field = value
            computeCurrentItemRect()
            invalidate()
        }

    /**
     * 是否显示空气感效果
     *
     * @see isAtmospheric
     */
    override var isAtmospheric: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 数据是否循环展示
     *
     * @see isCyclic
     */
    override var isCyclic: Boolean = false
        set(value) {
            field = value
            computeFlingLimitY()
            invalidate()
        }

    /**
     * 滚轮是否为卷曲效果
     *
     * @see isCurved
     */
    override var isCurved: Boolean = false
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    /**
     * 是否为点击模式
     */
    private var isClick = false

    /**
     * 是否为强制结束滑动
     */
    private var isForceFinishScroll = false

    private var isDebug = false

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.WheelPicker)
        val idData = a.getResourceId(R.styleable.WheelPicker_wheel_data, 0)
        
        // 初始化数据
        val dataArray = resources.getStringArray(
            if (idData == 0) R.array.WheelArrayDefault else idData
        )
        // 这里的 data setter 会被调用，但这可能导致一些计算方法在属性尚未完全初始化时被调用
        // 建议先使用幕后字段赋值，最后统一更新
        // 为了安全起见，这里先直接赋值给 field，然后手动调用初始化方法
        // 或者因为 Kotlin 属性初始化的顺序，这些 var 已经在上面被初始化了，所以 setter 是安全的
        
        // 注意：data setter 依赖 selectedItemPosition，所以得小心顺序
        // 这里为了模拟 Java 构造函数的行为，我们尽量直接赋值给 fields，最后调用 update 方法
        
        @Suppress("UNCHECKED_CAST")
        data = dataArray.toList() // 这会触发 setter
        
        itemTextSize = a.getDimensionPixelSize(
            R.styleable.WheelPicker_wheel_item_text_size,
            resources.getDimensionPixelSize(R.dimen.WheelItemTextSize)
        )
        visibleItemCount = a.getInt(R.styleable.WheelPicker_wheel_visible_item_count, 7)
        selectedItemPosition = a.getInt(R.styleable.WheelPicker_wheel_selected_item_position, 0)
        hasSameWidth = a.getBoolean(R.styleable.WheelPicker_wheel_same_width, false)
        maximumWidthTextPosition = a.getInt(R.styleable.WheelPicker_wheel_maximum_width_text_position, -1)
        maximumWidthText = a.getString(R.styleable.WheelPicker_wheel_maximum_width_text)
        selectedItemTextColor = a.getColor(R.styleable.WheelPicker_wheel_selected_item_text_color, -1)
        itemTextColor = a.getColor(R.styleable.WheelPicker_wheel_item_text_color, -0x777778) // 0xFF888888
        itemSpace = a.getDimensionPixelSize(
            R.styleable.WheelPicker_wheel_item_space,
            resources.getDimensionPixelSize(R.dimen.WheelItemSpace)
        )
        isCyclic = a.getBoolean(R.styleable.WheelPicker_wheel_cyclic, false)
        isIndicator = a.getBoolean(R.styleable.WheelPicker_wheel_indicator, false)
        indicatorColor = a.getColor(R.styleable.WheelPicker_wheel_indicator_color, -0x11cccd) // 0xFFEE3333
        indicatorSize = a.getDimensionPixelSize(
            R.styleable.WheelPicker_wheel_indicator_size,
            resources.getDimensionPixelSize(R.dimen.WheelIndicatorSize)
        )
        isCurtain = a.getBoolean(R.styleable.WheelPicker_wheel_curtain, false)
        curtainColor = a.getColor(R.styleable.WheelPicker_wheel_curtain_color, -0x77000001) // 0x88FFFFFF
        isAtmospheric = a.getBoolean(R.styleable.WheelPicker_wheel_atmospheric, false)
        isCurved = a.getBoolean(R.styleable.WheelPicker_wheel_curved, false)
        itemAlign = a.getInt(R.styleable.WheelPicker_wheel_item_align, ALIGN_CENTER)
        a.recycle()

        // 可见数据项改变后更新与之相关的参数
        // Update relevant parameters when the count of visible item changed
        updateVisibleItemCount()

        mPaint.textSize = itemTextSize.toFloat()

        // 更新文本对齐方式
        // Update alignment of text
        updateItemTextAlign()

        // 计算文本尺寸
        // Correct sizes of text
        computeTextSize()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
            val conf = ViewConfiguration.get(context)
            mMinimumVelocity = conf.scaledMinimumFlingVelocity
            mMaximumVelocity = conf.scaledMaximumFlingVelocity
            mTouchSlop = conf.scaledTouchSlop
        }
    }

    private fun updateVisibleItemCount() {
        if (visibleItemCount < 2) throw ArithmeticException("Wheel's visible item count can not be less than 2!")

        // 确保滚轮选择器可见数据项数量为奇数
        // Be sure count of visible item is odd number
        if (visibleItemCount % 2 == 0) visibleItemCount += 1
        mDrawnItemCount = visibleItemCount + 2
        mHalfDrawnItemCount = mDrawnItemCount / 2
    }

    private fun computeTextSize() {
        mTextMaxWidth = 0
        mTextMaxHeight = 0
        if (hasSameWidth) {
            mTextMaxWidth = mPaint.measureText(data!![0].toString()).toInt()
        } else if (isPosInRang(maximumWidthTextPosition)) {
            mTextMaxWidth = mPaint.measureText(data!![maximumWidthTextPosition].toString()).toInt()
        } else if (!TextUtils.isEmpty(maximumWidthText)) {
            mTextMaxWidth = mPaint.measureText(maximumWidthText).toInt()
        } else {
            data?.forEach { obj ->
                val text = obj.toString()
                val width = mPaint.measureText(text).toInt()
                mTextMaxWidth = max(mTextMaxWidth, width)
            }
        }
        val metrics = mPaint.fontMetrics
        mTextMaxHeight = (metrics.bottom - metrics.top).toInt()
    }

    private fun updateItemTextAlign() {
        when (itemAlign) {
            ALIGN_LEFT -> mPaint.textAlign = Paint.Align.LEFT
            ALIGN_RIGHT -> mPaint.textAlign = Paint.Align.RIGHT
            else -> mPaint.textAlign = Paint.Align.CENTER
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)

        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)

        // 计算原始内容尺寸
        // Correct sizes of original content
        var resultWidth = mTextMaxWidth
        var resultHeight = mTextMaxHeight * visibleItemCount + itemSpace * (visibleItemCount - 1)

        // 如果开启弯曲效果则需要重新计算弯曲后的尺寸
        // Correct view sizes again if curved is enable
        if (isCurved) {
            resultHeight = (2 * resultHeight / Math.PI).toInt()
        }
        if (isDebug) Log.i(TAG, "Wheel's content size is ($resultWidth:$resultHeight)")

        // 考虑内边距对尺寸的影响
        // Consideration padding influence the view sizes
        resultWidth += paddingLeft + paddingRight
        resultHeight += paddingTop + paddingBottom
        if (isDebug) Log.i(TAG, "Wheel's size is ($resultWidth:$resultHeight)")

        // 考虑父容器对尺寸的影响
        // Consideration sizes of parent can influence the view sizes
        resultWidth = measureSize(modeWidth, sizeWidth, resultWidth)
        resultHeight = measureSize(modeHeight, sizeHeight, resultHeight)

        setMeasuredDimension(resultWidth, resultHeight)
    }

    private fun measureSize(mode: Int, sizeExpect: Int, sizeActual: Int): Int {
        var realSize = 0
        if (mode == MeasureSpec.EXACTLY) {
            realSize = sizeExpect
        } else {
            realSize = sizeActual
            if (mode == MeasureSpec.AT_MOST) realSize = min(realSize, sizeExpect)
        }
        return realSize
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        // 设置内容区域
        // Set content region
        mRectDrawn.set(
            paddingLeft, paddingTop, width - paddingRight,
            height - paddingBottom
        )
        if (isDebug) Log.i(
            TAG, "Wheel's drawn rect size is (" + mRectDrawn.width() + ":" +
                    mRectDrawn.height() + ") and location is (" + mRectDrawn.left + ":" +
                    mRectDrawn.top + ")"
        )

        // 获取内容区域中心坐标
        // Get the center coordinates of content region
        mWheelCenterX = mRectDrawn.centerX()
        mWheelCenterY = mRectDrawn.centerY()

        // 计算数据项绘制中心
        // Correct item drawn center
        computeDrawnCenter()

        mHalfWheelHeight = mRectDrawn.height() / 2

        mItemHeight = mRectDrawn.height() / visibleItemCount
        mHalfItemHeight = mItemHeight / 2

        // 初始化滑动最大坐标
        // Initialize fling max Y-coordinates
        computeFlingLimitY()

        // 计算指示器绘制区域
        // Correct region of indicator
        computeIndicatorRect()

        // 计算当前选中的数据项区域
        // Correct region of current select item
        computeCurrentItemRect()
    }

    private fun computeDrawnCenter() {
        mDrawnCenterX = when (itemAlign) {
            ALIGN_LEFT -> mRectDrawn.left
            ALIGN_RIGHT -> mRectDrawn.right
            else -> mWheelCenterX
        }
        mDrawnCenterY = (mWheelCenterY - ((mPaint.ascent() + mPaint.descent()) / 2)).toInt()
    }

    private fun computeFlingLimitY() {
        val currentItemOffset = selectedItemPosition * mItemHeight
        mMinFlingY = if (isCyclic) Int.MIN_VALUE else -mItemHeight * ((data?.size ?: 0) - 1) + currentItemOffset
        mMaxFlingY = if (isCyclic) Int.MAX_VALUE else currentItemOffset
    }

    private fun computeIndicatorRect() {
        if (!isIndicator) return
        val halfIndicatorSize = indicatorSize / 2
        val indicatorHeadCenterY = mWheelCenterY + mHalfItemHeight
        val indicatorFootCenterY = mWheelCenterY - mHalfItemHeight
        mRectIndicatorHead.set(
            mRectDrawn.left, indicatorHeadCenterY - halfIndicatorSize,
            mRectDrawn.right, indicatorHeadCenterY + halfIndicatorSize
        )
        mRectIndicatorFoot.set(
            mRectDrawn.left, indicatorFootCenterY - halfIndicatorSize,
            mRectDrawn.right, indicatorFootCenterY + halfIndicatorSize
        )
    }

    private fun computeCurrentItemRect() {
        if (!isCurtain && selectedItemTextColor == -1) return
        mRectCurrentItem.set(
            mRectDrawn.left, mWheelCenterY - mHalfItemHeight, mRectDrawn.right,
            mWheelCenterY + mHalfItemHeight
        )
    }

    override fun onDraw(canvas: Canvas) {
        mOnWheelChangeListener?.onWheelScrolled(mScrollOffsetY)
        
        // 这里的 mItemHeight 在初始化前可能为 0，会导致除零异常，加个保护
        if (mItemHeight == 0) return 

        val drawnDataStartPos = -mScrollOffsetY / mItemHeight - mHalfDrawnItemCount
        var drawnDataPos = drawnDataStartPos + selectedItemPosition
        var drawnOffsetPos = -mHalfDrawnItemCount
        
        while (drawnDataPos < drawnDataStartPos + selectedItemPosition + mDrawnItemCount) {
            var dataStr = ""
            if (isCyclic) {
                // data 可能是 null, 使用 elvis
                val size = data?.size ?: 0
                if (size > 0) {
                    var actualPos = drawnDataPos % size
                    actualPos = if (actualPos < 0) actualPos + size else actualPos
                    dataStr = data!![actualPos].toString()
                }
            } else {
                if (isPosInRang(drawnDataPos)) dataStr = data!![drawnDataPos].toString()
            }
            mPaint.color = itemTextColor
            mPaint.style = Paint.Style.FILL
            val mDrawnItemCenterY = mDrawnCenterY + drawnOffsetPos * mItemHeight +
                    mScrollOffsetY % mItemHeight

            var distanceToCenter = 0
            if (isCurved) {
                // 计算数据项绘制中心距离滚轮中心的距离比率
                // Correct ratio of item's drawn center to wheel center
                val ratio = (mDrawnCenterY - abs(mDrawnCenterY - mDrawnItemCenterY) -
                        mRectDrawn.top) * 1.0F / (mDrawnCenterY - mRectDrawn.top)

                // 计算单位
                // Correct unit
                var unit = 0
                if (mDrawnItemCenterY > mDrawnCenterY) unit = 1 else if (mDrawnItemCenterY < mDrawnCenterY) unit = -1

                var degree = -(1 - ratio) * 90 * unit
                if (degree < -90) degree = -90f
                if (degree > 90) degree = 90f
                distanceToCenter = computeSpace(degree.toInt())

                var transX = mWheelCenterX
                when (itemAlign) {
                    ALIGN_LEFT -> transX = mRectDrawn.left
                    ALIGN_RIGHT -> transX = mRectDrawn.right
                }
                val transY = mWheelCenterY - distanceToCenter

                mCamera.save()
                mCamera.rotateX(degree)
                mCamera.getMatrix(mMatrixRotate)
                mCamera.restore()
                mMatrixRotate.preTranslate(-transX.toFloat(), -transY.toFloat())
                mMatrixRotate.postTranslate(transX.toFloat(), transY.toFloat())

                mCamera.save()
                mCamera.translate(0f, 0f, computeDepth(degree.toInt()).toFloat())
                mCamera.getMatrix(mMatrixDepth)
                mCamera.restore()
                mMatrixDepth.preTranslate(-transX.toFloat(), -transY.toFloat())
                mMatrixDepth.postTranslate(transX.toFloat(), transY.toFloat())

                mMatrixRotate.postConcat(mMatrixDepth)
            }
            if (isAtmospheric) {
                var alpha =
                    ((mDrawnCenterY - abs(mDrawnCenterY - mDrawnItemCenterY)) * 1.0F / mDrawnCenterY * 255).toInt()
                alpha = if (alpha < 0) 0 else alpha
                mPaint.alpha = alpha
            }
            // 根据卷曲与否计算数据项绘制Y方向中心坐标
            // Correct item's drawn centerY base on curved state
            val drawnCenterY = if (isCurved) mDrawnCenterY - distanceToCenter else mDrawnItemCenterY

            // 判断是否需要为当前数据项绘制不同颜色
            // Judges need to draw different color for current item or not
            if (selectedItemTextColor != -1) {
                canvas.save()
                if (isCurved) canvas.concat(mMatrixRotate)
                canvas.clipRect(mRectCurrentItem, Region.Op.DIFFERENCE)
                canvas.drawText(dataStr, mDrawnCenterX.toFloat(), drawnCenterY.toFloat(), mPaint)
                canvas.restore()

                mPaint.color = selectedItemTextColor
                canvas.save()
                if (isCurved) canvas.concat(mMatrixRotate)
                canvas.clipRect(mRectCurrentItem)
                canvas.drawText(dataStr, mDrawnCenterX.toFloat(), drawnCenterY.toFloat(), mPaint)
                canvas.restore()
            } else {
                canvas.save()
                canvas.clipRect(mRectDrawn)
                if (isCurved) canvas.concat(mMatrixRotate)
                canvas.drawText(dataStr, mDrawnCenterX.toFloat(), drawnCenterY.toFloat(), mPaint)
                canvas.restore()
            }
            if (isDebug) {
                canvas.save()
                canvas.clipRect(mRectDrawn)
                mPaint.color = -0x11cccd // 0xFFEE3333
                val lineCenterY = mWheelCenterY + drawnOffsetPos * mItemHeight
                canvas.drawLine(
                    mRectDrawn.left.toFloat(), lineCenterY.toFloat(),
                    mRectDrawn.right.toFloat(), lineCenterY.toFloat(), mPaint
                )
                mPaint.color = -0xccCC12 // 0xFF3333EE
                mPaint.style = Paint.Style.STROKE
                val top = lineCenterY - mHalfItemHeight
                canvas.drawRect(
                    mRectDrawn.left.toFloat(), top.toFloat(),
                    mRectDrawn.right.toFloat(), (top + mItemHeight).toFloat(), mPaint
                )
                canvas.restore()
            }
            drawnDataPos++
            drawnOffsetPos++
        }
        // 是否需要绘制幕布
        // Need to draw curtain or not
        if (isCurtain) {
            mPaint.color = curtainColor
            mPaint.style = Paint.Style.FILL
            canvas.drawRect(mRectCurrentItem, mPaint)
        }
        // 是否需要绘制指示器
        // Need to draw indicator or not
        if (isIndicator) {
            mPaint.color = indicatorColor
            mPaint.style = Paint.Style.FILL
            canvas.drawRect(mRectIndicatorHead, mPaint)
            canvas.drawRect(mRectIndicatorFoot, mPaint)
        }
        if (isDebug) {
            mPaint.color = 0x4433EE33
            mPaint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, paddingLeft.toFloat(), height.toFloat(), mPaint)
            canvas.drawRect(0f, 0f, width.toFloat(), paddingTop.toFloat(), mPaint)
            canvas.drawRect(
                (width - paddingRight).toFloat(), 0f, width.toFloat(),
                height.toFloat(), mPaint
            )
            canvas.drawRect(
                0f, (height - paddingBottom).toFloat(), width.toFloat(),
                height.toFloat(), mPaint
            )
        }
    }

    private fun isPosInRang(position: Int): Boolean {
        return position >= 0 && position < (data?.size ?: 0)
    }

    private fun computeSpace(degree: Int): Int {
        return (sin(Math.toRadians(degree.toDouble())) * mHalfWheelHeight).toInt()
    }

    private fun computeDepth(degree: Int): Int {
        return (mHalfWheelHeight - cos(Math.toRadians(degree.toDouble())) * mHalfWheelHeight).toInt()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mTracker == null) {
            mTracker = VelocityTracker.obtain()
        }
        mTracker?.addMovement(event)
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                    isForceFinishScroll = true
                }
                mLastPointY = event.y.toInt()
                mDownPointY = mLastPointY
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(mDownPointY - event.y) < mTouchSlop) {
                    isClick = true
                    // Kotlin when is exhaustive, but here we just want to break logic flow
                    // In kotlin, we continue execution. To break "case", we don't need 'break'.
                    // But here we need to skip the rest of the code in this case.
                } else {
                    isClick = false
                    mOnWheelChangeListener?.onWheelScrollStateChanged(SCROLL_STATE_DRAGGING)

                    // 滚动内容
                    // Scroll WheelPicker's content
                    val move = event.y - mLastPointY
                    if (abs(move) >= 1) {
                        mScrollOffsetY += move.toInt()
                        mLastPointY = event.y.toInt()
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                if (!isClick) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                        mTracker?.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                    } else {
                        mTracker?.computeCurrentVelocity(1000)
                    }

                    // 根据速度判断是该滚动还是滑动
                    // Judges the WheelPicker is scroll or fling base on current velocity
                    isForceFinishScroll = false
                    val velocity = mTracker?.yVelocity?.toInt() ?: 0
                    if (abs(velocity) > mMinimumVelocity) {
                        mScroller.fling(0, mScrollOffsetY, 0, velocity, 0, 0, mMinFlingY, mMaxFlingY)
                        mScroller.finalY = mScroller.finalY +
                                computeDistanceToEndPoint(mScroller.finalY % mItemHeight)
                    } else {
                        mScroller.startScroll(
                            0, mScrollOffsetY, 0,
                            computeDistanceToEndPoint(mScrollOffsetY % mItemHeight)
                        )
                    }
                    // 校正坐标
                    // Correct coordinates
                    if (!isCyclic) {
                        if (mScroller.finalY > mMaxFlingY) mScroller.finalY =
                            mMaxFlingY else if (mScroller.finalY < mMinFlingY) mScroller.finalY =
                            mMinFlingY
                    }
                    mHandler.post(this)
                    mTracker?.recycle()
                    mTracker = null
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                mTracker?.recycle()
                mTracker = null
            }
        }
        return true
    }

    private fun computeDistanceToEndPoint(remainder: Int): Int {
        return if (abs(remainder) > mHalfItemHeight) {
            if (mScrollOffsetY < 0) -mItemHeight - remainder else mItemHeight - remainder
        } else {
            -remainder
        }
    }

    override fun run() {
        val size = data?.size ?: 0
        if (size == 0) return
        if (mScroller.isFinished && !isForceFinishScroll) {
            if (mItemHeight == 0) return
            var position = (-mScrollOffsetY / mItemHeight + selectedItemPosition) % size
            position = if (position < 0) position + size else position
            if (isDebug) Log.i(TAG, "$position:${data!![position]}:$mScrollOffsetY")
            currentItemPosition = position
            mOnItemSelectedListener?.onItemSelected(this, data!![position], position)
            mOnWheelChangeListener?.onWheelSelected(position)
            mOnWheelChangeListener?.onWheelScrollStateChanged(SCROLL_STATE_IDLE)
        }
        if (mScroller.computeScrollOffset()) {
            mOnWheelChangeListener?.onWheelScrollStateChanged(SCROLL_STATE_SCROLLING)
            mScrollOffsetY = mScroller.currY
            postInvalidate()
            mHandler.postDelayed(this, 16)
        }
    }

    override fun setDebug(isDebug: Boolean) {
        this.isDebug = isDebug
    }

    override fun setSameWidth(hasSameSize: Boolean) {
        this.hasSameWidth = hasSameSize
        computeTextSize()
        requestLayout()
        invalidate()
    }

    override fun hasSameWidth(): Boolean {
        return hasSameWidth
    }

    override fun setOnWheelChangeListener(listener: OnWheelChangeListener?) {
        mOnWheelChangeListener = listener
    }

    override fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        mOnItemSelectedListener = listener
    }

    // 这些 getTypeface / setTypeface 方法在 IWheelPicker 接口中已经是属性了
    // 但在 View 中，getTypeface 是存在的吗？ View 没有 getTypeface，TextView 有。
    // WheelPicker 继承自 View。
    // IWheelPicker 定义了 `var typeface: Typeface?`
    // 所以这里我们需要 override 属性

    override var typeface: Typeface?
        get() = mPaint.typeface
        set(value) {
            mPaint.typeface = value
            computeTextSize()
            requestLayout()
            invalidate()
        }


    /**
     * 滚轮选择器Item项被选中时监听接口
     *
     * @author AigeStudio 2016-06-17
     * 新项目结构
     * @version 1.1.0
     */
    interface OnItemSelectedListener {
        /**
         * 当滚轮选择器数据项被选中时回调该方法
         * 滚动选择器滚动停止后会回调该方法并将当前选中的数据和数据在数据列表中对应的位置返回
         *
         * @param picker   滚轮选择器
         * @param data     当前选中的数据
         * @param position 当前选中的数据在数据列表中的位置
         */
        fun onItemSelected(picker: WheelPicker, data: Any?, position: Int)
    }

    /**
     * 滚轮选择器滚动时监听接口
     *
     * @author AigeStudio 2016-06-17
     * 新项目结构
     *
     * New project structure
     * @since 2016-06-17
     */
    interface OnWheelChangeListener {
        /**
         * 当滚轮选择器滚动时回调该方法
         * 滚轮选择器滚动时会将当前滚动位置与滚轮初始位置之间的偏移距离返回，该偏移距离有正负之分，正值表示
         * 滚轮正在往上滚动，负值则表示滚轮正在往下滚动
         *
         * Invoke when WheelPicker scroll stopped
         * WheelPicker will return a distance offset which between current scroll position and
         * initial position, this offset is a positive or a negative, positive means WheelPicker is
         * scrolling from bottom to top, negative means WheelPicker is scrolling from top to bottom
         *
         * @param offset 当前滚轮滚动距离上一次滚轮滚动停止后偏移的距离
         *
         * Distance offset which between current scroll position and initial position
         */
        fun onWheelScrolled(offset: Int)

        /**
         * 当滚轮选择器停止后回调该方法
         * 滚轮选择器停止后会回调该方法并将当前选中的数据项在数据列表中的位置返回
         *
         * Invoke when WheelPicker scroll stopped
         * This method will be called when WheelPicker stop and return current selected item data's
         * position in list
         *
         * @param position 当前选中的数据项在数据列表中的位置
         *
         * Current selected item data's position in list
         */
        fun onWheelSelected(position: Int)

        /**
         * 当滚轮选择器滚动状态改变时回调该方法
         * 滚动选择器的状态总是会在静止、拖动和滑动三者之间切换，当状态改变时回调该方法
         *
         * Invoke when WheelPicker's scroll state changed
         * The state of WheelPicker always between idle, dragging, and scrolling, this method will
         * be called when they switch
         *
         * @param state 滚轮选择器滚动状态，其值仅可能为下列之一
         * [WheelPicker.SCROLL_STATE_IDLE]
         * 表示滚动选择器处于静止状态
         * [WheelPicker.SCROLL_STATE_DRAGGING]
         * 表示滚动选择器处于拖动状态
         * [WheelPicker.SCROLL_STATE_SCROLLING]
         * 表示滚动选择器处于滑动状态
         *
         * State of WheelPicker, only one of the following
         * [WheelPicker.SCROLL_STATE_IDLE]
         * Express WheelPicker in state of idle
         * [WheelPicker.SCROLL_STATE_DRAGGING]
         * Express WheelPicker in state of dragging
         * [WheelPicker.SCROLL_STATE_SCROLLING]
         * Express WheelPicker in state of scrolling
         */
        fun onWheelScrollStateChanged(state: Int)
    }
}
