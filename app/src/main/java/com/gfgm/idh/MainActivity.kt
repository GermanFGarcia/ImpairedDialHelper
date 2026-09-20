package com.gfgm.idh

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gfgm.idh.model.AppMode
import com.gfgm.idh.ui.components.CardModeScreen
import com.gfgm.idh.ui.components.KeypadModeScreen
import com.gfgm.idh.ui.components.ListModeScreen
import com.gfgm.idh.ui.components.ModeNavigationBar
import com.gfgm.idh.ui.components.SettingsScreen
import com.gfgm.idh.ui.theme.AppTheme
import com.gfgm.idh.util.CallMonitor
import com.gfgm.idh.util.playKeyHaptics

class MainActivity : ComponentActivity() {

    private lateinit var callMonitor: CallMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // request permissions for making calls and monitoring call state
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        }

        // prepare the call monitor to bring this app to top when call ends
        callMonitor = CallMonitor(this)

        // show UI
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    MainApp()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        callMonitor.startMonitoring {
            // when a call ends, brin this app to top level
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
            startActivity(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        callMonitor.stopMonitoring()
    }
}

// main UI container
@Composable
fun MainApp() {
    var currentMode by remember { mutableStateOf(AppMode.CARDS) }
    var previousMode by remember { mutableStateOf(AppMode.CARDS) }
    val view = LocalView.current

    Scaffold(
        bottomBar = {
            ModeNavigationBar(
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
            // screen Content based on selected mode
            when (currentMode) {
                AppMode.CARDS -> CardModeScreen()
                AppMode.LIST -> ListModeScreen()
                AppMode.KEYPAD -> KeypadModeScreen()
                AppMode.SETTINGS -> SettingsScreen(
                    onBack = { currentMode = previousMode }
                )
            }

            // floating settings button overlaid at top-right
            FloatingActionButton(
                onClick = {
                    playKeyHaptics(view)
                    previousMode = currentMode
                    currentMode = AppMode.SETTINGS
                },
                shape = CircleShape,
                containerColor = Color.Transparent,
                contentColor = Color.Black,
                elevation = FloatingActionButtonDefaults.elevation( // remove shadow below icon
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}



