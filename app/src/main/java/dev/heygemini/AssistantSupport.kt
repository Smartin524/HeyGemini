package dev.heygemini

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.voice.VoiceInteractionService
import android.widget.Toast

object AssistantSupport {
    const val GOOGLE_APP_PACKAGE = "com.google.android.googlequicksearchbox"

    enum class State {
        /** Google's voice interaction service is bound and running. */
        ACTIVE,

        /** Google is the selected assistant, but no voice interaction service is running. */
        SELECTED_BUT_INACTIVE,

        /** Another assistant, or none, is selected. */
        NOT_SELECTED,
    }

    /**
     * Matches Google by package rather than a hard-coded service class, so a renamed
     * VoiceInteractionService in a future Google App release keeps working.
     */
    fun googleAssistantState(context: Context): State {
        val voiceInteraction = secureComponent(context, VOICE_INTERACTION_SETTING)
        if (voiceInteraction?.packageName == GOOGLE_APP_PACKAGE && isActive(context, voiceInteraction)) {
            return State.ACTIVE
        }

        val assistant = secureComponent(context, ASSISTANT_SETTING)
        return if (assistant?.packageName == GOOGLE_APP_PACKAGE) {
            State.SELECTED_BUT_INACTIVE
        } else {
            State.NOT_SELECTED
        }
    }

    fun settingsSummary(context: Context): String {
        val assistant = secureSetting(context, ASSISTANT_SETTING)
        val voiceInteraction = secureSetting(context, VOICE_INTERACTION_SETTING)
        return "assistant=$assistant; voiceInteraction=$voiceInteraction"
    }

    fun settingsMessage(assistantInactive: Boolean): Int =
        if (assistantInactive) R.string.reselect_google_assistant else R.string.select_google_assistant

    fun openSettings(context: Context, messageRes: Int) {
        val settingsIntents = listOf(
            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS),
        )

        for (intent in settingsIntents) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                Toast.makeText(context, messageRes, Toast.LENGTH_LONG).show()
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

    private fun isActive(context: Context, service: ComponentName): Boolean = try {
        VoiceInteractionService.isActiveService(context, service)
    } catch (error: RuntimeException) {
        LaunchLogger.log(context, "Unable to inspect the active assistant", error)
        false
    }

    private fun secureComponent(context: Context, name: String): ComponentName? =
        Settings.Secure.getString(context.contentResolver, name)
            ?.let(ComponentName::unflattenFromString)

    private fun secureSetting(context: Context, name: String): String =
        Settings.Secure.getString(context.contentResolver, name)
            ?.takeIf { it.isNotEmpty() }
            ?: "<unset>"

    private const val ASSISTANT_SETTING = "assistant"
    private const val VOICE_INTERACTION_SETTING = "voice_interaction_service"
}
