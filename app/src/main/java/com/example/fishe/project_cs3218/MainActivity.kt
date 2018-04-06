package com.example.fishe.project_cs3218

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val RECORD_AUDIO_REQUEST_CODE = 101

    lateinit private var soundTransmitter: SoundTransmitter

    companion object {
        lateinit var buffer: ShortArray
        var soundFFTMag: DoubleArray  = DoubleArray(1024)

        var FFT_Len = 512
        var bufferSize: Int = 0     // in bytes, will be altered in SoundSampler.kt

        val FS = 44100     // sampling frequency
        var mx = -99999.0
        var freqResolution: Double = FS * 1.0 / FFT_Len
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // seek permission to do audio recording
        setupPermissions()
        initiateSoundTransmitting()

        sendButton?.setOnClickListener {
            val x: String? = textMessage?.text.toString()
            val charset = Charsets.UTF_8

            if (x.isNullOrBlank()) {
                textMessage?.error = "Empty Message"
                textMessage?.requestFocus()
                return@setOnClickListener
            }
            textMessage!!.text.clear()

            val byteArray = x?.toByteArray(charset)
            Toast.makeText(this, byteArray?.contentToString() , Toast.LENGTH_LONG)
                    .show()
            textView.text = byteArray?.toString(charset)
            transmitMessage(byteArray)
        }

        val button1 = findViewById<Button>(R.id.soundReceive)
        button1.setOnClickListener {
            val intent = Intent(this, SoundReceiver::class.java).apply {
            }
            startActivity(intent)
        }

    }


    private fun setupPermissions() {
        val permission = checkSelfPermission(Manifest.permission.RECORD_AUDIO)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("tag", "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE)
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_AUDIO_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i("tag", "Permission has been denied by user")
                } else {
                    Log.i("tag", "Permission has been granted by user")
                }
            }
        }
    }

    private fun initiateSoundTransmitting() {
        try {
            soundTransmitter = SoundTransmitter(this)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Cannot instantiate SoundTransmitter", Toast.LENGTH_LONG).show()
        }
    }

    private fun transmitMessage(byteArray: ByteArray?) {
        var size:Int = byteArray!!.size
        soundTransmitter.playSound(freqResolution * 180, 0.5)
        for(i in 0..size-1)
            soundTransmitter.playSound(freqResolution * (10 + byteArray?.get(i)!!.toInt()), 0.1)
        soundTransmitter.playSound(freqResolution * 200, 0.1)
    }

}
