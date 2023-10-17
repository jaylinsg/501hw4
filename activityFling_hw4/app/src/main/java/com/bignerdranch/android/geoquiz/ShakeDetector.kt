package com.bignerdranch.android.geoquiz

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(private val listener: OnShakeListener) : SensorEventListener {

    private val shakeThresholdGravity = 2.7F
    private val shakeSlopTimeInMs = 500
    private var lastShakeTimestamp: Long = 0

    interface OnShakeListener {
        fun onShake()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val x = event.values[0] / SensorManager.GRAVITY_EARTH
        val y = event.values[1] / SensorManager.GRAVITY_EARTH
        val z = event.values[2] / SensorManager.GRAVITY_EARTH

        val gForce = sqrt(x * x + y * y + z * z)
        if (gForce >= shakeThresholdGravity) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastShakeTimestamp < shakeSlopTimeInMs) {
                return
            }

            lastShakeTimestamp = currentTime
            listener.onShake()
        }
    }
}