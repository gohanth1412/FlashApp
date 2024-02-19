package com.example.flashapp

import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.flashapp.Constants.PERMISSION_REQUEST_CODE
import com.example.flashapp.databinding.ActivityMainBinding
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity(), SmsReceiver.SmsListener, CallReceiver.CallListener,
    EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityMainBinding
    private var cameraFlash = false
    private var flashOn = false
    private var sosOn = false
    private var sosJob: Job? = null
    private lateinit var smsReceiver: SmsReceiver
    private lateinit var callReceiver: CallReceiver

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissions()

        cameraFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        smsReceiver = SmsReceiver(this)
        registerReceiver(
            smsReceiver, IntentFilter("android.provider.Telephony.SMS_RECEIVER"),
            RECEIVER_NOT_EXPORTED
        )

        callReceiver = CallReceiver(this)
        registerReceiver(callReceiver, IntentFilter("android.intent.action.PHONE_STATE"))

        onClickListener()
    }

    private fun onClickListener() {
        binding.layoutButtonOnOff.setOnClickListener {
            if (cameraFlash) {
                if (flashOn) {
                    flashOn = false
                    binding.apply {
                        btnOn.visibility = View.GONE
                        tvOn.visibility = View.GONE
                        btnOff.visibility = View.VISIBLE
                        tvOff.visibility = View.VISIBLE
                    }
                    flashLightOff()
                } else {
                    flashOn = true
                    binding.apply {
                        btnOn.visibility = View.VISIBLE
                        tvOn.visibility = View.VISIBLE
                        btnOff.visibility = View.GONE
                        tvOff.visibility = View.GONE
                    }
                    flashLightOn()
                }
            }
        }

        binding.btnSos.setOnClickListener {
            if (cameraFlash) {
                startStopSos()
            }
        }
    }

    private fun startStopSos() {
        if (sosOn) {
            sosOn = false
            if (flashOn) {
                binding.imgLight.visibility = View.VISIBLE
            } else {
                binding.imgLight.visibility = View.INVISIBLE
            }
            sosJob?.cancel()
        } else {
            sosOn = true
            sosJob = lifecycleScope.launch {
                while (sosOn) {
                    for (i in 0..2) {
                        sortMorse()
                    }
                    for (i in 0..2) {
                        longMorse()
                    }
                    delay(100L)
                }
            }
        }
    }

    private suspend fun longMorse() {
        flashLightOn()
        delay(300L)
        flashLightOff()
        delay(100L)
    }

    private suspend fun sortMorse() {
        flashLightOn()
        delay(100L)
        flashLightOff()
        delay(100L)
    }

    private fun flashLightOff() {
        val cameraManager: CameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId: String
        try {
            cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
        } catch (e: Exception) {
            Log.e("CameraException", "$e")
        }
        binding.imgLight.visibility = View.INVISIBLE
    }

    private fun flashLightOn() {
        val cameraManager: CameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId: String
        try {
            cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, true)
        } catch (e: Exception) {
            Log.e("CameraException", "$e")
        }
        binding.imgLight.visibility = View.VISIBLE
    }

    override fun onSmsReceived() {
        if (cameraFlash) {
            for (i in 0..2) {
                runBlocking {
                    sortMorse()
                    Log.d("232323", "smsActivity")
                }
            }
        }
    }

    override fun onCallReceived() {
        if (cameraFlash) {
            for (i in 0..2) {
                runBlocking {
                    sortMorse()
                }
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {}

    private fun requestPermissions() {
        EasyPermissions.requestPermissions(
            this,
            getString(R.string.these_permission_are_required_for_this_application),
            PERMISSION_REQUEST_CODE,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.RECEIVE_SMS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}