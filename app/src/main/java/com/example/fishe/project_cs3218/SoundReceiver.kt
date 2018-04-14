package com.example.fishe.project_cs3218

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.fishe.project_cs3218.MainActivity.Companion.startPw

class SoundReceiver : AppCompatActivity() {
    lateinit private var soundSampler: SoundSampler
    lateinit private var soundFFT: FFT

    companion object {
        var msg: String = ""
        var prevIndex: Int = 0
        var isRepeated: Boolean = false
        var isCounted: Boolean = false
        var count: Int = 0
        var currCount : String = ""
        var repeatedWord : Char = ';'
        var isStarted: Boolean = false
        var isEnd: Boolean = false
        var threshold = -1.0


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_receiver)
    }
    override fun onStart() {
        super.onStart()
        //initiateFFT()
        initiateSoundSampling()
        val buttonRec = findViewById<Button>(R.id.receiveButton)
        val buttonEnd = findViewById<Button>(R.id.endButton)
        val receiveMsg : TextView = findViewById(R.id.receiveMessage)
        receiveMsg.text = startPw.toString()
        buttonRec.setOnClickListener {
            soundSampler.endRec()
            initiateSoundSampling()
            receiveMsg.text = "Recording................................."
        }
        buttonEnd.setOnClickListener {
            receiveMsg.text = msg
            msg = ""
            soundSampler.endRec()
            isStarted = false
            isEnd = false
            prevIndex = 0
            isRepeated = false
            isCounted = false
            count = 0
            currCount = ""
            repeatedWord = ';'
            isStarted = false
            isEnd = false
            threshold = -1.0
        }
    }

    override fun onPause() {

        //soundSampler.audioRecord!!.stop()
        //soundSampler.audioRecord!!.release()

        super.onPause()
    }

    private fun initiateSoundSampling(){
        try {
            soundSampler = SoundSampler(this)

        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Cannot instantiate SoundSampler", Toast.LENGTH_LONG).show()
        }

        try {
            soundSampler.init()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Cannot initialize SoundSampler.", Toast.LENGTH_LONG).show()
        }
    }

    private fun initiateFFT() {
        try {
            soundFFT = FFT(this)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Cannot instantiate SoundFFT", Toast.LENGTH_LONG).show()
        }
        try {
            soundFFT.runFFT()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Cannot run FFT.", Toast.LENGTH_LONG).show()
        }
    }

}
/*
    <com.example.fishe.project_cs3218.MySurfaceView
        android:id="@+id/surfaceView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_weight="8" />

 */