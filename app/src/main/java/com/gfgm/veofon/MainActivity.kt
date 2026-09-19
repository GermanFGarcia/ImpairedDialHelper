package com.gfgm.veofon

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

enum class AppMode {
    PHOTOS, LIST, KEYPAD
}

class MainActivity : ComponentActivity() {

    private var wasInCall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions for making calls and monitoring call state
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        }

        setupCallListener()

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    background = Color.White,
                    surface = Color(0xFFF0F0F0)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    MainApp()
                }
            }
        }
    }

    private fun setupCallListener() {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {
            // Modern Android 12+ API
            telephonyManager.registerTelephonyCallback(
                mainExecutor,
                object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) {
                        handleCallStateChange(state)
                    }
                }
            )
        } else {
            // Legacy Android API
            @Suppress("DEPRECATION")
            telephonyManager.listen(object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    handleCallStateChange(state)
                }
            }, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun handleCallStateChange(state: Int) {
        when (state) {
            TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> {
                // Call started/active
                wasInCall = true
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                // Call ended: Bring Veofon back to foreground
                if (wasInCall) {
                    wasInCall = false
                    bringAppToForeground()
                }
            }
        }
    }

    private fun bringAppToForeground() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

}


// Data model for a contact
data class DirectContact(
    val name: String,
    val number: String,
    val relationship: String,
    @DrawableRes val photoResId: Int? = null
)

// Helper to launch direct calls
fun makePhoneCall(context: Context, number: String) {
    if (number.isNotEmpty()) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:${Uri.encode(number)}")
        }
        try {
            context.startActivity(intent)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}

