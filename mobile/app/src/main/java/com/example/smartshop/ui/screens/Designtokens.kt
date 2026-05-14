package com.example.smartshop.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Palette ──────────────────────────────────────────────────────────────────
val BgColor         = Color(0xFFF0F6FF)   // page background (cool blue-white)
val SurfaceColor    = Color(0xFFFFFFFF)   // card / nav surface
val Surface2Color   = Color(0xFFEAF2FB)   // input / chip background
val BorderColor     = Color(0xFFB5D4F4)   // blue-100
val Border2Color    = Color(0xFF85B7EB)   // blue-200

val AccentColor     = Color(0xFF185FA5)   // blue-600
val AccentBgColor   = Color(0xFFE6F1FB)   // blue-50
val AccentLightColor= Color(0xFF378ADD)   // blue-400

val TextPrimary     = Color(0xFF042C53)   // blue-900
val TextMuted       = Color(0xFF185FA5)   // blue-600
val TextMuted2      = Color(0xFF85B7EB)   // blue-200

val GreenColor      = Color(0xFF3B6D11)   // unchanged
val GreenBgColor    = Color(0xFFEAF3DE)   // unchanged
val AmberColor      = Color(0xFF854F0B)   // unchanged
val AmberBgColor    = Color(0xFFFAEEDA)   // unchanged
val RedColor        = Color(0xFFA32D2D)   // unchanged
val RedBgColor      = Color(0xFFFCEBEB)   // unchanged

val HeroBorderColor = Color(0xFFB5D4F4)   // blue-100
val HeroTextDark    = Color(0xFF042C53)   // blue-900
val HeroTextMid     = Color(0xFF185FA5)   // blue-600

// ── Typography ────────────────────────────────────────────────────────────────
val FrauncesFamily = FontFamily(
    Font(com.example.smartshop.R.font.fraunces_regular, FontWeight.Normal),
    Font(com.example.smartshop.R.font.fraunces_semibold, FontWeight.SemiBold),
)
val JakartaFamily = FontFamily(
    Font(com.example.smartshop.R.font.plusjakartasans_regular, FontWeight.Normal),
    Font(com.example.smartshop.R.font.plusjakartasans_medium, FontWeight.Medium),
    Font(com.example.smartshop.R.font.plusjakartasans_semibold, FontWeight.SemiBold),
)

val AppTypography = androidx.compose.material3.Typography(
    headlineLarge  = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    headlineSmall  = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleLarge     = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium    = TextStyle(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall     = TextStyle(fontFamily = JakartaFamily,  fontWeight = FontWeight.Medium,   fontSize = 14.sp),
    bodyLarge      = TextStyle(fontFamily = JakartaFamily,  fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 22.sp),
    bodyMedium     = TextStyle(fontFamily = JakartaFamily,  fontWeight = FontWeight.Normal,   fontSize = 13.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = JakartaFamily,  fontWeight = FontWeight.Normal,   fontSize = 12.sp),
    labelSmall     = TextStyle(fontFamily = JakartaFamily,  fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.5.sp),
)

private val LightColors = lightColorScheme(
    primary            = AccentColor,
    onPrimary          = Color.White,
    primaryContainer   = AccentBgColor,
    onPrimaryContainer = HeroTextDark,
    secondary          = AccentLightColor,
    background         = BgColor,
    surface            = SurfaceColor,
    onBackground       = TextPrimary,
    onSurface          = TextPrimary,
    outline            = BorderColor,
    error              = RedColor,
)

@Composable
fun SmartShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = AppTypography,
        content     = content,
    )
}

// ─── Design tokens (single source of truth for all screens) ──────────────────
internal val AppBgColor     = Color(0xFFF0F6FF)   // cool blue-white page bg
internal val CardSoftColor  = Color(0xFFFFFFFF)   // white card surface
internal val HeroColor      = Color(0xFFE6F1FB)   // blue-50 hero banner bg
internal val MutedTextColor = Color(0xFF185FA5)   // blue-600 muted text
internal val ErrorRedColor  = Color(0xFFA32D2D)   // unchanged