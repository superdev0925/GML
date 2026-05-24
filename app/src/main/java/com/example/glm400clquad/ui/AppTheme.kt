package com.example.glm400clquad.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppColors {
    val Background = Color(0xFFF5F6FA)
    val Surface = Color(0xFFFFFFFF)
    val CardMuted = Color(0xFFF0F1F5)
    val Primary = Color(0xFF6236FF)
    val PrimaryDark = Color(0xFF4B25D6)
    val PrimaryLight = Color(0xFFE8E4FF)
    val TitlePurple = Color(0xFF3D2E7A)
    val ConnectBlue = Color(0xFF007AFF)
    val TextPrimary = Color(0xFF1F2937)
    val TextSecondary = Color(0xFF9CA3AF)
    val Border = Color(0xFFE5E7EB)
    val StatusRed = Color(0xFFE53935)
    val StatusGreen = Color(0xFF43A047)
    val StatusOrange = Color(0xFFF57C00)
    val LogGreen = Color(0xFF2E7D32)
}

/** Compact typography and spacing tuned for tablet landscape (single-screen fit). */
object UiScale {
    val AppTitle = 17.sp
    val SectionTitle = 13.sp
    val CardTitle = 13.sp
    val Body = 11.sp
    val BodySmall = 10.sp
    val Caption = 9.sp
    val Status = 10.sp
    val Log = 9.sp
    val LogLine = 13.sp
    val Button = 11.sp

    val PadScreenH = 12.dp
    val PadScreenV = 8.dp
    val PadCard = 10.dp
    val PadPanel = 12.dp
    val GapSection = 8.dp
    val GapGrid = 10.dp
    val GridMax = 168.dp
    val LogMin = 64.dp

    val IconSection = 16.dp
    val IconHeader = 18.dp
    val IconSmall = 12.dp
    val Logo = 30.dp
    val Slot = 36.dp
    val BtCircle = 32.dp
}

@Composable
fun QuadLaserTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = AppColors.Primary,
        onPrimary = Color.White,
        background = AppColors.Background,
        surface = AppColors.Surface,
        onBackground = AppColors.TextPrimary,
        onSurface = AppColors.TextPrimary,
        onSurfaceVariant = AppColors.TextSecondary,
        outline = AppColors.Border,
    )
    MaterialTheme(colorScheme = scheme, content = content)
}
