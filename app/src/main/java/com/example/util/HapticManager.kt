package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import java.util.concurrent.atomic.AtomicLong

/**
 * Dedicated tactile haptic feedback manager with debounce and state-transition controls.
 * Provides subtle, tactile visionOS-grade sensory responses for real-time marker/bubble detection
 * and dual-pulse confirmation for grading submissions.
 */
object HapticManager {

    private val lastDetectionTickTime = AtomicLong(0L)
    const val DETECTION_COOLDOWN_MS = 1800L

    fun shouldDebounce(now: Long): Boolean {
        val last = lastDetectionTickTime.get()
        if (now - last < DETECTION_COOLDOWN_MS) {
            return true
        }
        return !lastDetectionTickTime.compareAndSet(last, now)
    }

    fun resetCooldown() {
        lastDetectionTickTime.set(0L)
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun isSystemHapticFeedbackEnabled(context: Context): Boolean {
        return try {
            @Suppress("DEPRECATION")
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.HAPTIC_FEEDBACK_ENABLED,
                1
            ) != 0
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Subtle, crisp tactile tick triggered when a sheet's corner markers / bubbles lock into stability.
     * Guaranteed to be debounced to prevent continuous vibration fatigue.
     */
    fun performBubbleDetectedTick(context: Context, hapticsEnabled: Boolean = true) {
        if (!hapticsEnabled || !isSystemHapticFeedbackEnabled(context)) return

        val now = System.currentTimeMillis()
        if (shouldDebounce(now)) {
            return
        }

        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Predefined light tactile tick or subtle 12ms pulse
                val effect = try {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                } catch (_: Exception) {
                    VibrationEffect.createOneShot(12L, 60)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(12L)
            }
        } catch (_: Exception) {
            // Protect against platform-level vibration exceptions
        }
    }

    /**
     * Dual-pulse success confirmation haptic on grading or answer key submission.
     */
    fun performSubmissionSuccess(context: Context, hapticsEnabled: Boolean = true) {
        if (!hapticsEnabled || !isSystemHapticFeedbackEnabled(context)) return

        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 2 subtle pulses: 25ms, 60ms gap, 40ms confirmation
                val timings = longArrayOf(0L, 25L, 60L, 40L)
                val amplitudes = intArrayOf(0, 110, 0, 180)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0L, 25L, 60L, 40L)
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (_: Exception) {
            // Fallback graceful degradation
        }
    }
}
