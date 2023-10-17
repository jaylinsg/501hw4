package com.bignerdranch.android.geoquiz

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.bignerdranch.android.geoquiz.databinding.ActivityMainBinding
import kotlin.math.abs

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), ShakeDetector.OnShakeListener {
    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize gesture detector
        gestureDetector = GestureDetectorCompat(this, GestureListener())

        // Initialize shake detector
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Initialize shake detector with this activity as the listener
        shakeDetector = ShakeDetector(this)

        // Assuming imageView is the view you want to shake
        val imageView: ImageView = findViewById(R.id.image_view)

        // Create a shake animation
        val shakeAnimation = TranslateAnimation(-10f, 10f, 0f, 0f)
        shakeAnimation.duration = 100 // Adjust the duration as needed
        shakeAnimation.interpolator = LinearInterpolator()
        shakeAnimation.repeatMode = Animation.INFINITE
        shakeAnimation.repeatCount = Animation.INFINITE

        // Start the animation
        imageView.startAnimation(shakeAnimation)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            downEvent: MotionEvent,
            moveEvent: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val deltaX = moveEvent.x - downEvent.x
            val deltaY = moveEvent.y - downEvent.y

            // Check the direction of the fling and start the corresponding activity
            if (abs(deltaX) > abs(deltaY)) {
                if (deltaX > 0) {
                    // Right fling, open East Activity
                    startActivity(Intent(this@MainActivity, EastActivity::class.java))
                } else {
                    // Left fling, open West Activity
                    startActivity(Intent(this@MainActivity, WestActivity::class.java))
                }
            } else {
                if (deltaY > 0) {
                    // Downwards fling, open South Activity
                    startActivity(Intent(this@MainActivity, SouthActivity::class.java))
                } else {
                    // Upwards fling, open North Activity
                    startActivity(Intent(this@MainActivity, NorthActivity::class.java))
                }
            }
            return true
        }
    }

    override fun onShake() {
        // Handle the shake event (e.g., stop the shake animation)
        val imageView: ImageView = findViewById(R.id.image_view)
        imageView.clearAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(shakeDetector)
    }
}
