package com.aigestudio.wheelpicker.demo

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.aigestudio.wheelpicker.WheelPicker

/**
 * @author AigeStudio 2015-12-06
 * @author AigeStudio 2016-07-08
 */
class PreviewActivity : Activity(), WheelPicker.OnItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_preview)

        val wheelLeft = findViewById<WheelPicker>(R.id.main_wheel_left)
        wheelLeft.setOnItemSelectedListener(this)
        val wheelCenter = findViewById<WheelPicker>(R.id.main_wheel_center)
        wheelCenter.setOnItemSelectedListener(this)
        val wheelRight = findViewById<WheelPicker>(R.id.main_wheel_right)
        wheelRight.setOnItemSelectedListener(this)
    }

    override fun onItemSelected(picker: WheelPicker, data: Any?, position: Int) {
        var text = ""
        when (picker.id) {
            R.id.main_wheel_left -> text = "Left:"
            R.id.main_wheel_center -> text = "Center:"
            R.id.main_wheel_right -> text = "Right:"
        }
        Toast.makeText(this, text + data.toString(), Toast.LENGTH_SHORT).show()
    }
}
