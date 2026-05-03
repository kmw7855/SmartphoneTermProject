package com.example.smartphonetermproject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IRecyclable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.collidesWith
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class LaserBeam private constructor(
    private val gctx: GameContext,
) : IGameObject, IBoxCollidable, IRecyclable {
    private var x = 0f
    private var beamBottom = 0f

    private var lifetime = 0f
    private var elapsed = 0f
    private var tickCooldown = 0f
    private var beamHalf = 0f

    private val beamRect = RectF()
    override val collisionRect = RectF()

    init {
        if (sharedBitmap == null) sharedBitmap = gctx.res.getBitmap(R.mipmap.weapon_laser)
    }

    fun init(startBottom: Float, lifetime: Float, beamHalf: Float): LaserBeam {
        this.beamBottom = startBottom
        this.lifetime = lifetime
        this.elapsed = 0f
        this.tickCooldown = 0f
        this.beamHalf = beamHalf
        return this
    }

    override fun update(gctx: GameContext) {
        val scene = gctx.scene as? MainScene ?: return

        elapsed += gctx.frameTime
        if (elapsed >= lifetime) {
            scene.world.remove(this, MainScene.Layer.LASER)
            return
        }

        val player = scene.player
        x = player.x
        beamBottom = player.y - Player.PLAYER_HEIGHT / 2f - Player.BULLET_OFFSET
        updateCollisionRect()

        tickCooldown -= gctx.frameTime
        if (tickCooldown <= 0f) {
            tickCooldown = LASER_TICK_INTERVAL
            scene.world.forEachReversedAt(MainScene.Layer.ENEMY) { enemyObj ->
                val enemy = enemyObj as? Enemy ?: return@forEachReversedAt
                if (collidesWith(enemy)) {
                    val (rawPower, isCrit) = player.calculatePower()
                    val tickPower = (rawPower / LASER_DAMAGE_DIVISOR).coerceAtLeast(1)
                    enemy.decreaseLife(tickPower)
                    scene.spawnDamagePopup(enemy.x, enemy.y, tickPower, isCrit)
                    if (enemy.dead) {
                        enemy.startDying(scene)
                        scene.addScore(enemy.score)
                    }
                }
            }
        }
    }

    override fun draw(canvas: Canvas) {
        val bmp = sharedBitmap ?: return
        beamRect.set(x - beamHalf, 0f, x + beamHalf, beamBottom)
        canvas.drawBitmap(bmp, null, beamRect, null)
    }

    private fun updateCollisionRect() {
        val half = beamHalf * COLLISION_INSET_RATIO
        collisionRect.set(x - half, 0f, x + half, beamBottom)
    }

    override fun onRecycle() {}

    companion object {
        private const val LASER_TICK_INTERVAL = 0.1f
        private const val LASER_DAMAGE_DIVISOR = 4
        private const val COLLISION_INSET_RATIO = 0.15f

        private var sharedBitmap: Bitmap? = null

        fun get(
            gctx: GameContext,
            startBottom: Float,
            lifetime: Float,
            beamHalf: Float,
        ): LaserBeam {
            val scene = gctx.scene as? MainScene
                ?: return LaserBeam(gctx).init(startBottom, lifetime, beamHalf)
            val laser = scene.world.obtain(LaserBeam::class.java) ?: LaserBeam(gctx)
            return laser.init(startBottom, lifetime, beamHalf)
        }
    }
}
