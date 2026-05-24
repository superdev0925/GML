package com.example.glm400clquad.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import java.util.UUID

/** Matches GLM400 / GLM400CL device names (also tolerates common "GML400" typo). */
private val GLM400_NAME_REGEX = Regex("""(?i)(glm|gml)\s*400""")

fun isGlm400BleDevice(name: String, measurementServiceUuid: UUID, scanResult: ScanResult): Boolean {
    if (GLM400_NAME_REGEX.containsMatchIn(name)) return true
    val record = scanResult.scanRecord ?: return false
    return record.serviceUuids?.any { it.uuid == measurementServiceUuid } == true
}

data class ScannedBleDevice(
    val device: BluetoothDevice,
    val name: String,
    val address: String,
    val rssi: Int
)

data class LaserPanelState(
    val slot: Int,
    val address: String? = null,
    val name: String = "Not assigned",
    val connected: Boolean = false,
    val connecting: Boolean = false,
    val rawHex: String = "--",
    val distanceText: String = "Waiting...",
    val lastUpdate: String = "--"
)

fun ByteArray.toHexString(): String = joinToString("-") { "%02X".format(it) }
