package com.example.fishe.project_cs3218

import android.media.AudioFormat.*
import android.media.AudioManager
import android.media.AudioTrack
import android.media.AudioTrack.MODE_STREAM
import com.example.fishe.project_cs3218.MainActivity.Companion.bytePerSoundSignal
import com.example.fishe.project_cs3218.MainActivity.Companion.freqResolution
import com.example.fishe.project_cs3218.MainActivity.Companion.freqResolutionPerByte
import com.example.fishe.project_cs3218.MainActivity.Companion.offset
import kotlin.math.abs

class SoundTransmitter(activity: MainActivity) {
    private val duration = 3.5

    private val mBufferSize = AudioTrack.getMinBufferSize(44100,
            CHANNEL_OUT_MONO, ENCODING_PCM_8BIT)

    private var mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC, 44100,
            CHANNEL_OUT_MONO, ENCODING_PCM_16BIT,
            mBufferSize, MODE_STREAM)

    fun playSound(byteArray: ByteArray?) {
        // AudioTrack definition
        mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                CHANNEL_OUT_MONO, ENCODING_PCM_16BIT,
                mBufferSize, MODE_STREAM)

        val lengthOfMessage = byteArray!!.size
        var frequencyArray = DoubleArray(lengthOfMessage*4) //each byte occupies 4 frequencies

        val mSound = DoubleArray((duration*44100).toInt())
        val mBuffer = ShortArray((duration*44100).toInt())

        //playStartSignal()

        //populate frequencyArray. each byte takes up 4 frequencies
        for (i in 0 until lengthOfMessage-1) {
            var temp:Int = byteArray!![i].toInt()
            var index = 4*i //index in frequencyArray
            var startingPosition = offset + freqResolutionPerByte*(i%bytePerSoundSignal) //in frequency spectrum

            if (temp < 0) {
                frequencyArray[index] = freqResolution*(startingPosition)
                temp = abs(temp)
            }
            else {
                frequencyArray[index] = freqResolution*(startingPosition + 1)
            }
            frequencyArray[index] = freqResolution*(startingPosition + temp/100 + 2)
            temp /= 100
            frequencyArray[index+1] = freqResolution*(startingPosition + temp/10 + 4)
            temp /= 10
            frequencyArray[index+2] = freqResolution*(startingPosition + temp + 14)
        }

        //play composite sine wave as sound
        for (k in 0 until lengthOfMessage/bytePerSoundSignal + Math.ceil((lengthOfMessage%bytePerSoundSignal)/10.0).toInt()) {
            var index = bytePerSoundSignal*4*k //first index in frequencyArray
            for (i in mSound.indices) {
                for (j in 0 until 4*bytePerSoundSignal - 1)
                    if(j+index < frequencyArray.size)
                        mSound[i] = mSound[i] + Math.sin(2.0 * Math.PI * i.toDouble() / (44100 / frequencyArray[index + j]))
                mBuffer[i] = (mSound[i] * java.lang.Short.MAX_VALUE).toShort()
            }

            mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume())
            mAudioTrack.play()
        }

        mAudioTrack.write(mBuffer, 0, mSound.size)
        mAudioTrack.stop()
        mAudioTrack.release()
    }

    fun playStartSignal() {
        val mSound = DoubleArray((duration*3*44100).toInt()) //start signal is longer
        val mBuffer = ShortArray((duration*3*44100).toInt())
        for (i in mSound.indices) {
            mSound[i] = Math.sin(2.0 * Math.PI * i.toDouble() / (44100 / (freqResolution*200)))
            mBuffer[i] = (mSound[i] * java.lang.Short.MAX_VALUE).toShort()
        }
        mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume())
        mAudioTrack.play()

        mAudioTrack.write(mBuffer, 0, mSound.size)
    }
}