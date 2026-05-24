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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glm400clquad.ble.LaserPanelState
import com.example.glm400clquad.ble.ScannedBleDevice
import com.example.glm400clquad.ui.AppColors
import com.example.glm400clquad.ui.QuadLaserTheme

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
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left column ~65%
                Column(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                ) {
                    SectionHeader("Live Measurements", Icons.Default.MonitorHeart)
                    Spacer(Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 220.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            LaserCard(panels[0], Modifier.weight(1f).fillMaxHeight())
                            LaserCard(panels[1], Modifier.weight(1f).fillMaxHeight())
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            LaserCard(panels[2], Modifier.weight(1f).fillMaxHeight())
                            LaserCard(panels[3], Modifier.weight(1f).fillMaxHeight())
                        }
                    }

                    Spacer(Modifier.height(22.dp))
                    BleLogSection(
                        log = displayLog,
                        onClear = { suppressedLog = log }
                    )
                }

                // Right column ~35%
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    SidebarPanel {
                        SectionHeader("Assign Device", Icons.Default.Person)
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Slot:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = AppColors.TextPrimary
                            )
                            Spacer(Modifier.width(12.dp))
                            (1..4).forEach { slot ->
                                SlotChip(
                                    slot = slot,
                                    selected = selectedSlot == slot,
                                    onClick = { selectedSlot = slot }
                                )
                                Spacer(Modifier.width(10.dp))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Showing GLM400 / GLM400CL only",
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                    }

                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        NearbyDevicesHeader(isScanning = isScanning)
                        Spacer(Modifier.height(12.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (devices.isEmpty()) {
                                item {
                                    Text(
                                        "No GLM400 found yet. Tap Scan and move closer to the laser.",
                                        fontSize = 13.sp,
                                        color = AppColors.TextSecondary,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            } else {
                                items(devices, key = { it.address }) { device ->
                                    DeviceCard(
                                        device = device,
                                        onConnect = {
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
            }
        }
    }
}

@Composable
private fun AppHeader(onScan: () -> Unit, onStop: () -> Unit) {
    Surface(color = AppColors.Surface, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, null, tint = AppColors.Primary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(
                "GLM400CL Quad Laser Monitor",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = AppColors.TitlePurple,
                modifier = Modifier.weight(1f)
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.horizontalGradient(listOf(AppColors.Primary, AppColors.PrimaryDark)))
                    .clickable(onClick = onScan)
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Scan", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                onClick = onStop,
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.5.dp, AppColors.Primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AppColors.Surface,
                    contentColor = AppColors.Primary
                )
            ) {
                Icon(Icons.Default.Stop, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Stop", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun SidebarPanel(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AppColors.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) { content() }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.Primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = AppColors.TextPrimary)
    }
}

@Composable
private fun SlotChip(slot: Int, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .size(if (selected) 50.dp else 46.dp)
            .then(
                if (selected) {
                    Modifier.border(2.dp, AppColors.Primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = if (selected) AppColors.Primary else AppColors.Surface,
            modifier = Modifier
                .size(44.dp)
                .border(1.dp, if (selected) AppColors.Primary else AppColors.Border, shape)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "$slot",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
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
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = AppColors.CardMuted,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, AppColors.Border.copy(alpha = 0.6f))
    ) {
        Box(Modifier.fillMaxSize()) {
            Icon(
                Icons.Default.MyLocation,
                contentDescription = null,
                tint = AppColors.Primary.copy(alpha = 0.28f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(24.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AppColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${panel.slot}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Device ${panel.slot}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppColors.TextPrimary
                        )
                        Text(assignmentLabel, fontSize = 14.sp, color = AppColors.TextSecondary)
                    }
                }

                if (panel.connected || panel.connecting) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        panel.distanceText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = AppColors.TextPrimary
                    )
                    if (panel.rawHex != "--") {
                        Text(
                            "Raw: ${panel.rawHex}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        statusLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = statusColor,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BleLogSection(log: String, onClear: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Description, null, tint = AppColors.Primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            "Raw BLE Log",
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            color = AppColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onClear) {
            Icon(Icons.Default.Delete, "Clear", tint = AppColors.Primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Clear", color = AppColors.Primary, fontWeight = FontWeight.Medium)
        }
    }
    Spacer(Modifier.height(10.dp))
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp),
        shape = RoundedCornerShape(14.dp),
        color = AppColors.Surface,
        border = BorderStroke(1.dp, AppColors.Border)
    ) {
        val scroll = rememberScrollState()
        LaunchedEffect(log) { scroll.animateScrollTo(scroll.maxValue) }
        Text(
            text = colorizeLog(log.ifBlank { "Ready\n" }),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(14.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun NearbyDevicesHeader(isScanning: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Bluetooth, null, tint = AppColors.Primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            "Nearby Bluetooth Devices",
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            color = AppColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (isScanning) {
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = AppColors.Primary)
        }
    }
}

@Composable
private fun DeviceCard(device: ScannedBleDevice, onConnect: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.Surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, AppColors.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppColors.ConnectBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Bluetooth, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(device.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
                    Text(
                        device.address,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                }
                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.ConnectBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("CONNECT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeviceStat(Icons.Default.SignalCellularAlt, "${device.rssi} dBm")
                StatDivider()
                DeviceStat(Icons.Default.AccessTime, "— ms")
                StatDivider()
                DeviceStat(Icons.Default.MyLocation, "Unspecified")
                StatDivider()
                DeviceStat(Icons.Default.Bluetooth, "Connectible")
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FavoriteBorder, null, tint = AppColors.TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Not bonded", fontSize = 12.sp, color = AppColors.TextSecondary)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowDown, null, tint = AppColors.TextSecondary, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun DeviceStat(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.TextSecondary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 11.sp, color = AppColors.TextSecondary)
    }
}

@Composable
private fun StatDivider() {
    Box(Modifier.height(22.dp).width(1.dp).background(AppColors.Border))
}

private fun colorizeLog(text: String) = buildAnnotatedString {
    text.lines().forEachIndexed { index, line ->
        if (index > 0) append('\n')
        if (line.isBlank()) {
            withStyle(SpanStyle(color = AppColors.TextPrimary)) { append(line) }
            return@forEachIndexed
        }
        val bracketEnd = line.indexOf(']')
        if (line.startsWith('[') && bracketEnd > 0) {
            withStyle(SpanStyle(color = AppColors.TextPrimary)) { append(line.substring(0, bracketEnd + 1)) }
            val rest = line.substring(bracketEnd + 1).trimStart()
            val restStyle = when {
                rest.contains("Scan started", ignoreCase = true) -> SpanStyle(color = AppColors.LogGreen)
                rest.contains("Ready", ignoreCase = true) -> SpanStyle(color = AppColors.LogGreen)
                rest.contains("connected", ignoreCase = true) -> SpanStyle(color = AppColors.ConnectBlue)
                rest.contains("error", ignoreCase = true) || rest.contains("failed", ignoreCase = true) ->
                    SpanStyle(color = AppColors.StatusRed)
                else -> SpanStyle(color = AppColors.TextPrimary)
            }
            if (rest.isNotEmpty()) {
                withStyle(SpanStyle(color = AppColors.TextPrimary)) { append(" ") }
                withStyle(restStyle) { append(rest) }
            }
        } else {
            val style = when {
                line.equals("Ready", ignoreCase = true) -> SpanStyle(color = AppColors.LogGreen)
                line.contains("Scan started", ignoreCase = true) -> SpanStyle(color = AppColors.LogGreen)
                else -> SpanStyle(color = AppColors.TextPrimary)
            }
            withStyle(style) { append(line) }
        }
    }
}
