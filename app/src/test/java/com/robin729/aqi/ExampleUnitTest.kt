package com.robin729.aqi

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.mapboxsdk.geometry.LatLng
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun just_doit() {
        val type = object : TypeToken<HashSet<Int>>(){}.type
        val z = hashSetOf(1, 2, 3, 4)
        val x = Gson().toJson(z)
        val y: HashSet<Int> = Gson().fromJson(x, type)
        assertEquals(y, z)
    }
}
