package com.example.flashlight

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraCharacteristics
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var flashlightToggle: Switch
    private lateinit var flashlightAction: EditText
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        flashlightToggle = findViewById(R.id.flashlight_toggle)
        flashlightAction = findViewById(R.id.flashlight_action)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        if (!hasFlashlightFeature()) {
            // Handle the case where the device doesn't have a flashlight.
            showToast("Flashlight not available on this device.")
            flashlightToggle.isEnabled = false
        } else {
            try {
                val cameraIdList = cameraManager.cameraIdList
                if (cameraIdList.isNotEmpty()) {
                    cameraId = cameraIdList[0]
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        flashlightToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                turnOnFlashlight()
            } else {
                turnOffFlashlight()
            }
        }

        flashlightAction.setOnEditorActionListener { _, _, _ ->
            val input = flashlightAction.text.toString().trim()
            if (input.equals("on", ignoreCase = true)) {
                flashlightToggle.isChecked = true
            } else if (input.equals("off", ignoreCase = true)) {
                flashlightToggle.isChecked = false
            }
            true
        }
    }

    private fun hasFlashlightFeature(): Boolean {
        val hasFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        return hasFlash
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun turnOnFlashlight() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val id = cameraId // Store the cameraId in a local variable
            if (id != null) {
                if (isFlashAvailable(id)) {
                    try {
                        cameraManager.setTorchMode(id, true)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                        showToast("Failed to turn on flashlight.")
                        flashlightToggle.isChecked = false
                    }
                } else {
                    showToast("No flashlight available on this device.")
                    flashlightToggle.isChecked = false
                }
            } else {
                showToast("Camera ID is not set. Cannot turn on flashlight.")
                flashlightToggle.isChecked = false
            }
        } else {
            // Handle the case where the app doesn't have camera permission.
            showToast("Camera permission is required to use the flashlight.")
            flashlightToggle.isChecked = false
        }
    }


    private fun isFlashAvailable(cameraId: String): Boolean {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
        return flashAvailable == true
    }


    private fun turnOffFlashlight() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val id = cameraId
            try {
                if (id != null) {
                    cameraManager.setTorchMode(id, false)
                } else {
                    showToast("No flashlight available on this device.")
                    flashlightToggle.isChecked = false
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
                showToast("Failed to turn off flashlight.")
                flashlightToggle.isChecked = true
            }
        } else {
            // Handle the case where the app doesn't have camera permission.
            showToast("Camera permission is required to use the flashlight.")
            flashlightToggle.isChecked = true
        }
    }
}
