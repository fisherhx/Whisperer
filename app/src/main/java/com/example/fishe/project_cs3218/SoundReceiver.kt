package com.example.fishe.project_cs3218

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class SoundReceiver : AppCompatActivity() {
    lateinit private var soundSampler: SoundSampler
    lateinit private var soundFFT: FFT

    companion object {

        var msg: String = ""
        var prevIndex: Int = 0
        var prevLetter: String = ""
        var isRepeated: Boolean = false
        var isCounted: Boolean = false
        var count: Int = 0
        var currCount : String = ""

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

        buttonRec.setOnClickListener {
            initiateSoundSampling()
            receiveMsg.text = ""
        }
        buttonEnd.setOnClickListener {
            var textMsg = ""
            receiveMsg.text = msg
            msg = ""
            soundSampler.endRec()
            //soundSampler.audioRecord!!.stop()
        }

    }


    override fun onPause() {

        soundSampler.audioRecord!!.stop()
        soundSampler.audioRecord!!.release()

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
