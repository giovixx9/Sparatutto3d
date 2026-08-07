package com.giovanni.sparatutto3d

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameRenderer(
    private val context: Context,
    private val onScoreChanged: (Int) -> Unit
) : GLSurfaceView.Renderer {

    private var program = 0
    private lateinit var vertexBuffer: java.nio.FloatBuffer

    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var yaw = 0f
    private var pitch = 0f
    private val camX = 0f
    private val camY = 1.6f
    private val camZ = 6f

    private var score = 0
    private val enemies = mutableListOf<Enemy>()

    private var aPositionLoc = 0
    private var aNormalLoc = 0
    private var uMvpLoc = 0
    private var uModelLoc = 0
    private var uColorLoc = 0
    private var uLightDirLoc = 0

    private val vertexShader = """
        uniform mat4 uMVP;
        uniform mat4 uModel;
        attribute vec3 aPosition;
        attribute vec3 aNormal;
        varying vec3 vNormal;
        void main() {
            gl_Position = uMVP * vec4(aPosition, 1.0);
            vNormal = mat3(uModel) * aNormal;
        }
    """.trimIndent()

    private val fragmentShader = """
        precision mediump float;
        uniform vec3 uColor;
        uniform vec3 uLightDir;
        varying vec3 vNormal;
        void main() {
            float diff = max(dot(normalize(vNormal), normalize(uLightDir)), 0.25);
            gl_FragColor = vec4(uColor * diff, 1.0);
        }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.05f, 0.07f, 0.12f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        program = ShaderUtil.buildProgram(vertexShader, fragmentShader)
        vertexBuffer = Cube.buffer()

        aPositionLoc = GLES20.glGetAttribLocation(program, "aPosition")
        aNormalLoc = GLES20.glGetAttribLocation(program, "aNormal")
        uMvpLoc = GLES20.glGetUniformLocation(program, "uMVP")
        uModelLoc = GLES20.glGetUniformLocation(program, "uModel")
        uColorLoc = GLES20.glGetUniformLocation(program, "uColor")
        uLightDirLoc = GLES20.glGetUniformLocation(program, "uLightDir")

        spawnEnemies()
    }

    private fun spawnEnemies() {
        enemies.clear()
        repeat(6) { enemies.add(randomEnemy()) }
    }

    private fun randomEnemy(): Enemy {
        val x = Random.nextFloat() * 16f - 8f
        val z = -Random.nextFloat() * 14f - 4f
        val y = 0.5f
        return Enemy(
            x, y, z,
            r = Random.nextFloat() * 0.5f + 0.5f,
            g = Random.nextFloat() * 0.3f,
            b = Random.nextFloat() * 0.3f
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projMatrix, 0, 60f, ratio, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)

        val dirX = -sin(yaw) * cos(pitch)
        val dirY = -sin(pitch)
        val dirZ = -cos(yaw) * cos(pitch)

        Matrix.setLookAtM(
            viewMatrix, 0,
            camX, camY, camZ,
            camX + dirX, camY + dirY, camZ + dirZ,
            0f, 1f, 0f
        )

        GLES20.glUniform3f(uLightDirLoc, 0.4f, 1f, 0.6f)

        drawCube(0f, -0.5f, -8f, 20f, 0.05f, 20f, 0.2f, 0.25f, 0.2f)

        for (e in enemies) {
            if (!e.alive) continue
            drawCube(e.x, e.y, e.z, 0.5f, 0.5f, 0.5f, e.r, e.g, e.b)
        }
    }

    private fun drawCube(
        x: Float, y: Float, z: Float,
        sx: Float, sy: Float, sz: Float,
        r: Float, g: Float, b: Float
    ) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        Matrix.scaleM(modelMatrix, 0, sx, sy, sz)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0)

        GLES20.glUniformMatrix4fv(uMvpLoc, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelLoc, 1, false, modelMatrix, 0)
        GLES20.glUniform3f(uColorLoc, r, g, b)

        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(aPositionLoc, 3, GLES20.GL_FLOAT, false, 24, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionLoc)

        vertexBuffer.position(3)
        GLES20.glVertexAttribPointer(aNormalLoc, 3, GLES20.GL_FLOAT, false, 24, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aNormalLoc)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

    fun rotateCamera(dx: Float, dy: Float) {
        yaw -= dx * 0.005f
        pitch -= dy * 0.005f
        pitch = pitch.coerceIn(-1.4f, 1.4f)
    }

    fun shoot() {
        val dirX = -sin(yaw) * cos(pitch)
        val dirY = -sin(pitch)
        val dirZ = -cos(yaw) * cos(pitch)

        var closestDist = Float.MAX_VALUE
        var hit: Enemy? = null

        for (e in enemies) {
            if (!e.alive) continue
            val dist = rayAABBIntersect(
                camX, camY, camZ, dirX, dirY, dirZ,
                e.x - 0.5f, e.y - 0.5f, e.z - 0.5f,
                e.x + 0.5f, e.y + 0.5f, e.z + 0.5f
            )
            if (dist != null && dist < closestDist) {
                closestDist = dist
                hit = e
            }
        }

        hit?.let {
            it.alive = false
            score++
            onScoreChanged(score)
            respawn(it)
        }
    }

    private fun respawn(e: Enemy) {
        val n = randomEnemy()
        e.x = n.x; e.y = n.y; e.z = n.z
        e.r = n.r; e.g = n.g; e.b = n.b
        e.alive = true
    }

    private fun rayAABBIntersect(
        ox: Float, oy: Float, oz: Float,
        dx: Float, dy: Float, dz: Float,
        minX: Float, minY: Float, minZ: Float,
        maxX: Float, maxY: Float, maxZ: Float
    ): Float? {
        var tmin = (minX - ox) / dx
        var tmax = (maxX - ox) / dx
        if (tmin > tmax) { val t = tmin; tmin = tmax; tmax = t }

        var tymin = (minY - oy) / dy
        var tymax = (maxY - oy) / dy
        if (tymin > tymax) { val t = tymin; tymin = tymax; tymax = t }

        if (tmin > tymax || tymin > tmax) return null
        if (tymin > tmin) tmin = tymin
        if (tymax < tmax) tmax = tymax

        var tzmin = (minZ - oz) / dz
        var tzmax = (maxZ - oz) / dz
        if (tzmin > tzmax) { val t = tzmin; tzmin = tzmax; tzmax = t }

        if (tmin > tzmax || tzmin > tmax) return null
        if (tzmin > tmin) tmin = tzmin

        return if (tmin > 0) tmin else null
    }
}
