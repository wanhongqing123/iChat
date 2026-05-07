package com.ichat.login.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val TitleLg   = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
val BodyMd    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal,   color = TextPrimary)
val BodySm    = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal,   color = TextSecondary)
val Caption   = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal,   color = TextTertiary)
val CodeBox   = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)

val IChatTypography = Typography(
    titleLarge = TitleLg,
    bodyMedium = BodyMd,
    bodySmall  = BodySm,
)