// Audio and haptic feedback
fun playSoundAndVibration(view: View) {
    try {
        view.playSoundEffect(SoundEffectConstants.CLICK)
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// MAIN APP CONTAINER
@Composable
fun MainApp() {
    var currentMode by remember { mutableStateOf(AppMode.PHOTOS) }

    Scaffold(
        bottomBar = {
            ModeNavigationBottomBar(
                currentMode = currentMode,
                onModeSelected = { newMode -> currentMode = newMode }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentMode) {
                AppMode.PHOTOS -> PhotosModeScreen()
                AppMode.LIST -> ListModeScreen()
                AppMode.KEYPAD -> LargeKeypadScreen()
            }
        }
    }
}

// BOTTOM NAVIGATION BAR WITH 2 BUTTONS LEADING TO OTHER MODES
@Composable
fun ModeNavigationBottomBar(
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
                AppMode.PHOTOS -> {
                    // Show LIST and KEYPAD buttons
                    BottomNavButton(
                        text = "LIST",
                        icon = Icons.Default.FormatListNumbered,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            playSoundAndVibration(view)
                            onModeSelected(AppMode.LIST)
                        }
                    )
                    BottomNavButton(
                        text = "KEYPAD",
                        icon = Icons.Default.Phone,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            playSoundAndVibration(view)
                            onModeSelected(AppMode.KEYPAD)
                        }
                    )
                }

                AppMode.LIST -> {
                    // Show PHOTOS and KEYPAD buttons
                    BottomNavButton(
                        text = "PHOTOS",
                        icon = Icons.Default.GridView,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            playSoundAndVibration(view)
                            onModeSelected(AppMode.PHOTOS)
                        }
                    )
                    BottomNavButton(
                        text = "KEYPAD",
                        icon = Icons.Default.Phone,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            playSoundAndVibration(view)
                            onModeSelected(AppMode.KEYPAD)
                        }
                    )
                }

                AppMode.KEYPAD -> {
                    // Show PHOTOS and LIST buttons
                    BottomNavButton(
                        text = "PHOTOS",
                        icon = Icons.Default.GridView,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            playSoundAndVibration(view)
                            onModeSelected(AppMode.PHOTOS)
                        }
                    )
                    BottomNavButton(
                        text = "LIST",
                        icon = Icons.Default.FormatListNumbered,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            playSoundAndVibration(view)
                            onModeSelected(AppMode.LIST)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavButton(
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
            Icon(imageVector = icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// -------------------------------------------------------------
// 1. PHOTOS MODE (6 DIRECT PHOTO BUTTONS)
// -------------------------------------------------------------
@Composable
fun PhotosModeScreen() {
    val context = LocalContext.current
    val view = LocalView.current

    // List of 6 contacts for Photo Mode
    val photoContacts = listOf(
        DirectContact("Marta", "666560569", "Hija", null),
        DirectContact("German", "637548550", "Hijo", null),
        DirectContact("Gemma", "617560930", "", null),
        DirectContact("Jordi", "644555666", "", null),
        DirectContact("Anna", "677888999", "", null),
        DirectContact("Pau", "600111222", "", null),
        DirectContact("Emergencias", "112", "SOS", null)
    )

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
            items(photoContacts) { contact ->
                PhotoContactCard(
                    contact = contact,
                    onClick = {
                        playSoundAndVibration(view)
                        makePhoneCall(context, contact.number)
                    }
                )
            }
        }
    }
}

@Composable
fun PhotoContactCard(
    contact: DirectContact,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contact.relationship == "SOS") Color(0xFFFFEBEE) else Color(0xFFE3F2FD)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (contact.photoResId != null) {
                Image(
                    painter = painterResource(id = contact.photoResId),
                    contentDescription = contact.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF0D47A1), CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(if (contact.relationship == "SOS") Color(0xFFD32F2F) else Color(0xFF0D47A1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = contact.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Text(
                text = contact.relationship,
                fontSize = 14.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

// -------------------------------------------------------------
// 2. LIST MODE (SCROLLABLE LIST WITH UP/DOWN & CALL BUTTON)
// -------------------------------------------------------------
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
                            playSoundAndVibration(view)
                            selectedIndex = index
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFBBDEFB) else Color(0xFFF5F5F5)
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, Color(0xFF0D47A1)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = contact.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = contact.relationship,
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
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
                    playSoundAndVibration(view)
                    if (selectedIndex > 0) {
                        selectedIndex--
                        coroutineScope.launch {
                            listState.animateScrollToItem(selectedIndex)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(70.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Up",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Down Button
            Button(
                onClick = {
                    playSoundAndVibration(view)
                    if (selectedIndex < contactsList.size - 1) {
                        selectedIndex++
                        coroutineScope.launch {
                            listState.animateScrollToItem(selectedIndex)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(70.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Down",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Call Selected Contact Button
            Button(
                onClick = {
                    playSoundAndVibration(view)
                    makePhoneCall(context, contactsList[selectedIndex].number)
                },
                modifier = Modifier
                    .weight(2f)
                    .height(70.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CALL", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

fun fetchSystemContacts(context: Context): List<DirectContact> {
    val contactsList = mutableListOf<DirectContact>()

    // Verifiquem si tenim permís abans de consultar
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED) {
        return contactsList
    }

    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null,
        null,
        "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC" // Ordenat alfabèticament
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (it.moveToNext()) {
            val name = it.getString(nameIndex) ?: "Sense nom"
            val number = it.getString(numberIndex) ?: ""

            if (number.isNotEmpty()) {
                contactsList.add(
                    DirectContact(
                        name = name,
                        number = number,
                        relationship = "Contacte" // O etiqueta per defecte
                    )
                )
            }
        }
    }

    return contactsList
}

// -------------------------------------------------------------
// 3. KEYPAD MODE (NUMERIC KEYPAD)
// -------------------------------------------------------------
@Composable
fun LargeKeypadScreen() {
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
                text = if (dialedNumber.isEmpty()) "Dial a number" else dialedNumber,
                fontSize = if (dialedNumber.length > 10) 32.sp else 40.sp,
                fontWeight = FontWeight.Bold,
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
                    playSoundAndVibration(view)
                    if (dialedNumber.isNotEmpty()) {
                        dialedNumber = dialedNumber.dropLast(1)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(65.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Button(
                onClick = {
                    playSoundAndVibration(view)
                    makePhoneCall(context, dialedNumber)
                },
                modifier = Modifier
                    .weight(2f)
                    .height(65.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CALL", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
            playSoundAndVibration(view)
            onClick()
        },
        modifier = modifier.height(62.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Text(text = text, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Color.White,
            surface = Color(0xFFF0F0F0)
        )
    ) {
        MainApp()
    }
}