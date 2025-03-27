package com.aigestudio.wheelpicker.model

import java.io.Serializable

/**
 * Created by Administrator on 2016/9/14 0014.
 */
class Province : Serializable {
    var name: String? = null
    var city: List<City> = ArrayList()
}
