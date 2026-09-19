package com.gfgm.veofon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gfgm.veofon.model.DirectContact
import com.gfgm.veofon.util.fetchFavoriteContacts
import com.gfgm.veofon.util.makePhoneCall
import com.gfgm.veofon.util.playKeyHaptics

// CARD MODE (DIRECT CALL BUTTONS WITH PHOTOS)

@Composable
fun CardModeScreen() {
    val context = LocalContext.current
    val view = LocalView.current

    var contacts by remember { mutableStateOf<List<DirectContact>>(emptyList()) }

    LaunchedEffect(Unit) {
        contacts = fetchFavoriteContacts(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(12.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(contacts) { contact ->
                ContactCard(
                    contact = contact,
                    onClick = {
                        playKeyHaptics(view)
                        makePhoneCall(context, contact.number)
                    }
                )
            }
        }
    }
}

@Composable
fun ContactCard(
    contact: DirectContact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // contact photo: shows picture if photoUri or photoResId exists, otherwise fallback icon
            ContactAvatar(contact = contact)

            Spacer(modifier = Modifier.height(12.dp))

            // contact Name
            Text(
                text = contact.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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

@Composable
private fun ContactAvatar(contact: DirectContact) {
    val avatarSize = 80.dp
    val avatarModifier = Modifier
        .size(avatarSize)
        .clip(CircleShape)
        .border(3.dp, Color(0xFF0D47A1), CircleShape)

    when {
        // 1. Uri photo fetched from phone contacts
        !contact.photoUri.isNullOrEmpty() -> {
            AsyncImage(
                ImageRequest.Builder(LocalContext.current)
                    .data(contact.photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = contact.name,
                contentScale = ContentScale.Crop,
                modifier = avatarModifier
            )
        }
        // 2. Drawable resource photo fallback
        contact.photoResId != null -> {
            androidx.compose.foundation.Image(
                painter = painterResource(id = contact.photoResId),
                contentDescription = contact.name,
                contentScale = ContentScale.Crop,
                modifier = avatarModifier
            )
        }
        // 3. Default high-contrast icon if no photo is available
        else -> {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(Color(0xFF0D47A1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}