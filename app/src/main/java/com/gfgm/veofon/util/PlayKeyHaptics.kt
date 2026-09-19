package com.gfgm.veofon.util

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View

// Audio and haptic feedback
fun playKeyHaptics(view: View) {
    try {
        view.playSoundEffect(SoundEffectConstants.CLICK)
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}