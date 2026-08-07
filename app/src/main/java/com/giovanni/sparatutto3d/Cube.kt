package com.giovanni.sparatutto3d

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Cube {
    companion object {
        val vertexData = floatArrayOf(
            -1f,-1f, 1f, 0f,0f,1f,   1f,-1f, 1f, 0f,0f,1f,   1f, 1f, 1f, 0f,0f,1f,
            -1f,-1f, 1f, 0f,0f,1f,   1f, 1f, 1f, 0f,0f,1f,  -1f, 1f, 1f, 0f,0f,1f,
             1f,-1f,-1f, 0f,0f,-1f, -1f,-1f,-1f, 0f,0f,-1f, -1f, 1f,-1f, 0f,0f,-1f,
             1f,-1f,-1f, 0f,0f,-1f, -1f, 1f,-1f, 0f,0f,-1f,  1f, 1f,-1f, 0f,0f,-1f,
            -1f,-1f,-1f,-1f,0f,0f,  -1f,-1f, 1f,-1f,0f,0f,  -1f, 1f, 1f,-1f,0f,0f,
            -1f,-1f,-1f,-1f,0f,0f,  -1f, 1f, 1f,-1f,0f,0f,  -1f, 1f,-1f,-1f,0f,0f,
             1f,-1f, 1f, 1f,0f,0f,   1f,-1f,-1f, 1f,0f,0f,   1f, 1f,-1f, 1f,0f,0f,
             1f,-1f, 1f, 1f,0f,0f,   1f, 1f,-1f, 1f,0f,0f,   1f, 1f, 1f, 1f,0f,0f,
            -1f, 1f, 1f, 0f,1f,0f,   1f, 1f, 1f, 0f,1f,0f,   1f, 1f,-1f, 0f,1f,0f,
            -1f, 1f, 1f, 0f,1f,0f,   1f, 1f,-1f, 0f,1f,0f,  -1f, 1f,-1f, 0f,1f,0f,
            -1f,-1f,-1f, 0f,-1f,0f,  1f,-1f,-1f, 0f,-1f,0f,  1f,-1f, 1f, 0f,-1f,0f,
            -1f,-1f,-1f, 0f,-1f,0f,  1f,-1f, 1f, 0f,-1f,0f, -1f,-1f, 1f, 0f,-1f,0f
        )

        fun buffer(): FloatBuffer {
            val bb = ByteBuffer.allocateDirect(vertexData.size * 4)
            bb.order(ByteOrder.nativeOrder())
            val fb = bb.asFloatBuffer()
            fb.put(vertexData)
            fb.position(0)
            return fb
        }
    }
}

data class Enemy(
    var x: Float, var y: Float, var z: Float,
    var alive: Boolean = true,
    var r: Float = 1f, var g: Float = 0.2f, var b: Float = 0.2f
)
