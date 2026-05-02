package com.example.smartphonetermproject

import android.graphics.Bitmap
import android.graphics.Canvas
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

    var power: Int = DAMAGE
        private set
    var isCrit: Boolean = false
        private set

    private var hitting = false
    private var hitTime = 0f
    private val hitRect = RectF()

    private val _collisionRect = RectF()
    override val collisionRect: RectF
        get() {
            if (hitting) {
                _collisionRect.setEmpty()
                return _collisionRect
            }
            val halfW = width * COLLISION_INSET_RATIO / 2f
            val halfH = height * COLLISION_INSET_RATIO / 2f
            _collisionRect.set(x - halfW, y - halfH, x + halfW, y + halfH)
            return _collisionRect
        }

    init {
        syncDstRect()
        if (sharedHitBitmap == null) {
            sharedHitBitmap = gctx.res.getBitmap(R.mipmap.vfx_player_hit)
        }
    }

    fun init(startX: Float, startY: Float, power: Int = DAMAGE, isCrit: Boolean = false): Bullet {
        x = startX
        y = startY
        this.power = power
        this.isCrit = isCrit
        hitting = false
        hitTime = 0f
        syncDstRect()
        return this
    }

    override fun update(gctx: GameContext) {
        if (hitting) {
            hitTime -= gctx.frameTime
            if (hitTime <= 0f) {
                val scene = gctx.scene as? MainScene ?: return
                scene.world.remove(this, MainScene.Layer.BULLET)
            }
            return
        }
        y -= SPEED * gctx.frameTime
        syncDstRect()
        if (y + height / 2f < 0f) {
            val scene = gctx.scene as? MainScene ?: return
            scene.world.remove(this, MainScene.Layer.BULLET)
        }
    }

    override fun draw(canvas: Canvas) {
        if (hitting) {
            val bmp = sharedHitBitmap ?: return
            hitRect.set(x - HIT_SIZE / 2f, y - HIT_SIZE / 2f, x + HIT_SIZE / 2f, y + HIT_SIZE / 2f)
            canvas.drawBitmap(bmp, null, hitRect, null)
            return
        }
        super.draw(canvas)
    }

    fun startHitting() {
        if (hitting) return
        hitting = true
        hitTime = HIT_DURATION
    }

    override fun onRecycle() {}

    companion object {
        const val BULLET_WIDTH = 56f
        const val BULLET_HEIGHT = 112f
        const val SPEED = 1500f
        const val DAMAGE = 1
        private const val COLLISION_INSET_RATIO = 0.8f
        private var sharedHitBitmap: Bitmap? = null
        private const val HIT_DURATION = 0.1f
        private const val HIT_SIZE = 110f

        fun get(
            gctx: GameContext,
            x: Float,
            y: Float,
            power: Int = DAMAGE,
            isCrit: Boolean = false,
        ): Bullet {
            val scene = gctx.scene as? MainScene ?: return Bullet(gctx).init(x, y, power, isCrit)
            val bullet = scene.world.obtain(Bullet::class.java) ?: Bullet(gctx)
            return bullet.init(x, y, power, isCrit)
        }
    }
}