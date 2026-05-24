package com.example.glm400clquad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.glm400clquad.ble.QuadBleManager

class MainViewModel(app: Application) : AndroidViewModel(app) {
    val manager = QuadBleManager(app.applicationContext)
    val devices = manager.devices
    val panels = manager.panels
    val log = manager.log

    fun startScan() = manager.startScan()
    fun stopScan() = manager.stopScan()
}
