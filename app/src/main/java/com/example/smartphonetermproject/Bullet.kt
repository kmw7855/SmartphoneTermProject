package com.example.smartphonetermproject

import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IRecyclable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class Bullet private constructor(
    private val gctx: GameContext,
) : Sprite(gctx, R.mipmap.bullet_placeholder), IBoxCollidable, IRecyclable {
    override var width = BULLET_WIDTH
    override var height = BULLET_HEIGHT
    override var x = 0f
    override var y = 0f

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

    fun init(startX: Float, startY: Float): Bullet {
        x = startX
        y = startY
        syncDstRect()
        return this
    }

    override fun update(gctx: GameContext) {
        y -= SPEED * gctx.frameTime
        syncDstRect()
        if (y + height / 2f < 0f) {
            val scene = gctx.scene as? MainScene ?: return
            scene.world.remove(this, MainScene.Layer.BULLET)
        }
    }

    override fun onRecycle() {}

    companion object {
        const val BULLET_WIDTH = 56f
        const val BULLET_HEIGHT = 112f
        const val SPEED = 1500f
        const val DAMAGE = 1
        private const val COLLISION_INSET_RATIO = 0.8f

        fun get(gctx: GameContext, x: Float, y: Float): Bullet {
            val scene = gctx.scene as? MainScene ?: return Bullet(gctx).init(x, y)
            val bullet = scene.world.obtain(Bullet::class.java) ?: Bullet(gctx)
            return bullet.init(x, y)
        }
    }
}