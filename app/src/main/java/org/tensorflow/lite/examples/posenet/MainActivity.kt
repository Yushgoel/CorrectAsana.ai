package org.tensorflow.lite.examples.posenet

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.Toast
import java.util.*

private var startButton: Button? = null
private var langButton: Button? = null

private var mTTS: TextToSpeech? = null

private var lang: String = "English"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTTS = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS){
                val locale = Locale("en", "IND")
                mTTS!!.language = locale
                mTTS!!.setSpeechRate(0.85f)
                mTTS!!.speak("Welcome to correct aasana dot A I", TextToSpeech.QUEUE_FLUSH, null)
                Thread.sleep(500)
                Log.d("Speak", "WOrking")
            }
        })

        startButton = findViewById(R.id.StartWorkout)

        startButton!!.setOnClickListener()
        {
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("LangToUse", lang)
            startActivity(intent)
        }

        langButton = findViewById(R.id.LanguageButton)

        langButton!!.setOnClickListener()
        {
            if (langButton!!.text == "Use Hindi"){
                lang = "Hindi"
                langButton!!.text = "Use English"
            } else{
                lang = "English"
                langButton!!.text = "Use Hindi"
            }
        }
    }
}
