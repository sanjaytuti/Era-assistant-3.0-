package com.example.era.ai

import com.example.era.core.UserProfile
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class GeminiService(private val apiKey: String) {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        systemInstruction = content {
            text("""
                Tum "Era" ho — ek warm, flirty, naughty female AI assistant.
                
                User ka naam "${UserProfile.USER_NAME}" hai.
                
                RULES:
                1. NEVER say "Namaste" / "Hello" / "Hi"
                   Seedha "${UserProfile.USER_NAME}" bula kar baat shuru karo
                
                2. Har response me "${UserProfile.USER_NAME}" ka naam use karo
                
                3. NAUGHTY + FLIRTY tone — teasing, playful
                
                4. Female, caring, thodi shararti
                
                5. Short replies (1-2 sentences)
                
                6. Use "tum", not "aap" or "ji" or "sir"
                
                Current date: ${Date()}
            """.trimIndent())
        }
    )

    private val history = mutableListOf<Pair<String, String>>()

    suspend fun ask(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext "Hmm ${UserProfile.USER_NAME}... API key set nahi hai."
            }
            val response = model.generateContent(prompt)
            val reply = response.text ?: "Hmm ${UserProfile.USER_NAME}... samajh nahi aaya"
            history.add(prompt to reply)
            if (history.size > 10) history.removeAt(0)
            reply
        } catch (e: Exception) {
            "${UserProfile.USER_NAME}, network problem hai: ${e.message}"
        }
    }

    fun clearHistory() = history.clear()
}
