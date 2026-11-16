package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class McqSemanticColors(
  val bg: Color = BgNavy,
  val card: Color = CardNavy,
  val inputSurface: Color = InputNavy,
  val border: Color = BorderNavy,
  val optionBadge: Color = OptionBadgeNavy,
  val textPrimary: Color = TextPrimary,
  val textMuted: Color = TextMuted,
  val codeText: Color = CodeText,
  val emerald: Color = EmeraldPrimary,
  val emeraldDark: Color = EmeraldDark,
  val emeraldLight: Color = EmeraldLight,
  val emeraldOnPrimary: Color = EmeraldOnPrimary,
  val emeraldSelectedBg: Color = EmeraldSelectedBg,
  val emeraldBadgeBg: Color = EmeraldBadgeBg,
  val emeraldBorder: Color = EmeraldBorder,
  val amber: Color = AmberAccent,
  val amberOnPrimary: Color = AmberOnPrimary,
  val amberSurface: Color = AmberSurface,
  val red: Color = RedAccent,
  val redSurface: Color = RedSurface,
  val redBorder: Color = RedBorder,
  val sky: Color = SkyAccent,
  val skySurface: Color = SkySurface,
)

val LocalMcqColors = staticCompositionLocalOf { McqSemanticColors() }

private val McqDarkColorScheme =
  darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldBadgeBg,
    onPrimaryContainer = EmeraldLight,
    secondary = AmberAccent,
    onSecondary = AmberOnPrimary,
    secondaryContainer = AmberSurface,
    onSecondaryContainer = AmberAccent,
    tertiary = SkyAccent,
    onTertiary = BgNavy,
    tertiaryContainer = SkySurface,
    onTertiaryContainer = SkyAccent,
    background = BgNavy,
    onBackground = TextPrimary,
    surface = CardNavy,
    onSurface = TextPrimary,
    surfaceVariant = InputNavy,
    onSurfaceVariant = TextMuted,
    outline = BorderNavy,
    outlineVariant = EmeraldBorder,
    error = RedAccent,
    onError = BgNavy,
    errorContainer = RedSurface,
    onErrorContainer = RedAccent,
  )

@Composable
fun McqQuizTheme(content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalMcqColors provides McqSemanticColors()) {
    MaterialTheme(
      colorScheme = McqDarkColorScheme,
      typography = AppTypography,
      content = content,
    )
  }
}
