package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IRecyclable
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.hypot

class ExpOrb private constructor() : IGameObject, IBoxCollidable, IRecyclable {
    var x = 0f
        private set
    var y = 0f
        private set

    override val collisionRect = RectF()

    fun init(x: Float, y: Float): ExpOrb {
        this.x = x
        this.y = y
        updateCollisionRect()
        return this
    }

    override fun update(gctx: GameContext) {
        val scene = gctx.scene as? MainScene ?: return
        val player = scene.player
        if (player.dead) return

        val dx = player.x - x
        val dy = player.y - y
        val dist = hypot(dx, dy)

        if (dist > 0f) {
            val step = ATTRACT_SPEED * gctx.frameTime
            x += dx / dist * step
            y += dy / dist * step
        }

        updateCollisionRect()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(x, y, RADIUS, fillPaint)
        canvas.drawCircle(x, y, RADIUS, strokePaint)
    }

    private fun updateCollisionRect() {
        val r = RADIUS * COLLISION_INSET_RATIO
        collisionRect.set(x - r, y - r, x + r, y + r)
    }

    override fun onRecycle() {}

    companion object {
        const val VALUE = 1
        private const val RADIUS = 18f
        private const val ATTRACT_SPEED = 800f
        private const val COLLISION_INSET_RATIO = 0.8f

        private val fillPaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.rgb(34, 211, 238)
            isAntiAlias = true
        }
        private val strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.rgb(165, 233, 255)
            isAntiAlias = true
        }

        fun get(gctx: GameContext, x: Float, y: Float): ExpOrb {
            val scene = gctx.scene as? MainScene ?: return ExpOrb().init(x, y)
            val orb = scene.world.obtain(ExpOrb::class.java) ?: ExpOrb()
            return orb.init(x, y)
        }
    }
}