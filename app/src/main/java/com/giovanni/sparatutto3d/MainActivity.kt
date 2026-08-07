package com.giovanni.sparatutto3d

import android.app.Activity
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var glView: GLSurfaceView
    private lateinit var renderer: GameRenderer
    private lateinit var scoreText: TextView

    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false
    private val dragThreshold = 12f
    private var totalDrag = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        renderer = GameRenderer(this) { score ->
            runOnUiThread { scoreText.text = "Punti: $score" }
        }

        glView = object : GLSurfaceView(this) {
            init {
                setEGLContextClientVersion(2)
                setRenderer(renderer)
                renderMode = RENDERMODE_CONTINUOUSLY
            }

            override fun onTouchEvent(event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.x
                        lastY = event.y
                        totalDrag = 0f
                        isDragging = false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.x - lastX
                        val dy = event.y - lastY
                        totalDrag += Math.abs(dx) + Math.abs(dy)
                        if (totalDrag > dragThreshold) isDragging = true
                        renderer.rotateCamera(dx, dy)
                        lastX = event.x
                        lastY = event.y
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            renderer.shoot()
                        }
                    }
                }
                return true
            }
        }

        val root = FrameLayout(this)
        root.addView(glView)

        scoreText = TextView(this).apply {
            text = "Punti: 0"
            setTextColor(Color.WHITE)
            textSize = 20f
            setPadding(24, 24, 24, 24)
        }
        root.addView(scoreText)

        setContentView(root)
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }
}
