package com.aigestudio.wheelpicker

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
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
) : View(context, attrs), IDebug, IWheelPicker {

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

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.LINEAR_TEXT_FLAG)
    private val scroller: Scroller = Scroller(context)
    private var tracker: VelocityTracker? = null

    /**
     * 相关监听器
     *
     * @see OnWheelChangeListener
     * @see OnItemSelectedListener
     */
    private var onItemSelectedListener: OnItemSelectedListener? = null
    private var onWheelChangeListener: OnWheelChangeListener? = null

    private val rectDrawn = Rect()
    private val rectIndicatorHead = Rect()
    private val rectIndicatorFoot = Rect()
    private val rectCurrentItem = Rect()

    private val camera = Camera()
    private val matrixRotate = Matrix()
    private val matrixDepth = Matrix()

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
                selectedItemPosition = currentItemPosition
            }
            scrollOffsetY = 0
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
            if (value != null) {
                field = value
                computeTextSize()
                requestLayout()
                invalidate()
            }
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
    private var drawnItemCount = 0

    /**
     * 滚轮选择器将会绘制的Item数量的一半
     */
    private var halfDrawnItemCount = 0

    /**
     * 单个文本最大宽高
     */
    private var textMaxWidth = 0
    private var textMaxHeight = 0

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
            paint.textSize = value.toFloat()
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
    private var itemHeight = 0
    private var halfItemHeight = 0

    /**
     * 滚轮选择器内容区域高度的一半
     */
    private var halfWheelHeight = 0

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
            scrollOffsetY = 0
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
    private var minFlingY = 0
    private var maxFlingY = 0

    /**
     * 滚轮滑动时的最小/最大速度
     */
    private var minimumVelocity = 50
    private var maximumVelocity = 8000

    /**
     * 滚轮选择器中心坐标
     */
    private var wheelCenterX = 0
    private var wheelCenterY = 0

    /**
     * 滚轮选择器绘制中心坐标
     */
    private var drawnCenterX = 0
    private var drawnCenterY = 0

    /**
     * 滚轮选择器视图区域在Y轴方向上的偏移值
     */
    private var scrollOffsetY = 0

    /**
     * 滚轮选择器中最宽或最高的文本在数据源中的位置
     */
    override var maximumWidthTextPosition: Int = -1
        set(value) {
            if (isPosInRang(value)) {
                field = value
                computeTextSize()
                requestLayout()
                invalidate()
            }
        }

    /**
     * 用户手指上一次触摸事件发生时事件Y坐标
     */
    private var lastPointY = 0

    /**
     * 手指触摸屏幕时事件点的Y坐标
     */
    private var downPointY = 0

    /**
     * 点击与触摸的切换阀值
     */
    private var touchSlop = 8

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
    
    /**
     * 是否正在滚动
     */
    private var isScrolling = false

    private var isDebug = false

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.WheelPicker)
        val idData = a.getResourceId(R.styleable.WheelPicker_wheel_data, 0)
        
        val dataArray = resources.getStringArray(
            if (idData == 0) R.array.WheelArrayDefault else idData
        )
        
        @Suppress("UNCHECKED_CAST")
        data = dataArray.toList()
        
        itemTextSize = a.getDimensionPixelSize(
            R.styleable.WheelPicker_wheel_item_text_size,
            resources.getDimensionPixelSize(R.dimen.WheelItemTextSize)
        )
        visibleItemCount = a.getInt(R.styleable.WheelPicker_wheel_visible_item_count, 7)
        selectedItemPosition = a.getInt(R.styleable.WheelPicker_wheel_selected_item_position, 0)
        hasSameWidth = a.getBoolean(R.styleable.WheelPicker_wheel_same_width, false)
        val pos = a.getInt(R.styleable.WheelPicker_wheel_maximum_width_text_position, -1)
        if (pos != -1) {
            maximumWidthTextPosition = pos
        }
        val maxWidthText = a.getString(R.styleable.WheelPicker_wheel_maximum_width_text)
        if (maxWidthText != null) {
            maximumWidthText = maxWidthText
        }
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

        updateVisibleItemCount()

        paint.textSize = itemTextSize.toFloat()

        updateItemTextAlign()

        computeTextSize()

        val conf = ViewConfiguration.get(context)
        minimumVelocity = conf.scaledMinimumFlingVelocity
        maximumVelocity = conf.scaledMaximumFlingVelocity
        touchSlop = conf.scaledTouchSlop
    }

    private fun updateVisibleItemCount() {
        if (visibleItemCount < 2) throw ArithmeticException("Wheel's visible item count can not be less than 2!")

        if (visibleItemCount % 2 == 0) visibleItemCount += 1
        drawnItemCount = visibleItemCount + 2
        halfDrawnItemCount = drawnItemCount / 2
    }

    private fun computeTextSize() {
        textMaxWidth = 0
        textMaxHeight = 0
        if (hasSameWidth) {
            textMaxWidth = paint.measureText(data!![0].toString()).toInt()
        } else if (isPosInRang(maximumWidthTextPosition)) {
            textMaxWidth = paint.measureText(data!![maximumWidthTextPosition].toString()).toInt()
        } else if (!TextUtils.isEmpty(maximumWidthText)) {
            textMaxWidth = paint.measureText(maximumWidthText).toInt()
        } else {
            data?.forEach { obj ->
                val text = obj.toString()
                val width = paint.measureText(text).toInt()
                textMaxWidth = max(textMaxWidth, width)
            }
        }
        val metrics = paint.fontMetrics
        textMaxHeight = (metrics.bottom - metrics.top).toInt()
    }

    private fun updateItemTextAlign() {
        when (itemAlign) {
            ALIGN_LEFT -> paint.textAlign = Paint.Align.LEFT
            ALIGN_RIGHT -> paint.textAlign = Paint.Align.RIGHT
            else -> paint.textAlign = Paint.Align.CENTER
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)

        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)

        var resultWidth = textMaxWidth
        var resultHeight = textMaxHeight * visibleItemCount + itemSpace * (visibleItemCount - 1)

        if (isCurved) {
            resultHeight = (2 * resultHeight / Math.PI).toInt()
        }
        if (isDebug) Log.i(TAG, "Wheel's content size is ($resultWidth:$resultHeight)")

        resultWidth += paddingLeft + paddingRight
        resultHeight += paddingTop + paddingBottom
        if (isDebug) Log.i(TAG, "Wheel's size is ($resultWidth:$resultHeight)")

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
        rectDrawn.set(
            paddingLeft, paddingTop, width - paddingRight,
            height - paddingBottom
        )
        if (isDebug) Log.i(
            TAG, "Wheel's drawn rect size is (" + rectDrawn.width() + ":" +
                    rectDrawn.height() + ") and location is (" + rectDrawn.left + ":" +
                    rectDrawn.top + ")"
        )

        wheelCenterX = rectDrawn.centerX()
        wheelCenterY = rectDrawn.centerY()

        computeDrawnCenter()

        halfWheelHeight = rectDrawn.height() / 2

        itemHeight = rectDrawn.height() / visibleItemCount
        halfItemHeight = itemHeight / 2

        computeFlingLimitY()

        computeIndicatorRect()

        computeCurrentItemRect()
    }

    private fun computeDrawnCenter() {
        drawnCenterX = when (itemAlign) {
            ALIGN_LEFT -> rectDrawn.left
            ALIGN_RIGHT -> rectDrawn.right
            else -> wheelCenterX
        }
        drawnCenterY = (wheelCenterY - ((paint.ascent() + paint.descent()) / 2)).toInt()
    }

    private fun computeFlingLimitY() {
        val currentItemOffset = selectedItemPosition * itemHeight
        minFlingY = if (isCyclic) Int.MIN_VALUE else -itemHeight * ((data?.size ?: 0) - 1) + currentItemOffset
        maxFlingY = if (isCyclic) Int.MAX_VALUE else currentItemOffset
    }

    private fun computeIndicatorRect() {
        if (!isIndicator) return
        val halfIndicatorSize = indicatorSize / 2
        val indicatorHeadCenterY = wheelCenterY + halfItemHeight
        val indicatorFootCenterY = wheelCenterY - halfItemHeight
        rectIndicatorHead.set(
            rectDrawn.left, indicatorHeadCenterY - halfIndicatorSize,
            rectDrawn.right, indicatorHeadCenterY + halfIndicatorSize
        )
        rectIndicatorFoot.set(
            rectDrawn.left, indicatorFootCenterY - halfIndicatorSize,
            rectDrawn.right, indicatorFootCenterY + halfIndicatorSize
        )
    }

    private fun computeCurrentItemRect() {
        if (!isCurtain && selectedItemTextColor == -1) return
        rectCurrentItem.set(
            rectDrawn.left, wheelCenterY - halfItemHeight, rectDrawn.right,
            wheelCenterY + halfItemHeight
        )
    }

    override fun onDraw(canvas: Canvas) {
        onWheelChangeListener?.onWheelScrolled(scrollOffsetY)
        
        if (itemHeight == 0) return 

        val drawnDataStartPos = -scrollOffsetY / itemHeight - halfDrawnItemCount
        var drawnDataPos = drawnDataStartPos + selectedItemPosition
        var drawnOffsetPos = -halfDrawnItemCount
        
        while (drawnDataPos < drawnDataStartPos + selectedItemPosition + drawnItemCount) {
            var dataStr = ""
            if (isCyclic) {
                val size = data?.size ?: 0
                if (size > 0) {
                    var actualPos = drawnDataPos % size
                    actualPos = if (actualPos < 0) actualPos + size else actualPos
                    dataStr = data!![actualPos].toString()
                }
            } else {
                if (isPosInRang(drawnDataPos)) dataStr = data!![drawnDataPos].toString()
            }
            paint.color = itemTextColor
            paint.style = Paint.Style.FILL
            val drawnItemCenterY = drawnCenterY + drawnOffsetPos * itemHeight +
                    scrollOffsetY % itemHeight

            var distanceToCenter = 0
            if (isCurved) {
                val ratio = (drawnCenterY - abs(drawnCenterY - drawnItemCenterY) -
                        rectDrawn.top) * 1.0F / (drawnCenterY - rectDrawn.top)

                var unit = 0
                if (drawnItemCenterY > drawnCenterY) unit = 1 else if (drawnItemCenterY < drawnCenterY) unit = -1

                var degree = -(1 - ratio) * 90 * unit
                if (degree < -90) degree = -90f
                if (degree > 90) degree = 90f
                distanceToCenter = computeSpace(degree.toInt())

                var transX = wheelCenterX
                when (itemAlign) {
                    ALIGN_LEFT -> transX = rectDrawn.left
                    ALIGN_RIGHT -> transX = rectDrawn.right
                }
                val transY = wheelCenterY - distanceToCenter

                camera.save()
                camera.rotateX(degree)
                camera.getMatrix(matrixRotate)
                camera.restore()
                matrixRotate.preTranslate(-transX.toFloat(), -transY.toFloat())
                matrixRotate.postTranslate(transX.toFloat(), transY.toFloat())

                camera.save()
                camera.translate(0f, 0f, computeDepth(degree.toInt()).toFloat())
                camera.getMatrix(matrixDepth)
                camera.restore()
                matrixDepth.preTranslate(-transX.toFloat(), -transY.toFloat())
                matrixDepth.postTranslate(transX.toFloat(), transY.toFloat())

                matrixRotate.postConcat(matrixDepth)
            }
            if (isAtmospheric) {
                var alpha =
                    ((drawnCenterY - abs(drawnCenterY - drawnItemCenterY)) * 1.0F / drawnCenterY * 255).toInt()
                alpha = if (alpha < 0) 0 else alpha
                paint.alpha = alpha
            }
            val itemDrawnCenterY = if (isCurved) drawnCenterY - distanceToCenter else drawnItemCenterY

            if (selectedItemTextColor != -1) {
                canvas.save()
                if (isCurved) canvas.concat(matrixRotate)
                canvas.clipRect(rectCurrentItem, Region.Op.DIFFERENCE)
                canvas.drawText(dataStr, drawnCenterX.toFloat(), itemDrawnCenterY.toFloat(), paint)
                canvas.restore()

                paint.color = selectedItemTextColor
                canvas.save()
                if (isCurved) canvas.concat(matrixRotate)
                canvas.clipRect(rectCurrentItem)
                canvas.drawText(dataStr, drawnCenterX.toFloat(), itemDrawnCenterY.toFloat(), paint)
                canvas.restore()
            } else {
                canvas.save()
                canvas.clipRect(rectDrawn)
                if (isCurved) canvas.concat(matrixRotate)
                canvas.drawText(dataStr, drawnCenterX.toFloat(), itemDrawnCenterY.toFloat(), paint)
                canvas.restore()
            }
            if (isDebug) {
                canvas.save()
                canvas.clipRect(rectDrawn)
                paint.color = -0x11cccd // 0xFFEE3333
                val lineCenterY = wheelCenterY + drawnOffsetPos * itemHeight
                canvas.drawLine(
                    rectDrawn.left.toFloat(), lineCenterY.toFloat(),
                    rectDrawn.right.toFloat(), lineCenterY.toFloat(), paint
                )
                paint.color = -0xccCC12 // 0xFF3333EE
                paint.style = Paint.Style.STROKE
                val top = lineCenterY - halfItemHeight
                canvas.drawRect(
                    rectDrawn.left.toFloat(), top.toFloat(),
                    rectDrawn.right.toFloat(), (top + itemHeight).toFloat(), paint
                )
                canvas.restore()
            }
            drawnDataPos++
            drawnOffsetPos++
        }
        if (isCurtain) {
            paint.color = curtainColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(rectCurrentItem, paint)
        }
        if (isIndicator) {
            paint.color = indicatorColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(rectIndicatorHead, paint)
            canvas.drawRect(rectIndicatorFoot, paint)
        }
        if (isDebug) {
            paint.color = 0x4433EE33
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, paddingLeft.toFloat(), height.toFloat(), paint)
            canvas.drawRect(0f, 0f, width.toFloat(), paddingTop.toFloat(), paint)
            canvas.drawRect(
                (width - paddingRight).toFloat(), 0f, width.toFloat(),
                height.toFloat(), paint
            )
            canvas.drawRect(
                0f, (height - paddingBottom).toFloat(), width.toFloat(),
                height.toFloat(), paint
            )
        }
    }

    private fun isPosInRang(position: Int): Boolean {
        return position >= 0 && position < (data?.size ?: 0)
    }

    private fun computeSpace(degree: Int): Int {
        return (sin(Math.toRadians(degree.toDouble())) * halfWheelHeight).toInt()
    }

    private fun computeDepth(degree: Int): Int {
        return (halfWheelHeight - cos(Math.toRadians(degree.toDouble())) * halfWheelHeight).toInt()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (tracker == null) {
            tracker = VelocityTracker.obtain()
        }
        tracker?.addMovement(event)
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                if (!scroller.isFinished) {
                    scroller.abortAnimation()
                    isForceFinishScroll = true
                }
                isScrolling = false // 任何按下操作都意味着之前的自动滚动必须结束（如果还在滚的话）
                lastPointY = event.y.toInt()
                downPointY = lastPointY
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(downPointY - event.y) < touchSlop) {
                    isClick = true
                } else {
                    isClick = false
                    onWheelChangeListener?.onWheelScrollStateChanged(SCROLL_STATE_DRAGGING)

                    val move = event.y - lastPointY
                    if (abs(move) >= 1) {
                        scrollOffsetY += move.toInt()
                        lastPointY = event.y.toInt()
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                if (!isClick) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                        tracker?.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                    } else {
                        tracker?.computeCurrentVelocity(1000)
                    }

                    isForceFinishScroll = false
                    val velocity = tracker?.yVelocity?.toInt() ?: 0
                    if (abs(velocity) > minimumVelocity) {
                        scroller.fling(0, scrollOffsetY, 0, velocity, 0, 0, minFlingY, maxFlingY)
                        scroller.finalY = scroller.finalY +
                                computeDistanceToEndPoint(scroller.finalY % itemHeight)
                    } else {
                        scroller.startScroll(
                            0, scrollOffsetY, 0,
                            computeDistanceToEndPoint(scrollOffsetY % itemHeight)
                        )
                    }
                    if (!isCyclic) {
                        if (scroller.finalY > maxFlingY) scroller.finalY =
                            maxFlingY else if (scroller.finalY < minFlingY) scroller.finalY =
                            minFlingY
                    }
                    
                    // 开始滚动
                    isScrolling = true
                    invalidate()
                    
                    tracker?.recycle()
                    tracker = null
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                tracker?.recycle()
                tracker = null
            }
        }
        return true
    }

    private fun computeDistanceToEndPoint(remainder: Int): Int {
        return if (abs(remainder) > halfItemHeight) {
            if (scrollOffsetY < 0) -itemHeight - remainder else itemHeight - remainder
        } else {
            -remainder
        }
    }

    override fun computeScroll() {
        if (isForceFinishScroll) return
        
        if (scroller.computeScrollOffset()) {
            onWheelChangeListener?.onWheelScrollStateChanged(SCROLL_STATE_SCROLLING)
            scrollOffsetY = scroller.currY
            postInvalidate()
        } else {
            // 滚动停止
            if (isScrolling) {
                isScrolling = false
                val size = data?.size ?: 0
                if (size > 0) {
                     var position = (-scrollOffsetY / itemHeight + selectedItemPosition) % size
                     position = if (position < 0) position + size else position
                     if (isDebug) Log.i(TAG, "$position:${data!![position]}:$scrollOffsetY")
                     
                     currentItemPosition = position
                     onItemSelectedListener?.onItemSelected(this, data!![position], position)
                     onWheelChangeListener?.onWheelSelected(position)
                }
                onWheelChangeListener?.onWheelScrollStateChanged(SCROLL_STATE_IDLE)
            }
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
        onWheelChangeListener = listener
    }

    override fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        onItemSelectedListener = listener
    }

    override var typeface: Typeface?
        get() = paint.typeface
        set(value) {
            paint.typeface = value
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
