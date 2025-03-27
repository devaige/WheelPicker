package com.aigestudio.wheelpicker.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import com.aigestudio.wheelpicker.WheelPicker
import com.aigestudio.wheelpicker.model.City
import com.aigestudio.wheelpicker.model.Province
import java.io.ObjectInputStream

/**
 * WheelAreaPicker
 * Created by Administrator on 2016/9/14 0014.
 */
class WheelAreaPicker(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs),
    IWheelAreaPicker {

    companion object {
        private const val ITEM_TEXT_SIZE = 18f
        private const val SELECTED_ITEM_COLOR = "#353535"
        private const val PROVINCE_INITIAL_INDEX = 0
    }

    private var mProvinceList: List<Province> = ArrayList()
    private var mCityList: List<City> = ArrayList()
    private val mProvinceName = ArrayList<String>()
    private val mCityName = ArrayList<String>()

    private lateinit var mLayoutParams: LayoutParams

    private lateinit var mWPProvince: WheelPicker
    private lateinit var mWPCity: WheelPicker
    private lateinit var mWPArea: WheelPicker

    init {
        initLayoutParams()
        initView(context)
        mProvinceList = getJsonDataFromAssets(context) ?: ArrayList()
        obtainProvinceData()
        addListenerToWheelPicker()
    }

    @Suppress("UNCHECKED_CAST")
    private fun getJsonDataFromAssets(context: Context): List<Province>? {
        var provinceList: List<Province>? = null
        try {
            val inputStream = context.assets.open("RegionJsonData.dat")
            val objectInputStream = ObjectInputStream(inputStream)
            provinceList = objectInputStream.readObject() as? List<Province>
            objectInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return provinceList
    }

    private fun initLayoutParams() {
        mLayoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mLayoutParams.setMargins(5, 5, 5, 5)
        mLayoutParams.width = 0
    }

    private fun initView(context: Context) {
        orientation = HORIZONTAL

        mWPProvince = WheelPicker(context)
        mWPCity = WheelPicker(context)
        mWPArea = WheelPicker(context)

        initWheelPicker(mWPProvince, 1f)
        initWheelPicker(mWPCity, 1.5f)
        initWheelPicker(mWPArea, 1.5f)
    }

    private fun initWheelPicker(wheelPicker: WheelPicker, weight: Float) {
        mLayoutParams.weight = weight
        wheelPicker.itemTextSize = dip2px(context, ITEM_TEXT_SIZE)
        wheelPicker.selectedItemTextColor = Color.parseColor(SELECTED_ITEM_COLOR)
        wheelPicker.isCurved = true
        wheelPicker.layoutParams = mLayoutParams
        addView(wheelPicker)
    }

    private fun obtainProvinceData() {
        if (mProvinceList.isEmpty()) return
        for (province in mProvinceList) {
            province.name?.let { mProvinceName.add(it) }
        }
        mWPProvince.data = mProvinceName
        setCityAndAreaData(PROVINCE_INITIAL_INDEX)
    }

    private fun addListenerToWheelPicker() {
        //监听省份的滑轮,根据省份的滑轮滑动的数据来设置市跟地区的滑轮数据
        mWPProvince.setOnItemSelectedListener(object : WheelPicker.OnItemSelectedListener {
            override fun onItemSelected(picker: WheelPicker, data: Any?, position: Int) {
                //获得该省所有城市的集合
                if (position < mProvinceList.size) {
                    mCityList = mProvinceList[position].city
                    setCityAndAreaData(position)
                }
            }
        })

        mWPCity.setOnItemSelectedListener(object : WheelPicker.OnItemSelectedListener {
            override fun onItemSelected(picker: WheelPicker, data: Any?, position: Int) {
                //获取城市对应的城区的名字
                if (position < mCityList.size) {
                    mWPArea.data = mCityList[position].area
                    mWPArea.selectedItemPosition = 0
                }
            }
        })
    }

    private fun setCityAndAreaData(position: Int) {
        if (position >= mProvinceList.size) return
        //获得该省所有城市的集合
        mCityList = mProvinceList[position].city
        //获取所有city的名字
        //重置先前的城市集合数据
        mCityName.clear()
        for (city in mCityList)
            city.name?.let { mCityName.add(it) }
        mWPCity.data = mCityName
        mWPCity.selectedItemPosition = 0
        //获取第一个城市对应的城区的名字
        //重置先前的城区集合的数据
        if (mCityList.isNotEmpty()) {
            mWPArea.data = mCityList[0].area
            mWPArea.selectedItemPosition = 0
        } else {
            mWPArea.data = emptyList<String>()
        }
    }

    override val province: String?
        get() = mProvinceList.getOrNull(mWPProvince.currentItemPosition)?.name

    override val city: String?
        get() = mCityList.getOrNull(mWPCity.currentItemPosition)?.name

    override val area: String?
        get() = mCityList.getOrNull(mWPCity.currentItemPosition)?.area?.getOrNull(mWPArea.currentItemPosition)

    override fun hideArea() {
        // this.removeViewAt(2) 
        // Index 2 might not exist or might not be Area picker if called multiple times or order changed.
        // Better safely remove mWPArea
        if (indexOfChild(mWPArea) != -1) {
            removeView(mWPArea)
        }
    }

    private fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}
