package dev.heygemini

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.voice.VoiceInteractionService
import android.widget.Toast

object AssistantSupport {
    const val GOOGLE_APP_PACKAGE = "com.google.android.googlequicksearchbox"

    private val googleAssistantService = ComponentName(
        GOOGLE_APP_PACKAGE,
        "com.google.android.voiceinteraction.GsaVoiceInteractionService",
    )

    fun isGoogleAssistantActive(context: Context): Boolean = try {
        VoiceInteractionService.isActiveService(context, googleAssistantService)
    } catch (error: RuntimeException) {
        LaunchLogger.log(context, "Unable to inspect the active assistant", error)
        false
    }

    fun settingsSummary(context: Context): String {
        val assistant = secureSetting(context, ASSISTANT_SETTING)
        val voiceInteraction = secureSetting(context, VOICE_INTERACTION_SETTING)
        return "assistant=$assistant; voiceInteraction=$voiceInteraction"
    }

    fun openSettings(context: Context) {
        val settingsIntents = listOf(
            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS),
        )

        for (intent in settingsIntents) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                Toast.makeText(
                    context,
                    R.string.select_google_assistant,
                    Toast.LENGTH_LONG,
                ).show()
                LaunchLogger.log(context, "Opened assistant settings via ${intent.action}")
                return
            } catch (error: RuntimeException) {
                LaunchLogger.log(
                    context,
                    "Settings destination unavailable: ${intent.action}",
                    error,
                )
            }
        }

        Toast.makeText(
            context,
            R.string.assistant_settings_unavailable,
            Toast.LENGTH_LONG,
        ).show()
        LaunchLogger.log(context, "All assistant settings destinations failed")
    }

    private fun secureSetting(context: Context, name: String): String =
        Settings.Secure.getString(context.contentResolver, name) ?: "<unset>"

    private const val ASSISTANT_SETTING = "assistant"
    private const val VOICE_INTERACTION_SETTING = "voice_interaction_service"
}
