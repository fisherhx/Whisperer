package com.example.fishe.project_cs3218

import android.graphics.Bitmap
import android.media.AudioRecord
import android.util.Log
import com.example.fishe.project_cs3218.MainActivity.Companion.FFT_Len
import com.example.fishe.project_cs3218.MainActivity.Companion.FS
import com.example.fishe.project_cs3218.MainActivity.Companion.bufferSize
import com.example.fishe.project_cs3218.MainActivity.Companion.buffer
import com.example.fishe.project_cs3218.MainActivity.Companion.soundFFTMag
import com.example.fishe.project_cs3218.MainActivity.Companion.mx
import com.example.fishe.project_cs3218.SoundReceiver.Companion.msg

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D

class SoundSampler(activity: SoundReceiver) {
    var audioRecord: AudioRecord? = null
    private val audioEncoding = 2
    private val nChannels = 16
    var isRecording = false


    private var recordingThread: Thread? = null


    @Throws(Exception::class)
    fun init() {

        bufferSize = AudioRecord.getMinBufferSize(FS, nChannels, audioEncoding)
        buffer = ShortArray(bufferSize)
        isRecording = true

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
                while (isRecording) {
                    audioRecord!!.read(buffer, 0, bufferSize)
                    performFFT()
                }
            }
        }
        recordingThread!!.start()

        return

    }

    fun endRec(){
        if (audioRecord != null) {
            audioRecord!!.stop();
            audioRecord!!.release();
            isRecording = false
            //recordingThread = null;
        }
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

        for (i in 0..FFT_Len - 1) {
            soundFFT[i * 2] = buffer[i].toDouble()
            soundFFT[i * 2 + 1] = 0.0
        }

        val fft = DoubleFFT_1D(FFT_Len)
        var index = 0
        var prevIndex = 0
        fft.complexForward(soundFFT)

        mx = -99999.0
        for (i in 0 until FFT_Len) {
            val re = soundFFT[2 * i]
            val im = soundFFT[2 * i + 1]
            soundFFTMag[i] = Math.log(re * re + im * im + 0.001)
            if (soundFFTMag[i] > mx){
                mx = soundFFTMag[i]
                index = i
            }

            if(index > 25 && index < 256) {
                if(index == prevIndex){
                    continue
                }
                if(index == 180){
                    Log.i("Max Starting", "Start of msg")
                }
                else if(index == 200){
                    Log.i("Max Ending", "End of msg")
                }
                else{
                    val a = index - 10
//                    val b = (a + '0'.toInt()).toChar()
                    val b = (a).toChar()

                    val str = (index-10).toString()
                    val charset = Charsets.UTF_8
                    val array = str.toByteArray()

                    msg = msg + b
                    Log.i("Max index", str)
                    Log.i("Max char", b.toString())
                    Log.i("Max msg", msg)
                    prevIndex = index
                }
            }
        }
        mxIntensity = mx
    }

}