package campalans.m8.kotlinballs

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val container = findViewById<View>(R.id.container) as LinearLayout
        container.addView(campalans.m8.kotlinballs.MyAnimationView(this))
    }
    class MyAnimationView(context: Context?) : View(context) {
        val balls: ArrayList<*> = ArrayList<Any?>()
        var animation: AnimatorSet? = null

        init {
            // Animate background color
// Note that setting the background color will automatically invalidate the
// view, so that the animated color, and the bouncing balls, get redisplayed on
// every frame of the animation.
            val colorAnim: ValueAnimator =
                ObjectAnimator.ofInt(this, "backgroundColor", GREEN, BLUE)
            colorAnim.duration = 3000
            colorAnim.setEvaluator(ArgbEvaluator())
            colorAnim.repeatCount = ValueAnimator.INFINITE
            colorAnim.repeatMode = ValueAnimator.REVERSE
            colorAnim.start()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action != MotionEvent.ACTION_DOWN &&
                event.action != MotionEvent.ACTION_MOVE
            ) {
                return false
            }
            val newBall = addBall(event.x, event.y)
            // Bouncing animation with squash and stretch
            val startY = newBall.getY()
            val endY = height - 50f
            val h = height.toFloat()
            val eventY = event.y
            val duration = (500 * ((h - eventY) / h)).toInt()
            val bounceAnim: ValueAnimator = ObjectAnimator.ofFloat(newBall, "y", startY, endY)
            bounceAnim.duration = duration.toLong()
            bounceAnim.interpolator = AccelerateInterpolator()
            val squashAnim1: ValueAnimator = ObjectAnimator.ofFloat(
                newBall, "x", newBall.getX(),
                newBall.getX() - 25f
            )
            squashAnim1.duration = (duration / 4).toLong()
            squashAnim1.repeatCount = 1
            squashAnim1.repeatMode = ValueAnimator.REVERSE
            squashAnim1.interpolator = DecelerateInterpolator()
            val squashAnim2: ValueAnimator = ObjectAnimator.ofFloat(
                newBall,
                "width",
                newBall.getWidth(),
                newBall.getWidth() + 50
            )
            squashAnim2.duration = (duration / 4).toLong()
            squashAnim2.repeatCount = 1
            squashAnim2.repeatMode = ValueAnimator.REVERSE
            squashAnim2.interpolator = DecelerateInterpolator()
            val stretchAnim1: ValueAnimator = ObjectAnimator.ofFloat(
                newBall, "y", endY,
                endY + 25f
            )
            stretchAnim1.duration = (duration / 4).toLong()
            stretchAnim1.repeatCount = 1
            stretchAnim1.interpolator = DecelerateInterpolator()
            stretchAnim1.repeatMode = ValueAnimator.REVERSE
            val stretchAnim2: ValueAnimator = ObjectAnimator.ofFloat(
                newBall,
                "height",
                newBall.getHeight(),
                newBall.getHeight() - 25
            )
            stretchAnim2.duration = (duration / 4).toLong()
            stretchAnim2.repeatCount = 1
            stretchAnim2.interpolator = DecelerateInterpolator()
            stretchAnim2.repeatMode = ValueAnimator.REVERSE
            val bounceBackAnim: ValueAnimator = ObjectAnimator.ofFloat(
                newBall, "y", endY,
                startY+((endY-startY)/2)
            )
            bounceBackAnim.duration = duration.toLong()
            bounceBackAnim.interpolator = DecelerateInterpolator()
            // Sequence the down/squash&stretch/up animations
            val bouncer = AnimatorSet()
            bouncer.play(bounceAnim).before(squashAnim1)
            bouncer.play(squashAnim1).with(squashAnim2)
            bouncer.play(squashAnim1).with(stretchAnim1)
            bouncer.play(squashAnim1).with(stretchAnim2)
            bouncer.play(bounceBackAnim).after(stretchAnim2)
            // Fading animation - remove the ball when the animation is done
            val fadeAnim: ValueAnimator = ObjectAnimator.ofFloat(newBall, "alpha", 1f, 0f)
            fadeAnim.duration = 250
            fadeAnim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    balls.remove((animation as ObjectAnimator).target)
                }
            })
            // Sequence the two animations to play one after the other
            val animatorSet = AnimatorSet()
            animatorSet.play(bouncer).before(fadeAnim)
            // Start the animation
            animatorSet.start()
            return true
        }

        private fun addBall(x: Float, y: Float): ShapeHolder {
            val circle = OvalShape()
            circle.resize(50f, 50f)
            val drawable = ShapeDrawable(circle)
            val shapeHolder = ShapeHolder(drawable)
            shapeHolder.setX(x - 25f)
            shapeHolder.setY(y - 25f)
            val red = (Math.random() * 255).toInt()
            val green = (Math.random() * 255).toInt()
            val blue = (Math.random() * 255).toInt()
            val color = -0x1000000 or (red shl 16) or (green shl 8) or blue
            val paint = drawable.paint //new Paint(Paint.ANTI_ALIAS_FLAG);
            val darkColor = -0x1000000 or (red / 4 shl 16) or (green / 4 shl 8) or blue / 4
            val gradient = RadialGradient(
                37.5f, 12.5f,
                50f, color, darkColor, Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            shapeHolder.setPaint(paint)
            balls.add(shapeHolder)
            return shapeHolder
        }

        override fun onDraw(canvas: Canvas) {
            Log.d("OnDraw", "OnDraw")
            for (i in balls.indices) {
                val shapeHolder = balls[i] as ShapeHolder
                canvas.save()
                canvas.translate(shapeHolder.getX(), shapeHolder.getY())
                shapeHolder.getShape()!!.draw(canvas)
                canvas.restore()
            }
        }

        companion object {
            private const val RED = -0x7f80
            private const val BLUE = -0x7f7f01
            private const val CYAN = -0x7f0001
            private const val GREEN = -0x7f0080
        }
    }
}