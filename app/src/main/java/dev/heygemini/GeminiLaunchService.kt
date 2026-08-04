package dev.heygemini

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Keeps the process alive only while the ColorOS sidebar finishes its closing animation. */
class GeminiLaunchService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LaunchLogger.log(this, "Launch scheduled in $SIDEBAR_SETTLE_DELAY_MS ms; startId=$startId")
        mainHandler.postDelayed(
            {
                try {
                    LaunchLogger.log(this, "Delay elapsed; beginning launch; startId=$startId")
                    performLaunchHaptic()
                    launchGemini()
                } finally {
                    LaunchLogger.log(this, "Stopping launch service; startId=$startId")
                    stopSelf(startId)
                }
            },
            SIDEBAR_SETTLE_DELAY_MS,
        )
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @Suppress("DEPRECATION")
    private fun performLaunchHaptic() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(VibratorManager::class.java).defaultVibrator
            } else {
                getSystemService(Vibrator::class.java)
            }

            if (!vibrator.hasVibrator()) {
                LaunchLogger.log(this, "Haptic skipped: device reports no vibrator")
                return
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
            LaunchLogger.log(this, "Haptic requested")
        } catch (error: RuntimeException) {
            // A haptic failure should never prevent the assistant from launching.
            LaunchLogger.log(this, "Haptic request failed", error)
        }
    }

    private fun launchGemini() {
        val launchIntent = Intent(Intent.ACTION_VOICE_COMMAND)
            .setPackage(AssistantSupport.GOOGLE_APP_PACKAGE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val resolvedComponent = launchIntent.resolveActivity(packageManager)
            ?.flattenToShortString()
            ?: "<unresolved>"
        LaunchLogger.log(
            this,
            "Sending ACTION_VOICE_COMMAND; resolved=$resolvedComponent",
        )

        try {
            startActivity(launchIntent)
            LaunchLogger.log(this, "Assistant startActivity returned successfully")
        } catch (error: RuntimeException) {
            LaunchLogger.log(this, "Assistant launch failed", error)
            AssistantSupport.openSettings(this)
        }
    }

    private companion object {
        val mainHandler = Handler(Looper.getMainLooper())
        const val SIDEBAR_SETTLE_DELAY_MS = 140L
        const val LAUNCH_HAPTIC_DURATION_MS = 60L
        const val LAUNCH_HAPTIC_AMPLITUDE = 220
    }
}
