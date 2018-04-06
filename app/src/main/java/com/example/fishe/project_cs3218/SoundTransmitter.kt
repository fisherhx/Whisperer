package com.example.fishe.project_cs3218

import android.media.AudioFormat.*
import android.media.AudioManager
import android.media.AudioTrack
import android.media.AudioTrack.MODE_STREAM
import com.example.fishe.project_cs3218.MainActivity.Companion.FFT_Len
import com.example.fishe.project_cs3218.MainActivity.Companion.FS


class SoundTransmitter(activity: MainActivity) {
    fun playSound(frequency: Double, duration: Int) {
        // AudioTrack definition
        val mBufferSize = AudioTrack.getMinBufferSize(44100,
                CHANNEL_OUT_MONO, ENCODING_PCM_8BIT)

        val mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                CHANNEL_OUT_MONO, ENCODING_PCM_16BIT,
                mBufferSize, MODE_STREAM)
        var freqResolution = FS / FFT_Len

        // Sine wave
        val mSound = DoubleArray(duration*44100)
        val mBuffer = ShortArray(duration*44100)
        for (i in mSound.indices) {
            mSound[i] = Math.sin(2.0 * Math.PI * i.toDouble() / (44100 / frequency))
            mBuffer[i] = (mSound[i] * java.lang.Short.MAX_VALUE).toShort()
        }

        mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume())
        mAudioTrack.play()

        mAudioTrack.write(mBuffer, 0, mSound.size)
        mAudioTrack.stop()
        mAudioTrack.release()

    }


}