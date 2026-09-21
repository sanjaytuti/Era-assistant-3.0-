package com.example.era.speech

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.era.emotion.EmotionEngine
import java.util.Locale

class EmotionalTTS(
    context: Context,
    private val emotionEngine: EmotionEngine,
    private val onDone: () -> Unit = {}
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var ready = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.language = Locale("en", "IN")
            }
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) { onDone() }
                @Deprecated("Deprecated")
                override fun onError(utteranceId: String?) { onDone() }
            })
            ready = true
        }
    }

    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        val v = emotionEngine.getVoiceModifiers()
        tts.setPitch(v.pitch)
        tts.setSpeechRate(v.speed)

        val params = Bundle()
        val id = "era_${System.currentTimeMillis()}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, id)
        } else {
            @Suppress("DEPRECATION")
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    fun stop() = tts.stop()
    fun shutdown() = tts.shutdown()
    fun isReady() = ready
}
