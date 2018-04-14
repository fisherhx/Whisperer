package com.example.fishe.project_cs3218

import android.graphics.Bitmap
import com.example.fishe.project_cs3218.MainActivity.Companion.FFT_Len
import com.example.fishe.project_cs3218.MainActivity.Companion.mx
import com.example.fishe.project_cs3218.MainActivity.Companion.soundFFTMag
//import com.example.fishe.project_cs3218.R.id.surfaceView
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D

class FFT(activity: SoundReceiver) {

    private var soundFFT: DoubleArray = DoubleArray(1024)

    private var soundFFTTemp: DoubleArray = DoubleArray(1024)
    private var mxIntensity: Double = 0.toDouble()
    private var soundSegmented: IntArray = intArrayOf(1024)   // dummy initialization
    private var soundBuffer: ShortArray? = null
    private var soundBackgroundImage: Bitmap? = null

    //----------- perform FFT
    fun runFFT() {
        var fftThread: Thread? = null

        soundBuffer = ShortArray(1024)
        soundBackgroundImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        soundSegmented = IntArray(FFT_Len)
        soundFFT = DoubleArray(FFT_Len * 2)
        soundFFTMag = DoubleArray(FFT_Len)
        soundFFTTemp = DoubleArray(FFT_Len * 2)

        fftThread = object : Thread() {
            override fun run() {
                while (true) {
                    performFFT()
                }
            }
        }
        fftThread!!.start()
    }

    fun performFFT() {


        for (i in 0..FFT_Len - 1) {
            soundFFT[i * 2] = MainActivity.buffer[i].toDouble()
            soundFFT[i * 2 + 1] = 0.0
        }

        val fft = DoubleFFT_1D(FFT_Len)
        fft.complexForward(soundFFT)

// perform fftshift here
        for (i in 0 until FFT_Len) {
            soundFFTTemp[i] = soundFFT[i + FFT_Len]
            soundFFTTemp[i + FFT_Len] = soundFFT[i]
        }
        for (i in 0 until FFT_Len * 2) {
            soundFFT[i] = soundFFTTemp[i]
        }

        mx = -99999.0
        for (i in 0 until FFT_Len) {
            val re = soundFFT[2 * i]
            val im = soundFFT[2 * i + 1]
            soundFFTMag[i] = Math.log(re * re + im * im + 0.001)
            if (soundFFTMag[i] > mx){
                mx = soundFFTMag[i]
            }
        }

// normalize
        /*for (i in 0 until FFT_Len) {
            soundFFTMag[i] = height * 4 / 5 - soundFFTMag[i] / mx * 500
        }*/

        mxIntensity = mx
    }

}