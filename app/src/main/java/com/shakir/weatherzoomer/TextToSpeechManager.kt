package com.shakir.weatherzoomer

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log

object TextToSpeechManager {
    private var textToSpeech: TextToSpeech? = null

    fun initialize(context: Context) {
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(context) { status ->
                if (status != TextToSpeech.SUCCESS) {
                    // Handle initialization failure if needed
                }
            }
        }
    }

    fun speak(text: String) {
        Log.d("WEATHER_ZOOMER_LOG", "using text_to_speech ")
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}