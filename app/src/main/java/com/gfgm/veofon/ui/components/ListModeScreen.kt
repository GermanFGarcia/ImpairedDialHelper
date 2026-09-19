package com.gfgm.veofon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.gfgm.veofon.model.DirectContact
import com.gfgm.veofon.util.fetchSystemContacts
import com.gfgm.veofon.util.makePhoneCall
import com.gfgm.veofon.util.playKeyHaptics
import kotlinx.coroutines.launch

// LIST MODE (SCROLLABLE LIST WITH UP/DOWN & CALL BUTTON)

@Composable
fun ListModeScreen() {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    var contactsList by remember { mutableStateOf<List<DirectContact>>(emptyList()) }

    LaunchedEffect(Unit) {
        contactsList = fetchSystemContacts(context)
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(12.dp)
    ) {
        // Contact List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(contactsList) { index, contact ->
                val isSelected = index == selectedIndex
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            playKeyHaptics(view)
                            selectedIndex = index
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFBBDEFB) else Color(0xFFF5F5F5)
                    ),
                    border = if (isSelected) BorderStroke(3.dp, Color(0xFF0D47A1)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // contact name
                        Text(
                            text = contact.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        // contact role
                        contact.role?.takeIf { it.isNotBlank() }?.let { roleText ->
                            Text(
                                text = roleText,
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Controls (Up, Down, Call)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Up Button
            Button(
                onClick = {
                    playKeyHaptics(view)
                    if (selectedIndex > 0) {
                        selectedIndex--
                        coroutineScope.launch {
                            listState.animateScrollToItem(selectedIndex)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF404040))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Up",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            // Down Button
            Button(
                onClick = {
                    playKeyHaptics(view)
                    if (selectedIndex < contactsList.size - 1) {
                        selectedIndex++
                        coroutineScope.launch {
                            listState.animateScrollToItem(selectedIndex)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF404040))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Down",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            // Call Selected Contact Button
            Button(
                onClick = {
                    playKeyHaptics(view)
                    makePhoneCall(context, contactsList[selectedIndex].number)
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
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
    }
}