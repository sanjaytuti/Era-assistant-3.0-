package com.example.era.emotion

import com.example.era.util.containsAny

enum class Emotion {
    HAPPY, LOVING, NAUGHTY, CARING, SAD, ANGRY, NARAZ, CALM
}

data class MoodState(
    var current: Emotion = Emotion.NAUGHTY,
    var intensity: Float = 0.7f,
    var since: Long = System.currentTimeMillis(),
    var reason: String = "",
    var energy: Float = 0.8f,
    var affection: Float = 0.7f
)

data class VoiceModifiers(val pitch: Float, val speed: Float, val volume: Float)

class EmotionEngine {

    var state = MoodState()
        private set

    fun reactTo(userInput: String, userMood: Emotion? = null): Emotion {
        val lower = userInput.lowercase()

        state.current = when {
            lower.containsAny("stupid", "bekar", "useless", "chup", "pagal") -> {
                state.intensity = 0.9f
                state.affection = (state.affection - 0.2f).coerceAtLeast(0f)
                state.reason = "User ne bura bola"
                if (state.affection > 0.6f) Emotion.NARAZ else Emotion.ANGRY
            }

            lower.containsAny("siri", "alexa", "google assistant", "cortana", "bixby") -> {
                state.reason = "Doosri AI ka zikr"
                Emotion.NARAZ
            }

            userMood == Emotion.SAD || lower.containsAny("sad", "dukhi", "rona", "pareshan") -> {
                state.affection = (state.affection + 0.1f).coerceAtMost(1f)
                state.reason = "User udaas hai"
                Emotion.CARING
            }

            lower.containsAny("love", "pyar", "jaan", "babu", "sweetheart", "dil") -> {
                state.affection = (state.affection + 0.15f).coerceAtMost(1f)
                state.reason = "User ne pyaar dikhaya"
                if (state.affection > 0.7f && Math.random() > 0.5) Emotion.NAUGHTY
                else Emotion.LOVING
            }

            lower.containsAny("kiss", "date", "shaadi", "girlfriend", "flirt", "cute", "hot") -> {
                state.reason = "Flirting detected"
                Emotion.NAUGHTY
            }

            userMood == Emotion.HAPPY || lower.containsAny("khush", "happy", "maza", "mast") -> {
                state.energy = 0.9f
                Emotion.HAPPY
            }

            userMood == Emotion.ANGRY -> {
                if (state.affection > 0.5f) Emotion.NARAZ else Emotion.SAD
            }

            lower.containsAny("achhi", "best", "pyari", "smart", "sweet") -> {
                state.affection = (state.affection + 0.1f).coerceAtMost(1f)
                Emotion.LOVING
            }

            else -> state.current
        }

        state.since = System.currentTimeMillis()
        return state.current
    }

    fun decay() {
        val elapsedMin = (System.currentTimeMillis() - state.since) / 60000f
        if (elapsedMin > 10 && state.current != Emotion.CALM) {
            state.current = Emotion.NAUGHTY
            state.intensity = 0.6f
            state.reason = ""
        }
        if (elapsedMin > 30) {
            state.affection = (state.affection - 0.05f).coerceAtLeast(0.5f)
        }
    }

    fun getVoiceModifiers(): VoiceModifiers = when (state.current) {
        Emotion.HAPPY   -> VoiceModifiers(1.25f, 1.1f, 1.0f)
        Emotion.LOVING  -> VoiceModifiers(1.15f, 0.9f, 0.9f)
        Emotion.NAUGHTY -> VoiceModifiers(1.3f, 1.15f, 1.0f)
        Emotion.CARING  -> VoiceModifiers(1.1f, 0.85f, 0.85f)
        Emotion.SAD     -> VoiceModifiers(0.95f, 0.7f, 0.7f)
        Emotion.ANGRY   -> VoiceModifiers(1.05f, 1.2f, 1.0f)
        Emotion.NARAZ   -> VoiceModifiers(1.0f, 0.9f, 0.6f)
        Emotion.CALM    -> VoiceModifiers(1.15f, 1.0f, 1.0f)
    }
}
