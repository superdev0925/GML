package com.example.glm400clquad

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glm400clquad.ble.LaserPanelState
import com.example.glm400clquad.ble.ScannedBleDevice
import com.example.glm400clquad.ui.AppColors
import com.example.glm400clquad.ui.QuadLaserTheme
import com.example.glm400clquad.ui.UiScale

private val DemoBleLog = """
13:35:12.123  Scan started...
13:35:12.456  [ADV] 6C:5C:B1:45:BB:51  RSSI=-18 dBm
13:35:12.789  [ADV] 7A:23:11:9C:3E:10  RSSI=-42 dBm
13:35:13.012  [CONN] Connected to 6C:5C:B1:45:BB:51
13:35:13.215  Ready
""".trim()

private data class DefaultNearbyDevice(
    val name: String = "SiLabs-Light",
    val address: String = "6C:5C:B1:45:BB:51",
    val rssi: Int = -18,
    val latencyMs: Int = 23,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(vm: MainViewModel) {
    val activity = LocalContext.current as MainActivity
    val devices by vm.devices.collectAsState(initial = emptyList())
    val panels by vm.panels.collectAsState(
        initial = (1..4).map { com.example.glm400clquad.ble.LaserPanelState(it) }
    )
    val log by vm.log.collectAsState(initial = "")
    var selectedSlot by remember { mutableIntStateOf(1) }
    var isScanning by remember { mutableStateOf(false) }
    var suppressedLog by remember { mutableStateOf<String?>(null) }

    val displayLog = when {
        suppressedLog != null && log == suppressedLog -> ""
        else -> {
            if (suppressedLog != null && log != suppressedLog) suppressedLog = null
            log
        }
    }

    QuadLaserTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
        ) {
            AppHeader(
                onScan = {
                    if (vm.manager.hasPermissions()) {
                        isScanning = true
                        vm.startScan()
                    } else {
                        activity.requestBlePermissions()
                    }
                },
                onStop = {
                    isScanning = false
                    vm.stopScan()
                }
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = UiScale.PadScreenH, vertical = UiScale.PadScreenV),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left column: Live Measurements (top) + Raw BLE Log (bottom, fills remaining space)
                Column(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                ) {
                    SectionHeader("Live Measurements", Icons.Default.MonitorHeart)
                    Spacer(Modifier.height(UiScale.GapSection))
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        val gridHeight = (maxHeight * 0.40f).coerceIn(130.dp, UiScale.GridMax)
                        Column(Modifier.fillMaxSize()) {
                            LiveMeasurementsGrid(
                                panels = panels,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(gridHeight)
                            )
                            Spacer(Modifier.height(UiScale.GapSection))
                            BleLogSection(
                                log = formatLogForDisplay(displayLog),
                                onClear = { suppressedLog = log },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .heightIn(min = UiScale.LogMin)
                            )
                        }
                    }
                }

                // Right column ~35%
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SidebarPanel {
                        SectionHeader("Assign Device", Icons.Default.Person)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Slot:",
                                fontWeight = FontWeight.Medium,
                                fontSize = UiScale.Body,
                                color = AppColors.TextPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            (1..4).forEach { slot ->
                                SlotChip(
                                    slot = slot,
                                    selected = selectedSlot == slot,
                                    onClick = { selectedSlot = slot }
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Showing GLM400 / GLM400CL only",
                            fontSize = UiScale.Caption,
                            color = AppColors.TextSecondary
                        )
                    }

                    NearbyDevicesSection(
                        devices = devices,
                        isScanning = isScanning,
                        onConnect = { device ->
                            isScanning = false
                            vm.stopScan()
                            vm.manager.connect(selectedSlot, device)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppHeader(onScan: () -> Unit, onStop: () -> Unit) {
    Surface(color = AppColors.Surface, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(UiScale.Logo)
                    .clip(CircleShape)
                    .background(AppColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, null, tint = AppColors.Primary, modifier = Modifier.size(UiScale.IconHeader))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "GLM400CL Quad Laser Monitor",
                fontWeight = FontWeight.Bold,
                fontSize = UiScale.AppTitle,
                color = AppColors.TitlePurple,
                modifier = Modifier.weight(1f)
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(AppColors.Primary, AppColors.PrimaryDark)))
                    .clickable(onClick = onScan)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(UiScale.IconSmall))
                Spacer(Modifier.width(5.dp))
                Text("Scan", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = UiScale.Button)
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onStop,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, AppColors.Primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AppColors.Surface,
                    contentColor = AppColors.Primary
                ),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Icon(Icons.Default.Stop, null, modifier = Modifier.size(UiScale.IconSmall))
                Spacer(Modifier.width(5.dp))
                Text("Stop", fontWeight = FontWeight.SemiBold, fontSize = UiScale.Button)
            }
        }
    }
}

