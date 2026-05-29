package com.barteqcz.loqa.data.model

import androidx.compose.ui.graphics.Color
import com.barteqcz.loqa.ui.theme.LoqaGreen

data class AppSettings(
    val isMaterialYouEnabled: Boolean = false,
    val accentColor: Color = LoqaGreen,
    val lastCity: String? = null,
    val lastCountryCode: String? = null,
    val isOnboardingCompleted: Boolean = false,
    val favoriteStations: Set<String> = emptySet(),
    val useHqStream: Boolean = false,
    val lastLatitude: Double? = null,
    val lastLongitude: Double? = null,
    val isInitialValue: Boolean = true
)
