package com.example.glm400clquad.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

class QuadBleManager(private val context: Context) {
    companion object {
        val MEASUREMENT_SERVICE_UUID: UUID = UUID.fromString("02a6c0d0-0451-4000-b000-fb3210111989")
        val MEASUREMENT_CHAR_UUID: UUID = UUID.fromString("02a6c0d1-0451-4000-b000-fb3210111989")
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner get() = adapter?.bluetoothLeScanner
    private val gattBySlot = mutableMapOf<Int, BluetoothGatt>()
    private val callbacksBySlot = mutableMapOf<Int, BluetoothGattCallback>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private val _devices = MutableStateFlow<List<ScannedBleDevice>>(emptyList())
    val devices: StateFlow<List<ScannedBleDevice>> = _devices

    private val _panels = MutableStateFlow((1..4).map { LaserPanelState(slot = it) })
    val panels: StateFlow<List<LaserPanelState>> = _panels

    private val _log = MutableStateFlow("Ready\n")
    val log: StateFlow<String> = _log

    private var scanCallback: ScanCallback? = null

    private fun appendLog(message: String) {
        val now = dateFormat.format(Date())
        _log.value = (_log.value + "[$now] $message\n").takeLast(12000)
    }

    fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasPermissions()) {
            appendLog("Bluetooth permission missing")
            return
        }
        if (adapter?.isEnabled != true) {
            appendLog("Bluetooth is disabled")
            return
        }
        _devices.value = emptyList()
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device ?: return
                val name = result.scanRecord?.deviceName ?: device.name ?: "N/A"
                if (!isGlm400BleDevice(name, MEASUREMENT_SERVICE_UUID, result)) return
                val item = ScannedBleDevice(device, name, device.address, result.rssi)
                _devices.update { old ->
                    (old.filterNot { it.address == item.address } + item)
                        .sortedByDescending { it.rssi }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                appendLog("Scan failed: $errorCode")
            }
        }
        scanner?.startScan(scanCallback)
        appendLog("Scan started (GLM400 devices only)")
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanCallback?.let { scanner?.stopScan(it) }
        scanCallback = null
        appendLog("Scan stopped")
    }

    @SuppressLint("MissingPermission")
    fun connect(slot: Int, scanned: ScannedBleDevice) {
        if (!hasPermissions()) return
        disconnect(slot)
        updatePanel(slot) { it.copy(address = scanned.address, name = scanned.name, connecting = true, connected = false, distanceText = "Connecting...") }
        appendLog("Slot $slot connecting to ${scanned.name} ${scanned.address}")

        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    appendLog("Slot $slot GATT error: $status")
                    updatePanel(slot) { it.copy(connecting = false, connected = false, distanceText = "GATT error $status") }
                    return
                }
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    appendLog("Slot $slot connected, discovering services")
                    updatePanel(slot) { it.copy(connecting = false, connected = true, distanceText = "Discovering services...") }
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    appendLog("Slot $slot disconnected")
                    updatePanel(slot) { it.copy(connecting = false, connected = false, distanceText = "Disconnected") }
                    gattBySlot.remove(slot)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    appendLog("Slot $slot service discovery failed: $status")
                    return
                }
                val ch = gatt.getService(MEASUREMENT_SERVICE_UUID)?.getCharacteristic(MEASUREMENT_CHAR_UUID)
                if (ch == null) {
                    appendLog("Slot $slot measurement characteristic not found")
                    updatePanel(slot) { it.copy(distanceText = "Measurement char not found") }
                    return
                }
                enableIndications(gatt, ch, slot)
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                onPacket(slot, characteristic.value)
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
                onPacket(slot, value)
            }
        }
        callbacksBySlot[slot] = callback
        val gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scanned.device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
        } else {
            scanned.device.connectGatt(context, false, callback)
        }
        gattBySlot[slot] = gatt
    }

    @SuppressLint("MissingPermission")
    private fun enableIndications(gatt: BluetoothGatt, ch: BluetoothGattCharacteristic, slot: Int) {
        gatt.setCharacteristicNotification(ch, true)
        val descriptor = ch.getDescriptor(CCCD_UUID)
        if (descriptor == null) {
            appendLog("Slot $slot CCCD descriptor not found")
            return
        }
        if (Build.VERSION.SDK_INT >= 33) {
            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(descriptor)
        }
        appendLog("Slot $slot indications enabled")
        updatePanel(slot) { it.copy(distanceText = "Waiting for stream...") }
    }

    private fun onPacket(slot: Int, bytes: ByteArray) {
        val hex = bytes.toHexString()
        appendLog("Slot $slot <= $hex")
        updatePanel(slot) {
            it.copy(
                rawHex = hex,
                distanceText = DistanceParser.parse(bytes),
                lastUpdate = dateFormat.format(Date())
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect(slot: Int) {
        gattBySlot.remove(slot)?.let {
            it.disconnect()
            it.close()
        }
        callbacksBySlot.remove(slot)
        updatePanel(slot) { LaserPanelState(slot = slot) }
    }

    @SuppressLint("MissingPermission")
    fun disconnectAll() {
        (1..4).forEach { disconnect(it) }
    }

    private fun updatePanel(slot: Int, transform: (LaserPanelState) -> LaserPanelState) {
        _panels.update { list -> list.map { if (it.slot == slot) transform(it) else it } }
    }
}
