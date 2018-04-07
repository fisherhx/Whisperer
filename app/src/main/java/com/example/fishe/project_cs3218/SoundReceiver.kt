package com.example.fishe.project_cs3218

import android.graphics.Bitmap
import android.media.AudioRecord
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.example.fishe.project_cs3218.MainActivity.Companion.FFT_Len
import com.example.fishe.project_cs3218.MainActivity.Companion.FS
import com.example.fishe.project_cs3218.MainActivity.Companion.bufferSize
import com.example.fishe.project_cs3218.MainActivity.Companion.buffer
import com.example.fishe.project_cs3218.MainActivity.Companion.message
import com.example.fishe.project_cs3218.MainActivity.Companion.mx
import com.example.fishe.project_cs3218.MainActivity.Companion.soundFFTMag
import com.example.fishe.project_cs3218.MainActivity.Companion.threshold
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D

class SoundReceiver(activity: MainActivity)  {
    var audioRecord: AudioRecord? = null
    private val audioEncoding = 2
    private val nChannels = 16


    private var recordingThread: Thread? = null

    //for decoding
    private val duration = 0.1
    private val offset = 21 //lower frequency is not used for coded because noises usually are in low frequencies
    private val bytePerSoundSignal = 16
    private val freqResolutionPerByte = 25

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

    private var soundFFT =  DoubleArray(FFT_Len * 2)

    fun performFFT() {
       // soundFFT = DoubleArray(FFT_Len * 2)
        soundFFTMag = DoubleArray(FFT_Len)

        for (i in 0..MainActivity.FFT_Len - 1) {
            soundFFT[i * 2] = MainActivity.buffer[i].toDouble()
            soundFFT[i * 2 + 1] = 0.0
        }

        val fft = DoubleFFT_1D(FFT_Len)
        fft.complexForward(soundFFT)

// perform fftshift here
        /*for (i in 0 until MainActivity.FFT_Len) {
            soundFFTTemp[i] = soundFFT[i + MainActivity.FFT_Len]
            soundFFTTemp[i + MainActivity.FFT_Len] = soundFFT[i]
        }
        for (i in 0 until MainActivity.FFT_Len * 2) {
            soundFFT[i] = soundFFTTemp[i]
        }*/

        mx = -99999.0
        var maxIndex = -1
        for (i in 0 until FFT_Len) {
            val re = soundFFT[2 * i]
            val im = soundFFT[2 * i + 1]
            //soundFFTMag[i] = Math.log(re * re + im * im + 0.001)
            //if (soundFFTMag[i] > mx) mx = soundFFTMag[i]
            soundFFTMag[i] = Math.sqrt(re * re + im * im)
            if (soundFFTMag[i] > mx) {
                mx = soundFFTMag[i]
                maxIndex = i
            }
        }
        if(maxIndex == 200) decode(8)
        threshold = mx*0.85
    }

    fun isOn(index:Int):Boolean {
        if(soundFFTMag[index] > threshold) {
            return true
        }
        return false
    }

    fun endSignal():Boolean {
        for(i in 0 until bytePerSoundSignal-1)
            if(!isOn(offset+24*i)) return false
        return true
    }

    fun decode(lengthOfMessage:Int) {
        var byteArray = ByteArray(lengthOfMessage)
        var temp = IntArray(4*bytePerSoundSignal)
        var prev = IntArray(4*bytePerSoundSignal)
        var count = 0 //start point in byteArray
        for (k in 0 until 7) {
        //while(!endSignal()) {
            var count2 = 0 //pointer in temp array
            for (i in offset until freqResolutionPerByte*bytePerSoundSignal) { //range of important signal
                if(isOn(i)) { //extracting info from sound signal
                    temp[count2] = i
                    count2++
                }
                if(temp!=prev) { // new wave of sound
                    for(j in count until Math.min(count+bytePerSoundSignal-1,lengthOfMessage-1)) {
                        var sign = 1
                        if(temp[(j - count)*4]%25 == offset) sign = -1
                        byteArray[j] = (sign*((temp[(j - count)*4 + 1]%25 - 2 - offset)*100 + (temp[(j - count)*4 + 2]%25 - 4 - offset)*10 + (temp[(j - count)*4 + 3]%25 - 14 - offset))).toByte()
                    }
                    count += bytePerSoundSignal
                }
            }
            prev = temp
        }
        message = byteArray?.toString(Charsets.UTF_8)
    }

}
