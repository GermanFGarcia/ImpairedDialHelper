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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gfgm.idh.model.AppMode
import com.gfgm.idh.ui.components.CardModeScreen
import com.gfgm.idh.ui.components.KeypadModeScreen
import com.gfgm.idh.ui.components.ListModeScreen
import com.gfgm.idh.ui.components.ModeNavigationBar
import com.gfgm.idh.ui.theme.AppTheme
import com.gfgm.idh.util.CallMonitor

class MainActivity : ComponentActivity() {

    private lateinit var callMonitor: CallMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions for making calls and monitoring call state
        val permissions = arrayOf(
            Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        }

        callMonitor = CallMonitor(this)

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

// MAIN APP CONTAINER
@Composable
fun MainApp() {
    var currentMode by remember { mutableStateOf(AppMode.CARDS) }

    Scaffold(bottomBar = {
        ModeNavigationBar(currentMode = currentMode, onModeSelected = { newMode -> currentMode = newMode })
    }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentMode) {
                AppMode.CARDS -> CardModeScreen()
                AppMode.LIST -> ListModeScreen()
                AppMode.KEYPAD -> KeypadModeScreen()
            }
        }
    }
}
