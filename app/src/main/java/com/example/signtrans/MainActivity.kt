package com.example.signtrans

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.*
import net.gotev.speech.Speech
import net.gotev.speech.GoogleVoiceTypingDisabledException

import net.gotev.speech.SpeechRecognitionNotAvailable

import net.gotev.speech.SpeechDelegate
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        getSupportActionBar()?.setIcon(R.drawable.ic_hackmerced_icwhite)

        Speech.init(this, getPackageName());

        val btn = findViewById<ImageButton>(R.id.button)
        val aslBtn = findViewById<ImageButton>(R.id.asl_button)
        val micView = findViewById<net.gotev.speech.ui.SpeechProgressView>(R.id.progress)
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        btn.setOnClickListener {
            if (vibrator.hasVibrator()) { // Vibrator availability checking
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.EFFECT_HEAVY_CLICK))
                } else {
                    vibrator.vibrate(500) // Vibrate method for below API Level 26
                }
            }
            try {
                Speech.getInstance().startListening(micView, object : SpeechDelegate {
                    override fun onStartOfSpeech() {
                        Log.i("speech", "speech recognition is now active")
                    }

                    override fun onSpeechRmsChanged(value: Float) {
                        Log.d("speech", "rms is now: $value")
                    }

                    override fun onSpeechPartialResults(results: List<String>) {
                        val str = StringBuilder()
                        for (res in results) {
                            str.append(res).append(" ")
                        }
                        Log.i("speech", "partial result: " + str.toString().trim { it <= ' ' })
                        val tvMain = findViewById<EditText>(R.id.tv_main)
                        tvMain.setText(str.toString())
                    }

                    override fun onSpeechResult(result: String) {
                        Log.i("speech", "result: $result")
                        val tvMain = findViewById<EditText>(R.id.tv_main)
                        tvMain.setText(result)
                    }
                })
            } catch (exc: SpeechRecognitionNotAvailable) {
                Log.e("speech", "Speech recognition is not available on this device!")
                // You can prompt the user if he wants to install Google App to have
                // speech recognition, and then you can simply call:
                //
                // SpeechUtil.redirectUserToGoogleAppOnPlayStore(this);
                //
                // to redirect the user to the Google App page on Play Store
            } catch (exc: GoogleVoiceTypingDisabledException) {
                Log.e("speech", "Google voice typing must be enabled!")
            }
        }

        aslBtn.setOnClickListener{
            val intent = Intent(this, TranslatorActivity::class.java)
            val tvMain = findViewById<EditText>(R.id.tv_main)
            var str: String? = tvMain.text.toString()
            if(str.isNullOrEmpty()){
                Toast.makeText(this, "Translator box cannot be empty", Toast.LENGTH_SHORT).show()
            }else{
                intent.putExtra("myString", str)
                startActivity(intent)
                finish()
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        Speech.getInstance().shutdown();
    }
}