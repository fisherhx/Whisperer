package com.example.fishe.project_cs3218

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val RECORD_AUDIO_REQUEST_CODE = 101

    lateinit private var soundSampler: SoundSampler

    companion object {
        lateinit var buffer: ShortArray

        var bufferSize: Int = 0     // in bytes
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // seek permission to do audio recording
        setupPermissions()


    }

    override fun onStart() {
        super.onStart()
        initiateSoundSampling()
    }


    override fun onPause() {

        soundSampler.audioRecord!!.stop()
        soundSampler.audioRecord!!.release()

        super.onPause()
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


    fun initiateSoundSampling(){
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


        Toast.makeText(this, "bufferSize = " +bufferSize, Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "buffer.size = " +buffer.size, Toast.LENGTH_SHORT).show()

    }

}
