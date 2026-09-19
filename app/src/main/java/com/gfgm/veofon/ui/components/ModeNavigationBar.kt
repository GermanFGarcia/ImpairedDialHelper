package com.gfgm.veofon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gfgm.veofon.model.AppMode
import com.gfgm.veofon.util.playKeyHaptics

// BOTTOM NAVIGATION BAR WITH 2 BUTTONS LEADING TO OTHER MODES
@Composable
fun ModeNavigationBar(
    currentMode: AppMode,
    onModeSelected: (AppMode) -> Unit
) {
    val view = LocalView.current

    Surface(
        color = Color(0xFFE0E0E0),
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (currentMode) {
                AppMode.CARDS -> {
                    // Show LIST and KEYPAD buttons
                    NavigationButton(
                        text = "",
                        icon = Icons.Default.FormatListBulleted,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            playKeyHaptics(view)
                            onModeSelected(AppMode.LIST)
                        }
                    )
                    NavigationButton(
                        text = "",
                        icon = Icons.Default.KeyboardAlt,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            playKeyHaptics(view)
                            onModeSelected(AppMode.KEYPAD)
                        }
                    )
                }

                AppMode.LIST -> {
                    // Show PHOTOS and KEYPAD buttons
                    NavigationButton(
                        text = "",
                        icon = Icons.Default.GridView,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            playKeyHaptics(view)
                            onModeSelected(AppMode.CARDS)
                        }
                    )
                    NavigationButton(
                        text = "",
                        icon = Icons.Default.KeyboardAlt,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            playKeyHaptics(view)
                            onModeSelected(AppMode.KEYPAD)
                        }
                    )
                }

                AppMode.KEYPAD -> {
                    // Show PHOTOS and LIST buttons
                    NavigationButton(
                        text = "",
                        icon = Icons.Default.GridView,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            playKeyHaptics(view)
                            onModeSelected(AppMode.CARDS)
                        }
                    )
                    NavigationButton(
                        text = "",
                        icon = Icons.Default.FormatListBulleted,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            playKeyHaptics(view)
                            onModeSelected(AppMode.LIST)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(65.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(40.dp))
            if (!text.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}