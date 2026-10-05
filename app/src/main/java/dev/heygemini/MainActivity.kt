package dev.heygemini

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/** A no-display trampoline that delegates the delayed launch and immediately finishes. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val state = AssistantSupport.googleAssistantState(this)
        LaunchLogger.log(
            this,
            "Trigger received; state=$state; ${AssistantSupport.settingsSummary(this)}",
        )

        when (state) {
            AssistantSupport.State.NOT_SELECTED ->
                AssistantSupport.openSettings(this, R.string.select_google_assistant)

            // Google is selected but its service is not bound: still try, the overlay may
            // come up anyway, and the service explains how to repair it if it does not.
            AssistantSupport.State.ACTIVE,
            AssistantSupport.State.SELECTED_BUT_INACTIVE -> startLaunchService(state)
        }

        LaunchLogger.log(this, "Finishing no-display trampoline activity")
        finish()
    }

    private fun startLaunchService(state: AssistantSupport.State) {
        val inactive = state == AssistantSupport.State.SELECTED_BUT_INACTIVE
        try {
            startService(
                Intent(this, GeminiLaunchService::class.java)
                    .putExtra(GeminiLaunchService.EXTRA_ASSISTANT_INACTIVE, inactive),
            )
            LaunchLogger.log(this, "Short-lived launch service requested")
        } catch (error: RuntimeException) {
            LaunchLogger.log(this, "Unable to start launch service", error)
            AssistantSupport.openSettings(this, AssistantSupport.settingsMessage(inactive))
        }
    }
}
