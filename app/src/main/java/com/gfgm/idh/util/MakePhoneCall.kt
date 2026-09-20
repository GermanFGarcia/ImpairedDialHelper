package com.gfgm.idh.util

import android.content.Context
import android.content.Intent
import android.net.Uri

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