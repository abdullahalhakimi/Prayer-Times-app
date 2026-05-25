package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = MildTeal,
    onPrimary = OnMildTeal,
    secondary = DarkSandSec,
    onSecondary = OnSlateDarkBg,
    tertiary = AmberAccent,
    background = SlateDarkBg,
    onBackground = OnSlateDarkBg,
    surface = SlateSurface,
    onSurface = OnSlateSurface,
    surfaceVariant = DarkSandSec,
    onSurfaceVariant = OnSlateDarkBg
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DeepTeal,
    onPrimary = OnDeepTeal,
    secondary = SandSec,
    onSecondary = OnSandSec,
    tertiary = AmberAccent,
    background = WarmCreame,
    onBackground = OnWarmCreame,
    surface = Color.White,
    onSurface = OnWarmCreame,
    surfaceVariant = SandSec,
    onSurfaceVariant = OnSandSec
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Default dynamicColor to false to maintain the custom teal/cream brand identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
