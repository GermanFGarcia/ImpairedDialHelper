package com.gfgm.veofon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gfgm.veofon.util.makePhoneCall
import com.gfgm.veofon.util.playKeyHaptics

// KEYPAD MODE (NUMERIC KEYPAD)

@Composable
fun KeypadModeScreen() {
    var dialedNumber by remember { mutableStateOf("") }
    val context = LocalContext.current
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (dialedNumber.isEmpty()) "" else dialedNumber,
                fontSize = if (dialedNumber.length > 10) 40.sp else 60.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 60.sp,
                color = if (dialedNumber.isEmpty()) Color.Gray else Color.Black,
                textAlign = TextAlign.Center
            )
        }

        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (digit in row) {
                        KeypadButton(
                            text = digit,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (dialedNumber.length < 15) {
                                    dialedNumber += digit
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = {
                    playKeyHaptics(view)
                    if (dialedNumber.isNotEmpty()) {
                        dialedNumber = dialedNumber.dropLast(1)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF05050))
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Button(
                onClick = {
                    playKeyHaptics(view)
                    makePhoneCall(context, dialedNumber)
                },
                modifier = Modifier
                    .weight(2f)
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF50A030))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Button(
        onClick = {
            playKeyHaptics(view)
            onClick()
        },
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF505050))
    ) {
        Text(text = text, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}