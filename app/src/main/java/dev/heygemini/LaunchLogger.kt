package dev.heygemini

import android.content.Context
import android.util.Log
import java.io.File
import java.time.Instant

object LaunchLogger {
    fun log(context: Context, message: String, error: Throwable? = null) {
        if (error == null) {
            Log.i(LOG_TAG, message)
        } else {
            Log.e(LOG_TAG, message, error)
        }

        try {
            val logFile = File(context.filesDir, LOG_FILE_NAME)
            if (logFile.length() >= MAX_LOG_FILE_BYTES) {
                trimToRecentHalf(logFile)
            }
            val errorSummary = error?.let {
                "; ${it.javaClass.simpleName}: ${it.message ?: "<no message>"}"
            }.orEmpty()
            logFile.appendText("${Instant.now()} $message$errorSummary\n")
        } catch (logError: Exception) {
            Log.e(LOG_TAG, "Unable to write persistent launch log", logError)
        }
    }

    /** Keeps the newer half so the entries just before a failure survive a rollover. */
    private fun trimToRecentHalf(logFile: File) {
        val text = logFile.readText()
        val tail = text.takeLast((MAX_LOG_FILE_BYTES / 2).toInt())
        logFile.writeText(tail.substringAfter('\n', ""))
    }

    private const val LOG_TAG = "HeyGemini"
    private const val LOG_FILE_NAME = "heygemini.log"
    private const val MAX_LOG_FILE_BYTES = 64 * 1024L
}
