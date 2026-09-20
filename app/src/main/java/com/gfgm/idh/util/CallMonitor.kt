package com.gfgm.idh.util

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class CallMonitor(private val context: Context) {

    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private var telephonyCallback: TelephonyCallback? = null
    private var legacyListener: PhoneStateListener? = null
    private var wasInCall = false

    fun startMonitoring(onCallEnded: () -> Unit) {
        val stateHandler: (Int) -> Unit = { state ->
            when (state) {
                TelephonyManager.CALL_STATE_OFFHOOK,
                TelephonyManager.CALL_STATE_RINGING -> {
                    wasInCall = true
                }

                TelephonyManager.CALL_STATE_IDLE -> {
                    if (wasInCall) {
                        wasInCall = false
                        onCallEnded()
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callback = Api31CallCallback(stateHandler)
            telephonyCallback = callback
            telephonyManager.registerTelephonyCallback(
                ContextCompat.getMainExecutor(context),
                callback
            )
        } else {
            @Suppress("DEPRECATION")
            legacyListener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    stateHandler(state)
                }
            }
            @Suppress("DEPRECATION")
            telephonyManager.listen(legacyListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    fun stopMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let {
                telephonyManager.unregisterTelephonyCallback(it)
                telephonyCallback = null
            }
        } else {
            @Suppress("DEPRECATION")
            legacyListener?.let {
                telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
                legacyListener = null
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private class Api31CallCallback(
    private val onStateChanged: (Int) -> Unit
) : TelephonyCallback(), TelephonyCallback.CallStateListener {
    override fun onCallStateChanged(state: Int) {
        onStateChanged(state)
    }
}