package com.aigestudio.wheelpicker.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.aigestudio.wheelpicker.WheelPicker

/**
 * A Jetpack Compose wrapper for [WheelPicker].
 *
 * @param data The data source for the picker.
 * @param modifier The modifier to be applied to the layout.
 * @param selectedItemPosition The initial selected item position.
 * @param onItemSelected Callback when an item is selected.
 * @param factory An optional factory to further configure the [WheelPicker] instance.
 */
@Composable
fun WheelPickerComposable(
    data: List<*>,
    modifier: Modifier = Modifier,
    selectedItemPosition: Int = 0,
    onItemSelected: ((picker: WheelPicker, data: Any?, position: Int) -> Unit)? = null,
    factory: (WheelPicker.() -> Unit)? = null
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WheelPicker(context).apply {
                this.data = data
                this.selectedItemPosition = selectedItemPosition
                factory?.invoke(this)
                setOnItemSelectedListener(object : WheelPicker.OnItemSelectedListener {
                    override fun onItemSelected(picker: WheelPicker, data: Any?, position: Int) {
                        onItemSelected?.invoke(picker, data, position)
                    }
                })
            }
        },
        update = { view ->
            // Update data and selection if they change
            if (view.data != data) {
                view.data = data
            }
            if (view.selectedItemPosition != selectedItemPosition) {
                view.selectedItemPosition = selectedItemPosition
            }
        }
    )
}