@Composable
private fun LiveMeasurementsGrid(
    panels: List<LaserPanelState>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val rowHeight = (maxHeight - UiScale.GapGrid) / 2
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                horizontalArrangement = Arrangement.spacedBy(UiScale.GapGrid)
            ) {
                LaserCard(panels[0], Modifier.weight(1f).fillMaxHeight())
                LaserCard(panels[1], Modifier.weight(1f).fillMaxHeight())
            }
            Spacer(Modifier.height(UiScale.GapGrid))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                horizontalArrangement = Arrangement.spacedBy(UiScale.GapGrid)
            ) {
                LaserCard(panels[2], Modifier.weight(1f).fillMaxHeight())
                LaserCard(panels[3], Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun SidebarPanel(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AppColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AppColors.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(UiScale.PadPanel)) { content() }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.Primary, modifier = Modifier.size(UiScale.IconSection))
        Spacer(Modifier.width(6.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = UiScale.SectionTitle, color = AppColors.TextPrimary)
    }
}

@Composable
private fun SlotChip(slot: Int, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .size(if (selected) UiScale.Slot + 4.dp else UiScale.Slot)
            .then(
                if (selected) {
                    Modifier.border(2.dp, AppColors.Primary.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = if (selected) AppColors.Primary else AppColors.Surface,
            modifier = Modifier
                .size(UiScale.Slot)
                .border(1.dp, if (selected) AppColors.Primary else AppColors.Border, shape)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "$slot",
                    fontWeight = FontWeight.Bold,
                    fontSize = UiScale.Body,
                    color = if (selected) Color.White else AppColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun LaserCard(panel: LaserPanelState, modifier: Modifier = Modifier) {
    val (statusLabel, statusColor) = when {
        panel.connected -> "CONNECTED" to AppColors.StatusGreen
        panel.connecting -> "CONNECTING" to AppColors.StatusOrange
        else -> "DISCONNECTED" to AppColors.StatusRed
    }
    val assignmentLabel = if (panel.address != null) panel.name else "Not assigned"

    Surface(
        modifier = modifier.heightIn(min = 72.dp),
        shape = RoundedCornerShape(12.dp),
        color = AppColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AppColors.Border)
    ) {
        Box(Modifier.fillMaxSize()) {
            Icon(
                Icons.Default.MyLocation,
                contentDescription = null,
                tint = AppColors.Primary.copy(alpha = 0.28f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(UiScale.IconSection)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AppColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${panel.slot}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = UiScale.Body
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Device ${panel.slot}",
                            fontWeight = FontWeight.Bold,
                            fontSize = UiScale.CardTitle,
                            color = AppColors.TextPrimary
                        )
                        Text(assignmentLabel, fontSize = UiScale.BodySmall, color = AppColors.TextSecondary)
                    }
                }

                if (panel.connected || panel.connecting) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        panel.distanceText,
                        fontWeight = FontWeight.Bold,
                        fontSize = UiScale.Body,
                        color = AppColors.TextPrimary
                    )
                    if (panel.rawHex != "--") {
                        Text(
                            "Raw: ${panel.rawHex}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = UiScale.Caption,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        statusLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = UiScale.Status,
                        color = statusColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BleLogSection(
    log: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = AppColors.Surface,
        border = BorderStroke(1.dp, AppColors.Border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(UiScale.PadCard)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, null, tint = AppColors.Primary, modifier = Modifier.size(UiScale.IconSection))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Raw BLE Log",
                    fontWeight = FontWeight.Bold,
                    fontSize = UiScale.SectionTitle,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, "Clear", tint = AppColors.Primary, modifier = Modifier.size(UiScale.IconSmall))
                    Spacer(Modifier.width(3.dp))
                    Text("Clear", color = AppColors.Primary, fontWeight = FontWeight.Medium, fontSize = UiScale.BodySmall)
                }
            }
            Spacer(Modifier.height(6.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(8.dp),
                color = AppColors.Surface,
                border = BorderStroke(1.dp, AppColors.Border.copy(alpha = 0.85f))
            ) {
                val scroll = rememberScrollState()
                LaunchedEffect(log) { scroll.animateScrollTo(scroll.maxValue) }
                Text(
                    text = colorizeLog(log),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = UiScale.Log,
                    lineHeight = UiScale.LogLine
                )
            }
        }
    }
}

@Composable
private fun NearbyDevicesSection(
    devices: List<ScannedBleDevice>,
    isScanning: Boolean,
    onConnect: (ScannedBleDevice) -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        NearbyDevicesHeader(isScanning = isScanning)
        Spacer(Modifier.height(6.dp))
        if (devices.isEmpty()) {
            DefaultNearbyDeviceCard()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                devices.forEach { device ->
                    DeviceCard(device) { onConnect(device) }
                }
            }
        }
    }
}

