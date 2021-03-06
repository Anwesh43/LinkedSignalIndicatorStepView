package ui.anwesome.com.signalindicatorstepview

/**
 * Created by anweshmishra on 17/10/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.content.Context
import android.content.pm.ActivityInfo

val nodes : Int = 5

val factor : Int = 4

fun Canvas.drawSISNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val kGap : Float = (2 * gap / 3) / factor
    val sFactor : Float = 1f / factor
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(gap * i + gap, h/2)
    for (j in 0..factor-1) {
        val sc : Float = Math.min(sFactor, Math.max(0f,scale - sFactor * j)) *  factor
        save()
        translate(kGap * j, 0f)
        paint.color = Color.parseColor("#9E9E9E")
        drawLine(0f, 0f, 0f, -kGap * (j + 1), paint)
        paint.color = Color.parseColor("#3F51B5")
        drawLine(0f, 0f, 0f, -kGap * sc * (j + 1), paint)
        restore()
    }
    restore()
}

class SignalIndicatorStepView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {
        fun update(cb : (Float) -> Unit) {
            scale += (0.1f / factor) * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {
        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SISNode(var i : Int, val state : State = State()) {
        private var next : SISNode? = null
        private var prev : SISNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SISNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSISNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SISNode {
            var curr : SISNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class SignalStepIndicator(var i : Int) {

        private var root : SISNode = SISNode(0)
        private var curr : SISNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SignalIndicatorStepView) {

        private val animator : Animator = Animator(view)

        private val ssi : SignalStepIndicator = SignalStepIndicator(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            ssi.draw(canvas, paint)
            animator.animate {
                ssi.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ssi.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : SignalIndicatorStepView {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val view : SignalIndicatorStepView = SignalIndicatorStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}