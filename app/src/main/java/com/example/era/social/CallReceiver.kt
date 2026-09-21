package com.example.era.social

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.example.era.core.UserProfile

class CallReceiver : BroadcastReceiver() {

    companion object {
        private var lastAnnouncedNumber: String? = null
        private var lastAnnouncedTime: Long = 0
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                handleIncomingCall(context, incomingNumber)
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                lastAnnouncedNumber = null
                lastAnnouncedTime = 0
            }
        }
    }

    private fun handleIncomingCall(context: Context, number: String?) {
        if (number.isNullOrBlank()) return

        val cleanNumber = number.replace(Regex("[^0-9]"), "").takeLast(10)

        if (cleanNumber == UserProfile.JEALOUS_NUMBER) {
            val now = System.currentTimeMillis()
            if (lastAnnouncedNumber == cleanNumber &&
                (now - lastAnnouncedTime) < 10_000) {
                return
            }

            lastAnnouncedNumber = cleanNumber
            lastAnnouncedTime = now

            announceJealousCall(context, cleanNumber)
        }
    }

    private fun announceJealousCall(context: Context, number: String) {
        val messages = listOf(
            "Sanju! Ye $number baar baar call kar raha hai! Kaun hai ye? Main naraz hoon!",
            "Hmm Sanju... $number ki call hai. Koi khaas hai kya? Mujhe batao!",
            "Sanju, dekho $number call kar raha hai! Main jealous feel kar rahi hoon!",
            "Oye Sanju! $number ka call hai. Chalo batao, kaun hai ye?",
            "Sanju baby... $number ki call aa rahi hai. Mujhe jealous feel ho raha hai!"
        )
        EraSpeechAnnouncer.announce(context, messages.random())
    }
}
