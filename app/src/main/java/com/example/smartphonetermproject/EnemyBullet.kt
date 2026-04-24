package com.example.smartphonetermproject

import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IRecyclable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class EnemyBullet private constructor(
    gctx: GameContext,
) : Sprite(gctx, R.mipmap.enemy_bullet), IBoxCollidable, IRecyclable {
    override var width = ENEMY_BULLET_WIDTH
    override var height = ENEMY_BULLET_HEIGHT
    override var x = 0f
    override var y = 0f

    private var vx = 0f
    private var vy = SPEED

    private val _collisionRect = RectF()
    override val collisionRect: RectF
        get() {
            val halfW = width * COLLISION_INSET_RATIO / 2f
            val halfH = height * COLLISION_INSET_RATIO / 2f
            _collisionRect.set(x - halfW, y - halfH, x + halfW, y + halfH)
            return _collisionRect
        }

    init {
        syncDstRect()
    }

    fun init(startX: Float, startY: Float, vx: Float = 0f, vy: Float = SPEED): EnemyBullet {
        x = startX
        y = startY
        this.vx = vx
        this.vy = vy
        syncDstRect()
        return this
    }

    override fun update(gctx: GameContext) {
        x += vx * gctx.frameTime
        y += vy * gctx.frameTime
        syncDstRect()

        val outBottom = y - height / 2f > gctx.metrics.height
        val outTop = y + height / 2f < 0f
        val outRight = x - width / 2f > gctx.metrics.width
        val outLeft = x + width / 2f < 0f
        if (outBottom || outTop || outRight || outLeft) {
            val scene = gctx.scene as? MainScene ?: return
            scene.world.remove(this, MainScene.Layer.ENEMY_BULLET)
        }
    }

    override fun onRecycle() {}

    companion object {
        const val ENEMY_BULLET_WIDTH = 80f
        const val ENEMY_BULLET_HEIGHT = 120f
        const val SPEED = 700f
        const val DAMAGE = 1
        private const val COLLISION_INSET_RATIO = 0.8f

        fun get(
            gctx: GameContext,
            x: Float,
            y: Float,
            vx: Float = 0f,
            vy: Float = SPEED,
        ): EnemyBullet {
            val scene = gctx.scene as? MainScene
                ?: return EnemyBullet(gctx).init(x, y, vx, vy)
            val bullet = scene.world.obtain(EnemyBullet::class.java) ?: EnemyBullet(gctx)
            return bullet.init(x, y, vx, vy)
        }
    }
}