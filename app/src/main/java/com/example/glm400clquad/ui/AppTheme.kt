package com.example.glm400clquad.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
