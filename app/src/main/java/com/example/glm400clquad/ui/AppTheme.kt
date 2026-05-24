package com.example.glm400clquad.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Background = Color(0xFFF8F9FD)
    val Surface = Color(0xFFFFFFFF)
    val Primary = Color(0xFF6236FF)
    val PrimaryLight = Color(0xFFEEF0FF)
    val ConnectBlue = Color(0xFF007AFF)
    val TextPrimary = Color(0xFF1A1D26)
    val TextSecondary = Color(0xFF6B7280)
    val Border = Color(0xFFE5E7EB)
    val StatusRed = Color(0xFFE53935)
    val StatusGreen = Color(0xFF2E7D32)
    val StatusOrange = Color(0xFFF57C00)
    val LogGreen = Color(0xFF2E7D32)
    val LogMuted = Color(0xFF9CA3AF)
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
