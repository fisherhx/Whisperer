package com.example.fishe.project_cs3218

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Half.toFloat
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.fishe.project_cs3218.MainActivity.Companion.FFT_Len
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D
import com.example.fishe.project_cs3218.MainActivity.Companion.bufferSize
import com.example.fishe.project_cs3218.MainActivity.Companion.buffer
import com.example.fishe.project_cs3218.MainActivity.Companion.mx
import com.example.fishe.project_cs3218.MainActivity.Companion.soundFFTMag
import java.util.*
import java.util.concurrent.Executors

class MySurfaceView : SurfaceView, SurfaceHolder.Callback {

    @Volatile var drawFlag = false

    val executor = Executors.newSingleThreadScheduledExecutor()

    private var x : Int = 0
    private var y : Int = 0

    private var line_width : Float = 6f

    private var soundLinePaint: Paint? = null
    private var soundLinePaint2: Paint? = null
    private var soundLinePaint3: Paint? = null


    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }


    override fun surfaceCreated(p0: SurfaceHolder?) {

        start()

    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {

        drawFlag = false

        executor.shutdownNow()

    }


    fun initialize() {
        holder.addCallback(this)


        soundLinePaint = Paint()
        soundLinePaint!!.setARGB(255, 0, 0, 255)
        soundLinePaint!!.strokeWidth = 3f

        soundLinePaint2 = Paint()
        soundLinePaint2!!.isAntiAlias = true
        soundLinePaint2!!.setARGB(255, 255, 0, 0)
        soundLinePaint2!!.strokeWidth = 4f

        soundLinePaint3 = Paint()
        soundLinePaint3!!.isAntiAlias = true
        soundLinePaint3!!.setARGB(255, 0, 255, 255)
        soundLinePaint3!!.strokeWidth = 3f

        drawFlag = true

    }


    fun draw() {

        if (drawFlag) {


            val c = holder.lockCanvas()

            c.drawColor(Color.LTGRAY)       // background color

            val p = Paint()

            p.setARGB(255, 255, rand(0, 128), rand(0, 255))

            p.setStrokeWidth(line_width)


            //----------------------------------------------------------

            //-----------  plot the sound wave

            val yOffset = 200.0f
            val yScale = 0.02f

            var prevX = 0.0f
            var prevY: Float = buffer[0].toFloat() * yScale
            var tempX = 0.0f
            var tempY: Float = prevY

            for (i in 1..bufferSize - 1) {
                tempX += 1
                tempY = buffer[i].toFloat() * yScale
                c.drawLine(prevX, prevY + yOffset, tempX, tempY + yOffset, p)

                prevX = tempX
                prevY = tempY

            }

            // normalize
            for (i in 0 until FFT_Len) {
                soundFFTMag[i] = height * 4 / 5 - soundFFTMag[i] / mx * 500
            }

            // display the fft results
            val xStepSz = 1
            // draw the vertical axis (at DC location)
            c.drawLine((FFT_Len / 2).toFloat(), height.toFloat(), (FFT_Len / 2).toFloat(), 0f, soundLinePaint3)
            var i = 0
            while (i < FFT_Len - 1) {
                c.drawLine((i / xStepSz).toFloat(), soundFFTMag[i].toInt().toFloat(), (i / xStepSz + 1).toFloat(), soundFFTMag[i + 1].toInt().toFloat(), soundLinePaint)

                if ((i - 12) % 50 == 0) {
                    p.color = Color.BLACK
                    p.textSize = 20f
                    c.drawText(Integer.toString(i - FFT_Len / 2), i.toFloat(), (height * 7 / 8).toFloat(), p)
                }
                i += xStepSz

            }

            holder.unlockCanvasAndPost(c)
        }
    }

    private fun start() {

        executor.scheduleAtFixedRate( {
            draw()
        }, 100, 10, java.util.concurrent.TimeUnit.MILLISECONDS )        // initial delay 100, period 10

    }


    private fun rand(from: Int, to: Int) : Int {
        return Random().nextInt(to-from) + from
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val a = event.action

        x = event.x.toInt()
        y = event.y.toInt()


        when (a) {
            MotionEvent.ACTION_DOWN -> line_width = 60f
            MotionEvent.ACTION_MOVE -> line_width = 10f
        }
        return true
    }


}