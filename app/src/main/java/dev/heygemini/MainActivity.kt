package dev.heygemini

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/** A no-display trampoline that delegates the delayed launch and immediately finishes. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val assistantActive = AssistantSupport.isGoogleAssistantActive(this)
        LaunchLogger.log(
            this,
            "Trigger received; active=$assistantActive; ${AssistantSupport.settingsSummary(this)}",
        )

        if (assistantActive) {
            try {
                startService(Intent(this, GeminiLaunchService::class.java))
                LaunchLogger.log(this, "Short-lived launch service requested")
            } catch (error: RuntimeException) {
                LaunchLogger.log(this, "Unable to start launch service", error)
                AssistantSupport.openSettings(this)
            }
        } else {
            AssistantSupport.openSettings(this)
        }

        LaunchLogger.log(this, "Finishing no-display trampoline activity")
        finish()
    }
}
