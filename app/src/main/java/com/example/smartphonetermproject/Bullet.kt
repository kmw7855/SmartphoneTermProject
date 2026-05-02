package com.example.smartphonetermproject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IRecyclable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.atan2

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

    private var vx = 0f
    private var vy = -SPEED
    private var hitBitmap: Bitmap? = null

    private var hitting = false
    private var hitTime = 0f
    private val hitRect = RectF()

    private var rotationDeg = 0f

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
    }

    fun init(
        startX: Float,
        startY: Float,
        power: Int = DAMAGE,
        isCrit: Boolean = false,
        vx: Float = 0f,
        vy: Float = -SPEED,
        spriteResId: Int = R.mipmap.bullet_placeholder,
        hitVfxResId: Int = R.mipmap.vfx_player_hit,
    ): Bullet {
        x = startX
        y = startY
        this.power = power
        this.isCrit = isCrit
        this.vx = vx
        this.vy = vy
        rotationDeg = if (vx == 0f && vy == 0f) 0f
        else Math.toDegrees(atan2(vy, vx).toDouble()).toFloat() + 90f
        bitmap = gctx.res.getBitmap(spriteResId)
        bitmap = gctx.res.getBitmap(spriteResId)
        hitBitmap = sharedHitBitmaps.getOrPut(hitVfxResId) { gctx.res.getBitmap(hitVfxResId) }
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
        x += vx * gctx.frameTime
        y += vy * gctx.frameTime
        syncDstRect()
        val out =
            (y - height / 2f > gctx.metrics.height) || (y + height / 2f < 0f) ||
                    (x - width / 2f > gctx.metrics.width) || (x + width / 2f < 0f)
        if (out) {
            val scene = gctx.scene as? MainScene ?: return
            scene.world.remove(this, MainScene.Layer.BULLET)
        }
    }

    override fun draw(canvas: Canvas) {
        if (hitting) {
            val bmp = hitBitmap ?: return
            hitRect.set(x - HIT_SIZE / 2f, y - HIT_SIZE / 2f, x + HIT_SIZE / 2f, y + HIT_SIZE / 2f)
            canvas.drawBitmap(bmp, null, hitRect, null)
            return
        }
        canvas.save()
        canvas.rotate(rotationDeg, x, y)
        super.draw(canvas)
        canvas.restore()
    }

    fun startHitting() {
        if (hitting) return
        hitting = true
        hitTime = HIT_DURATION
    }

    override fun onRecycle() {}

    companion object {
        const val BULLET_WIDTH = 80f
        const val BULLET_HEIGHT = 160f
        const val SPEED = 1500f
        const val DAMAGE = 1
        private const val COLLISION_INSET_RATIO = 0.3f
        private const val HIT_DURATION = 0.1f
        private const val HIT_SIZE = 110f

        private val sharedHitBitmaps = mutableMapOf<Int, Bitmap>()

        fun get(
            gctx: GameContext,
            x: Float,
            y: Float,
            power: Int = DAMAGE,
            isCrit: Boolean = false,
            vx: Float = 0f,
            vy: Float = -SPEED,
            spriteResId: Int = R.mipmap.bullet_placeholder,
            hitVfxResId: Int = R.mipmap.vfx_player_hit,
        ): Bullet {
            val scene = gctx.scene as? MainScene
                ?: return Bullet(gctx).init(x, y, power, isCrit, vx, vy, spriteResId, hitVfxResId)
            val bullet = scene.world.obtain(Bullet::class.java) ?: Bullet(gctx)
            return bullet.init(x, y, power, isCrit, vx, vy, spriteResId, hitVfxResId)
        }
    }
}