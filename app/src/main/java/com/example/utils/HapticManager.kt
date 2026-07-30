package com.example.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object HapticManager {

    private var vibrator: Vibrator? = null

    fun init(context: Context) {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun vibrateShort() {
        vibrator?.let { v ->
            if (!v.hasVibrator()) return@let
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(30)
            }
        }
    }

    fun vibrateSmall() = vibrateShort()

    fun vibrateMedium() {
        vibrator?.let { v ->
            if (!v.hasVibrator()) return@let
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(70)
            }
        }
    }

    fun vibrateHeavy() {
        vibrator?.let { v ->
            if (!v.hasVibrator()) return@let
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(120)
            }
        }
    }

    fun vibrateStrong() = vibrateHeavy()
}
