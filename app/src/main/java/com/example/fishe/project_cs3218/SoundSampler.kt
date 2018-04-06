package com.example.fishe.project_cs3218

import android.graphics.Bitmap
import android.media.AudioRecord
import android.util.Log
import com.example.fishe.project_cs3218.MainActivity.Companion.FFT_Len
import com.example.fishe.project_cs3218.MainActivity.Companion.FS
import com.example.fishe.project_cs3218.MainActivity.Companion.bufferSize
import com.example.fishe.project_cs3218.MainActivity.Companion.buffer
import com.example.fishe.project_cs3218.MainActivity.Companion.soundFFTMag
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D

class SoundSampler(activity: MainActivity) {
    var audioRecord: AudioRecord? = null
    private val audioEncoding = 2
    private val nChannels = 16


    private var recordingThread: Thread? = null


    @Throws(Exception::class)
    fun init() {

        bufferSize = AudioRecord.getMinBufferSize(FS, nChannels, audioEncoding)
        buffer = ShortArray(bufferSize)

        try {
            if (audioRecord != null) {
                audioRecord!!.stop()
                audioRecord!!.release()
            }
            audioRecord = AudioRecord(1, FS, nChannels, audioEncoding, bufferSize)

        } catch (e: Exception) {
            Log.d("Error in Init() ", e.message)
            throw Exception()
        }

        audioRecord!!.startRecording()

        recordingThread = object : Thread() {
            override fun run() {
                while (true) {
                    audioRecord!!.read(buffer, 0, bufferSize)
                    performFFT()
                }
            }
        }
        recordingThread!!.start()

        return

    }

    private var soundFFT: DoubleArray = DoubleArray(1024)

    private var soundFFTTemp: DoubleArray = DoubleArray(1024)
    private var mxIntensity: Double = 0.toDouble()
    private var soundSegmented: IntArray = intArrayOf(1024)   // dummy initialization
    private var soundBuffer: ShortArray? = null
    private var soundBackgroundImage: Bitmap? = null


    fun performFFT() {
        soundBuffer = ShortArray(1024)
        soundBackgroundImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        soundSegmented = IntArray(FFT_Len)
        soundFFT = DoubleArray(FFT_Len * 2)
        soundFFTMag = DoubleArray(FFT_Len)
        soundFFTTemp = DoubleArray(FFT_Len * 2)

        for (i in 0..MainActivity.FFT_Len - 1) {
            soundFFT[i * 2] = MainActivity.buffer[i].toDouble()
            soundFFT[i * 2 + 1] = 0.0
        }

        val fft = DoubleFFT_1D(MainActivity.FFT_Len)
        fft.complexForward(soundFFT)

// perform fftshift here
        for (i in 0 until MainActivity.FFT_Len) {
            soundFFTTemp[i] = soundFFT[i + MainActivity.FFT_Len]
            soundFFTTemp[i + MainActivity.FFT_Len] = soundFFT[i]
        }
        for (i in 0 until MainActivity.FFT_Len * 2) {
            soundFFT[i] = soundFFTTemp[i]
        }

        MainActivity.mx = -99999.0
        for (i in 0 until MainActivity.FFT_Len) {
            val re = soundFFT[2 * i]
            val im = soundFFT[2 * i + 1]
            MainActivity.soundFFTMag[i] = Math.log(re * re + im * im + 0.001)
            if (MainActivity.soundFFTMag[i] > MainActivity.mx) MainActivity.mx = MainActivity.soundFFTMag[i]
        }
        mxIntensity = MainActivity.mx
    }

}