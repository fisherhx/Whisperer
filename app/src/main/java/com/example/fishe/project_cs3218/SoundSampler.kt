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
import com.example.fishe.project_cs3218.MainActivity.Companion.offset
import com.example.fishe.project_cs3218.MainActivity.Companion.startPw
import com.example.fishe.project_cs3218.MainActivity.Companion.endPw
import com.example.fishe.project_cs3218.SoundReceiver.Companion.msg
import com.example.fishe.project_cs3218.SoundReceiver.Companion.prevIndex
import com.example.fishe.project_cs3218.SoundReceiver.Companion.currCount
import com.example.fishe.project_cs3218.SoundReceiver.Companion.isRepeated
import com.example.fishe.project_cs3218.SoundReceiver.Companion.isCounted
import com.example.fishe.project_cs3218.SoundReceiver.Companion.count
import com.example.fishe.project_cs3218.SoundReceiver.Companion.isEnd
import com.example.fishe.project_cs3218.SoundReceiver.Companion.isStarted
import com.example.fishe.project_cs3218.SoundReceiver.Companion.repeatedWord
import com.example.fishe.project_cs3218.SoundReceiver.Companion.threshold

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
        if (audioRecord != null && isRecording) {
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
        }
        if(index == startPw){
            isStarted = true
            threshold = 0.9* mx
            Log.i("Max Started", "start")
        }
        else if(index == endPw){
            isEnd = true
            Log.i("Max Ended", "end")
        }
        if(index > 9 && index < 197) {
            if(index == prevIndex){
                Log.i("Max Repeated Index", "Repeated")
            }
            else if(isStarted && !isEnd){
                var ascii_Value = ((index - offset)/2)+32
                val checkingAscii = ascii_Value

                if(ascii_Value == 123){
                    ascii_Value = 65
                }
                else if(ascii_Value == 124){
                    ascii_Value = 66
                }
                else if(ascii_Value == 125){
                    ascii_Value = 67
                }

                val ascii_Char = (ascii_Value).toChar()
                Log.i("Max index", ascii_Char.toString())

                //indicate that counting is finish
                if(ascii_Char.equals('.')){
                    isCounted = true
                    prevIndex = index
                }
                //Counting number of occurance by appending the character for the number to currCount and converting that value to string
                if(isRepeated && !isCounted && !ascii_Char.equals('/')){
                    if(checkingAscii>=48 && checkingAscii<=57){
                        currCount = currCount + ascii_Char
                        count = currCount.toInt()
                        Log.i("Count char", ascii_Char.toString())
                        Log.i("Count index", count.toString())
                        prevIndex = index
                    }
                }
                //Indicate start of counting repeated char
                if(ascii_Char.equals('/')){
                    isRepeated = true
                    var value = ((prevIndex - offset)/2)+32
                    if(value == 123){
                        value = 65
                    }
                    else if(value == 124){
                        value = 66
                    }
                    else if(value == 125){
                        value = 67
                    }
                    repeatedWord = value.toChar()
                    Log.i("Repeating started", ascii_Char.toString())
                    prevIndex = index
                }
                //After counting finish number of occurance, append the letter/number to the msg according to count
                if(isRepeated && isCounted && !ascii_Char.equals('.')){
                    isRepeated = false
                    isCounted = false
                    //Log.i("Count Check", count.toString())

                    for (i in 0 .. count-1){
                        if(!((checkingAscii>=33&&checkingAscii<=45)||(checkingAscii>=58&&checkingAscii<=67)||(checkingAscii>=91&&checkingAscii<=96))) {
                            msg = msg + repeatedWord
                            Log.i("Count msg repeat", msg)
                            Log.i("Count repeat", repeatedWord.toString())
                        }
                    }
                    count = 0
                    currCount = ""
                    prevIndex = index
                }
                //For single letters that are non-repeated
                else if(!isRepeated && !isCounted){
                    val str = (((index - 10)/2)+32).toString()
                    if(str != (((prevIndex - 10)/2)+32).toString()){
                        //exclude these non alpha numeric characters 64
                        if(!((checkingAscii>=33&&checkingAscii<=45)||(checkingAscii>=58&&checkingAscii<=67)||(checkingAscii>=91&&checkingAscii<=96))) {
                            msg = msg + ascii_Char
                            Log.i("Max char", ascii_Char.toString())
                        }
                    }
                    Log.i("Max index", str)
                    Log.i("Max prev index", (((prevIndex - 10)/2)+32).toString())
                    //Log.i("Max msg", msg)
                    prevIndex = index
                    count = 0
                    currCount = ""
                }
            }
        }
        mxIntensity = mx
    }
}