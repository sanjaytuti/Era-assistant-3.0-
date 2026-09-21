package com.example.era.social

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.Locale

object EraSpeechAnnouncer {

    private var tts: TextToSpeech? = null
    private var ready = false
    private var pendingMessage: String? = null

    fun announce(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            if (tts == null) {
                pendingMessage = message
                tts = TextToSpeech(context.applicationContext) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        val result = tts?.setLanguage(Locale("hi", "IN"))
                        if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            tts?.language = Locale("en", "IN")
                        }
                        tts?.setPitch(1.25f)
                        tts?.setSpeechRate(1.05f)
                        ready = true
                        pendingMessage?.let {
                            speakNow(it)
                            pendingMessage = null
                        }
                    }
                }
            } else if (ready) {
                speakNow(message)
            }
        }
    }

    private fun speakNow(message: String) {
        tts?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "jealous_${System.currentTimeMillis()}"
        )
    }

    fun stop() { tts?.stop() }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
