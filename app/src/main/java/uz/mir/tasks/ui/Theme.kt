package uz.mir.tasks.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PriorityHigh = Color(0xFFE5484D)
val PriorityMedium = Color(0xFFF5A524)
val PriorityLow = Color(0xFF30A46C)

private val Light = lightColorScheme(
    primary = Color(0xFF2F6BFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE4FF),
    onPrimaryContainer = Color(0xFF001A5C),
    secondaryContainer = Color(0xFFDCE4FF),
    onSecondaryContainer = Color(0xFF001A5C),
    background = Color(0xFFF5F7FC),
    surface = Color(0xFFF5F7FC),
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFEDF0F8),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFB4C5FF),
    onPrimary = Color(0xFF002A7A),
    primaryContainer = Color(0xFF1F4FD0),
    onPrimaryContainer = Color(0xFFDCE4FF),
    secondaryContainer = Color(0xFF2A3350),
    onSecondaryContainer = Color(0xFFDCE4FF),
    background = Color(0xFF10131A),
    surface = Color(0xFF10131A),
    surfaceContainerLow = Color(0xFF191D27),
    surfaceContainer = Color(0xFF1E2330),
)

@Composable
fun MirTasksTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
