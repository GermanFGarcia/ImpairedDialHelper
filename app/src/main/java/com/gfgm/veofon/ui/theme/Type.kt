package com.gfgm.veofon.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gfgm.veofon.R

val OpenSansFont = FontFamily(
    Font(R.font.open_sans_bold, FontWeight.Bold),
    Font(R.font.open_sans_regular, FontWeight.Normal)
)

// Select which font family to use globally
val SelectedFont = OpenSansFont
val PlatformStyle = PlatformTextStyle(includeFontPadding = false)

val defaultTypography = Typography()

val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = SelectedFont, platformStyle = PlatformStyle)
)