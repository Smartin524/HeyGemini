package dev.heygemini

import android.app.Activity
import android.content.ComponentName
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.VibrationAttributes
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.service.voice.VoiceInteractionService
import android.widget.Toast

/**
 * A no-display trampoline activity that invokes Gemini through the Google App.
 *
 * The activity creates no window and finishes during onCreate. A short process-local delay
 * lets the ColorOS sidebar finish closing before Gemini captures the current screen.
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isGoogleAssistantActive()) {
            performLaunchHaptic()
            scheduleGeminiLaunch()
        } else {
            openAssistantSettings()
        }

        finish()
    }

    private fun isGoogleAssistantActive(): Boolean =
        VoiceInteractionService.isActiveService(this, GOOGLE_ASSISTANT_SERVICE)

    @Suppress("DEPRECATION")
    private fun performLaunchHaptic() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            getSystemService(Vibrator::class.java)
        }

        val effect = VibrationEffect.createOneShot(
            LAUNCH_HAPTIC_DURATION_MS,
            LAUNCH_HAPTIC_AMPLITUDE,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            vibrator.vibrate(
                effect,
                VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM),
            )
        } else {
            vibrator.vibrate(effect)
        }
    }

    private fun scheduleGeminiLaunch() {
        val appContext = applicationContext
        MAIN_HANDLER.postDelayed(
            {
                try {
                    appContext.startActivity(
                        Intent(Intent.ACTION_VOICE_COMMAND)
                            .setPackage(GOOGLE_APP_PACKAGE)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                } catch (_: ActivityNotFoundException) {
                    openAssistantSettings(appContext)
                }
            },
            SIDEBAR_SETTLE_DELAY_MS,
        )
    }

    private fun openAssistantSettings() = openAssistantSettings(this)

    private companion object {
        val MAIN_HANDLER = Handler(Looper.getMainLooper())
        val GOOGLE_ASSISTANT_SERVICE = ComponentName(
            GOOGLE_APP_PACKAGE,
            "com.google.android.voiceinteraction.GsaVoiceInteractionService",
        )
        const val SIDEBAR_SETTLE_DELAY_MS = 140L
        const val LAUNCH_HAPTIC_DURATION_MS = 60L
        const val LAUNCH_HAPTIC_AMPLITUDE = 220
        const val GOOGLE_APP_PACKAGE = "com.google.android.googlequicksearchbox"

        fun openAssistantSettings(activityContext: android.content.Context) {
            val settingsIntents = listOf(
                Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
                Intent(Settings.ACTION_SETTINGS),
            )

            for (intent in settingsIntents) {
                try {
                    activityContext.startActivity(
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                    Toast.makeText(
                        activityContext,
                        R.string.select_google_assistant,
                        Toast.LENGTH_LONG,
                    ).show()
                    return
                } catch (_: ActivityNotFoundException) {
                    // Try the next, less-specific Settings destination.
                }
            }

            Toast.makeText(
                activityContext,
                R.string.assistant_settings_unavailable,
                Toast.LENGTH_LONG,
            ).show()
        }
    }
}
