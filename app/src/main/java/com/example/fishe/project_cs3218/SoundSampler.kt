package com.example.fishe.project_cs3218

import android.media.AudioRecord
import android.util.Log
import com.example.fishe.project_cs3218.MainActivity.Companion.bufferSize
import com.example.fishe.project_cs3218.MainActivity.Companion.buffer

class SoundSampler(activity: MainActivity) {
    private val FS = 16000     // sampling frequency
    var audioRecord: AudioRecord? = null
    private val audioEncoding = 2
    private val nChannels = 16


    private var recordingThread: Thread? = null


    @Throws(Exception::class)
    fun init() {


        try {
            if (audioRecord != null) {
                audioRecord!!.stop()
                audioRecord!!.release()
            }
            audioRecord = AudioRecord(1, FS, nChannels, audioEncoding, AudioRecord.getMinBufferSize(FS, nChannels, audioEncoding))

        } catch (e: Exception) {
            Log.d("Error in Init() ", e.message)
            throw Exception()
        }


        bufferSize = AudioRecord.getMinBufferSize(FS, nChannels, audioEncoding)
        buffer = ShortArray(bufferSize)


        audioRecord!!.startRecording()

        recordingThread = object : Thread() {
            override fun run() {
                while (true) {

                    audioRecord!!.read(buffer, 0, bufferSize)


                }
            }
        }
        recordingThread!!.start()

        return

    }

}