@Composable
private fun DefaultNearbyDeviceCard() {
    val demo = DefaultNearbyDevice()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AppColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AppColors.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(UiScale.PadCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(UiScale.BtCircle)
                        .clip(CircleShape)
                        .background(AppColors.ConnectBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Bluetooth, null, tint = Color.White, modifier = Modifier.size(UiScale.IconSmall))
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(demo.name, fontWeight = FontWeight.Bold, fontSize = UiScale.CardTitle, color = AppColors.TextPrimary)
                    Text(
                        demo.address,
                        fontFamily = FontFamily.Monospace,
                        fontSize = UiScale.Caption,
                        color = AppColors.TextSecondary
                    )
                }
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.ConnectBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Text("CONNECT", fontWeight = FontWeight.Bold, fontSize = UiScale.Caption)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeviceStat(Icons.Default.SignalCellularAlt, "${demo.rssi} dBm")
                StatDivider()
                DeviceStat(Icons.Default.AccessTime, "${demo.latencyMs} ms")
                StatDivider()
                DeviceStat(Icons.Default.MyLocation, "Unspecified")
                StatDivider()
                DeviceStat(Icons.Default.Bluetooth, "Connectible")
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FavoriteBorder, null, tint = AppColors.TextSecondary, modifier = Modifier.size(UiScale.IconSmall))
                Spacer(Modifier.width(4.dp))
                Text("Not bonded", fontSize = UiScale.Caption, color = AppColors.TextSecondary)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowDown, null, tint = AppColors.TextSecondary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun NearbyDevicesHeader(isScanning: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Bluetooth, null, tint = AppColors.Primary, modifier = Modifier.size(UiScale.IconSection))
        Spacer(Modifier.width(6.dp))
        Text(
            "Nearby Bluetooth Devices",
            fontWeight = FontWeight.Bold,
            fontSize = UiScale.SectionTitle,
            color = AppColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (isScanning) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = AppColors.Primary)
        }
    }
}

@Composable
private fun DeviceCard(device: ScannedBleDevice, onConnect: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AppColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AppColors.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(UiScale.PadCard)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(UiScale.BtCircle)
                        .clip(CircleShape)
                        .background(AppColors.ConnectBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Bluetooth, null, tint = Color.White, modifier = Modifier.size(UiScale.IconSmall))
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(device.name, fontWeight = FontWeight.Bold, fontSize = UiScale.CardTitle, color = AppColors.TextPrimary)
                    Text(
                        device.address,
                        fontFamily = FontFamily.Monospace,
                        fontSize = UiScale.Caption,
                        color = AppColors.TextSecondary
                    )
                }
                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.ConnectBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Text("CONNECT", fontWeight = FontWeight.Bold, fontSize = UiScale.Caption)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeviceStat(Icons.Default.SignalCellularAlt, "${device.rssi} dBm")
                StatDivider()
                DeviceStat(Icons.Default.AccessTime, "23 ms")
                StatDivider()
                DeviceStat(Icons.Default.MyLocation, "Unspecified")
                StatDivider()
                DeviceStat(Icons.Default.Bluetooth, "Connectible")
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FavoriteBorder, null, tint = AppColors.TextSecondary, modifier = Modifier.size(UiScale.IconSmall))
                Spacer(Modifier.width(4.dp))
                Text("Not bonded", fontSize = UiScale.Caption, color = AppColors.TextSecondary)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowDown, null, tint = AppColors.TextSecondary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun DeviceStat(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.TextSecondary, modifier = Modifier.size(UiScale.IconSmall))
        Spacer(Modifier.width(3.dp))
        Text(
            label,
            fontSize = UiScale.Micro,
            color = AppColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatDivider() {
    Box(Modifier.height(16.dp).width(1.dp).background(AppColors.Border))
}

private fun formatLogForDisplay(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return "Ready"
    val lines = raw.lines().filter { it.isNotBlank() }
    if (lines.isEmpty() || (lines.size == 1 && lines[0].trim().equals("Ready", ignoreCase = true))) {
        return DemoBleLog
    }
    return lines.joinToString("\n") { transformLogLine(it) }
}

private fun transformLogLine(line: String): String {
    val bracketed = Regex("""^\[(\d{2}:\d{2}:\d{2}\.\d{3})]\s*(.*)$""").find(line.trim())
    if (bracketed != null) {
        val time = bracketed.groupValues[1]
        val message = bracketed.groupValues[2]
        val body = when {
            message.contains("Scan started", ignoreCase = true) -> "Scan started..."
            message.contains("Slot", ignoreCase = true) && message.contains("<=", ignoreCase = true) ->
                message.substringAfter("<=", message).trim().let { hex -> "[DATA] $hex" }
            message.contains("connected", ignoreCase = true) ->
                message.substringAfter("to ", message).trim().let { addr -> "[CONN] Connected to $addr" }
            else -> message
        }
        return "$time  $body"
    }
    return line.trim()
}

private fun colorizeLog(text: String) = buildAnnotatedString {
    text.lines().forEachIndexed { index, line ->
        if (index > 0) append('\n')
        if (line.isBlank()) return@forEachIndexed

        val scanIdx = line.indexOf("Scan started", ignoreCase = true)
        if (scanIdx >= 0) {
            withStyle(SpanStyle(color = AppColors.TextPrimary)) { append(line.substring(0, scanIdx)) }
            withStyle(SpanStyle(color = AppColors.LogGreen)) { append(line.substring(scanIdx)) }
            return@forEachIndexed
        }

        withStyle(SpanStyle(color = AppColors.TextPrimary)) { append(line) }
    }
}
