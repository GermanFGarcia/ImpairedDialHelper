package com.gfgm.veofon

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gfgm.veofon.model.AppMode
import com.gfgm.veofon.ui.components.KeypadModeScreen
import com.gfgm.veofon.ui.components.ListModeScreen
import com.gfgm.veofon.ui.components.ModeNavigationBar
import com.gfgm.veofon.ui.components.CardModeScreen

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

// MAIN APP CONTAINER
@Composable
fun MainApp() {
    var currentMode by remember { mutableStateOf(AppMode.CARDS) }

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
            when (currentMode) {
                AppMode.CARDS -> CardModeScreen()
                AppMode.LIST -> ListModeScreen()
                AppMode.KEYPAD -> KeypadModeScreen()
            }
        }
    }
}
