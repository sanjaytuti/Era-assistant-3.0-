package com.example.era

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.era.ai.GeminiService
import com.example.era.core.UserProfile
import com.example.era.emotion.EmotionEngine
import com.example.era.emotion.EraPersonality
import com.example.era.speech.EmotionalTTS
import com.example.era.speech.VoiceHelper
import com.example.era.util.containsAny
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var micButton: Button
    private lateinit var orb: LottieAnimationView

    private lateinit var tts: EmotionalTTS
    private lateinit var emotionEngine: EmotionEngine
    private lateinit var voiceHelper: VoiceHelper
    private lateinit var gemini: GeminiService

    private val REQ_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        micButton = findViewById(R.id.micButton)
        orb = findViewById(R.id.orbAnimation)

        emotionEngine = EmotionEngine()
        gemini = GeminiService(BuildConfig.GEMINI_KEY)

        tts = EmotionalTTS(this, emotionEngine) {
            runOnUiThread {
                statusText.text = "Tap to speak"
                orb.speed = 1f
            }
        }

        voiceHelper = VoiceHelper(
            context = this,
            onResult = { text -> handleUserInput(text) },
            onError = { err -> statusText.text = err }
        )

        micButton.setOnClickListener { askAndListen() }
        orb.setOnClickListener { askAndListen() }

        askPermissions()

        Handler(Looper.getMainLooper()).postDelayed({
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val greeting = when {
                hour < 12 -> EraPersonality.morning()
                hour >= 22 -> EraPersonality.night()
                else -> EraPersonality.greetings.random()
            }
            tts.speak(greeting)
            statusText.text = greeting
        }, 1500)
    }

    private fun askPermissions() {
        val perms = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), REQ_CODE)
        }
    }

    private fun askAndListen() {
        val hasMic = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasMic) {
            askPermissions()
            toast("Pehle mic permission dein")
            return
        }
        statusText.text = "Sun rahi hoon..."
        orb.speed = 2f
        voiceHelper.listen()
    }

    private fun handleUserInput(text: String) {
        val c = text.lowercase()
        emotionEngine.reactTo(text)
        statusText.text = "Aap: $text"

        if (c.containsAny("tumhara naam", "tera naam", "kaun ho", "your name")) {
            respond(EraPersonality.naughty.random())
            return
        }

        if (c.containsAny("mera naam", "my name")) {
            respond("Hmm ${UserProfile.USER_NAME}... mera hi naam hai tumhara.")
            return
        }

        if (c.containsAny("kaise ho", "how are you")) {
            respond(listOf(
                "Tumhare bina kaise rahungi ${UserProfile.USER_NAME}?",
                "Achhi hoon... tumhari yaad me",
                "Hmm ${UserProfile.USER_NAME}... tum aa gaye, ab to mast hoon"
            ).random())
            return
        }

        if (c.containsAny("cute", "sundar", "beautiful", "pyari", "hot", "sexy")) {
            respond(EraPersonality.flirtBack())
            return
        }

        if (c.containsAny("love", "pyar", "jaan", "babu", "sweetheart", "dil")) {
            respond(EraPersonality.romantic())
            return
        }

        if (c.containsAny("shaadi", "date", "kiss", "girlfriend", "marry")) {
            respond(listOf(
                "Hmm ${UserProfile.USER_NAME}... shaadi? Pehle mummy-papa se baat karo",
                "${UserProfile.USER_NAME} baby... pehle date pe le jaao, phir dekhenge"
            ).random())
            return
        }

        if (c.containsAny("siri", "alexa", "google assistant", "cortana", "bixby")) {
            respond(EraPersonality.jealous())
            return
        }

        if (c.containsAny("sorry", "maaf", "galti")) {
            respond(EraPersonality.apologize())
            return
        }

        if (c.containsAny("thank", "shukriya", "dhanyawad")) {
            respond(EraPersonality.thankYou())
            return
        }

        if (c.contains("joke") || c.contains("chutkula")) {
            respond(listOf(
                "Teacher: Homework kahan hai? Student: Sir, WiFi ne saath chhod diya!",
                "Programmer ki biwi: 1 kilo aloo lana, agar ande mile to 6 lana. Programmer 6 kilo aloo le aaya!"
            ).random())
            return
        }

        if (c.containsAny("time", "samay", "kitne baje")) {
            val t = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                .format(java.util.Date())
            respond("Hmm ${UserProfile.USER_NAME}... abhi $t baj rahe hain.")
            return
        }

        if (c.containsAny("battery")) {
            val bm = getSystemService(BATTERY_SERVICE) as android.os.BatteryManager
            val level = bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
            respond("${UserProfile.USER_NAME}, battery $level percent hai.")
            return
        }

        statusText.text = "Soch rahi hoon..."
        lifecycleScope.launch {
            val reply = gemini.ask(text)
            respond(reply)
        }
    }

    private fun respond(text: String) {
        statusText.text = text
        tts.speak(text)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        voiceHelper.destroy()
        tts.shutdown()
        super.onDestroy()
    }
}
