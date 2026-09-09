package com.rustech.cpplearn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val RustechColorScheme = darkColorScheme(
    primary = RustechCyan,
    secondary = RustechAmber,
    tertiary = RustechGreen,
    background = RustechNavyDark,
    surface = RustechSurface,
    onPrimary = RustechNavyDark,
    onSecondary = RustechNavyDark,
    onBackground = RustechTextPrimary,
    onSurface = RustechTextPrimary,
    error = RustechRed
)

val CppLearnTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp)
)

@Composable
fun CppLearnAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RustechColorScheme,
        typography = CppLearnTypography,
        content = content
    )
}
