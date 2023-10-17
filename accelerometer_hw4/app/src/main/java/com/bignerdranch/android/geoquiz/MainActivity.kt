package com.bignerdranch.android.geoquiz

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var xTextView: TextView
    private lateinit var yTextView: TextView
    private lateinit var zTextView: TextView
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var sensitivitySeekBar: SeekBar
    private var sensitivity: Float = 10.0f  // Adjusted sensitivity value
    private lateinit var movementStatusTextView: TextView
    // Add a variable to track if a significant movement toast has been shown
    private var isSignificantMovementToastShown = false

    companion object {
        private const val MAX_SENSITIVITY = 2.0f
        private const val MIN_SENSITIVITY = 0.1f
        private const val DEFAULT_SENSITIVITY = 1.0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the TextViews for displaying accelerometer values
        xTextView = findViewById(R.id.x_axis_text_view)
        yTextView = findViewById(R.id.y_axis_text_view)
        zTextView = findViewById(R.id.z_axis_text_view)

        // Initialize the sensor manager and accelerometer
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Initialize the SeekBar for sensitivity
        sensitivitySeekBar = findViewById(R.id.seekBarSensitivity)

        // Initialize the initial sensitivity value based on SeekBar progress
        sensitivitySeekBar.progress = ((sensitivity - MIN_SENSITIVITY) / (MAX_SENSITIVITY - MIN_SENSITIVITY) * sensitivitySeekBar.max).toInt()
        sensitivity = MIN_SENSITIVITY + (sensitivitySeekBar.progress.toFloat() / sensitivitySeekBar.max) * (MAX_SENSITIVITY - MIN_SENSITIVITY)

        val sensitivityLabel = getString(R.string.sensitivity_label, sensitivity)
        findViewById<TextView>(R.id.sensitivity_label).text = sensitivityLabel

        // Initialize the TextView for movement status
        movementStatusTextView = findViewById(R.id.movement_status_text_view)

        // Check if the accelerometer is available
        if (accelerometer == null) {
            Toast.makeText(this, "Accelerometer not available on this device", Toast.LENGTH_LONG).show()
            finish()  // Close the app if accelerometer is not available
            return
        }

        // Register accelerometer listener
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Set up SeekBar listener to adjust sensitivity
        sensitivitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBar?.let {
                    // Directly map SeekBar progress to sensitivity (MIN_SENSITIVITY to MAX_SENSITIVITY)
                    sensitivity = MIN_SENSITIVITY + (progress.toFloat() / seekBar.max) * (MAX_SENSITIVITY - MIN_SENSITIVITY)

                    // Update the sensitivity label to display the new sensitivity value
                    findViewById<TextView>(R.id.sensitivity_label).text = getString(R.string.sensitivity_label, sensitivity)

                    // Display the current sensitivity in a Toast
                    Toast.makeText(applicationContext, getString(R.string.current_sensitivity, sensitivity), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Implement actions when the user starts dragging the SeekBar
                Toast.makeText(applicationContext, "SeekBar drag started", Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    // Update the sensitivity based on the SeekBar progress
                    sensitivity = MIN_SENSITIVITY + (seekBar.progress.toFloat() / seekBar.max) * (MAX_SENSITIVITY - MIN_SENSITIVITY)

                    // Display a Toast to indicate sensitivity change
                    Toast.makeText(applicationContext, getString(R.string.sensitivity_updated, sensitivity), Toast.LENGTH_SHORT).show()
                }
            }
        })

        // Set the initial progress of the SeekBar based on the initial sensitivity
        val initialSeekBarProgress = ((sensitivity - MIN_SENSITIVITY) / (MAX_SENSITIVITY - MIN_SENSITIVITY) * sensitivitySeekBar.max).toInt()
        sensitivitySeekBar.progress = sensitivitySeekBar.max - initialSeekBarProgress
        sensitivity = MIN_SENSITIVITY + (initialSeekBarProgress.toFloat() / sensitivitySeekBar.max) * (MAX_SENSITIVITY - MIN_SENSITIVITY)
    }

    override fun onResume() {
        super.onResume()
        // Register accelerometer listener
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Reset the flag when resuming the activity
        isSignificantMovementToastShown = false
    }

    override fun onPause() {
        super.onPause()
        // Unregister the accelerometer listener to save battery
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Convert raw accelerometer values to m/s^2
            val x = event.values[0] / SensorManager.GRAVITY_EARTH
            val y = event.values[1] / SensorManager.GRAVITY_EARTH
            val z = event.values[2] / SensorManager.GRAVITY_EARTH

            // Update the displayed accelerometer values
            updateAccelerometerValues(x, y, z)

            // Pass sensitivity as a float to the isSignificantMovement function
            GlobalScope.launch {
                handleAccelerometerData(x, y, z, sensitivity)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAccelerometerValues(x: Float, y: Float, z: Float) {
        // Convert raw accelerometer values to g (acceleration due to gravity) for each axis
        val xInG = x * SensorManager.GRAVITY_EARTH
        val yInG = y * SensorManager.GRAVITY_EARTH
        val zInG = z * SensorManager.GRAVITY_EARTH

        // Update the displayed accelerometer values for each axis using string resources
        xTextView.text = getString(R.string.x_axis, xInG)
        yTextView.text = getString(R.string.y_axis, yInG)
        zTextView.text = getString(R.string.z_axis, zInG)

        // Adjust layout dynamically based on SeekBar position
        val seekBar = findViewById<SeekBar>(R.id.seekBarSensitivity)
        val sensitivityLabel = findViewById<TextView>(R.id.sensitivity_label)
        val params = sensitivityLabel.layoutParams as RelativeLayout.LayoutParams
        params.addRule(RelativeLayout.BELOW, seekBar.id)
        sensitivityLabel.layoutParams = params
    }

    private fun handleAccelerometerData(x: Float, y: Float, z: Float, sensitivity: Float) {
        // Launch a coroutine and switch to the main thread (Dispatchers.Main)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                // Check significant movement for each axis
                val significantMovement = isSignificantMovement(x, y, z)

                // Log significant movement
                if (significantMovement) {
                    Log.d(TAG, "Significant movement detected: x=$x, y=$y, z=$z")
                    showToast("Significant movement detected")
                }

                // Update UI to show movement status based on significant movement
                movementStatusTextView.text = getString(
                    R.string.movement_status,
                    if (significantMovement) getString(R.string.significant_movement)
                    else getString(R.string.no_significant_movement)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleAccelerometerData: ${e.message}")
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        if (!isSignificantMovementToastShown) {
            isSignificantMovementToastShown = true
        }
    }

    private fun isSignificantMovement(x: Float, y: Float, z: Float): Boolean {
        // Determine if the movement is significant in any axis based on sensitivity
        return x > sensitivity || y > sensitivity || z > sensitivity
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }
}
