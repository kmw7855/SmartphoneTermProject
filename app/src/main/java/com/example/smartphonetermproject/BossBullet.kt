package com.example.smartphonetermproject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IRecyclable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.atan2

class BossBullet private constructor(
    private val gctx: GameContext,
) : Sprite(gctx, R.mipmap.boss_bullet_4), IBoxCollidable, IRecyclable {

    enum class Type(
        val resId: Int,
        val width: Float,
        val height: Float,
        val damage: Int,
    ) {
        SHARD(R.mipmap.boss_bullet_4, 110f, 140f, 1),
        SCYTHE(R.mipmap.boss_bullet_3, 150f, 140f, 1),
        AIMED(R.mipmap.boss_bullet_1, 90f, 160f, 1),
        CORE(R.mipmap.boss_bullet_2, 160f, 160f, 2),
    }

    override var width = 0f
    override var height = 0f
    override var x = 0f
    override var y = 0f

    private var vx = 0f
    private var vy = 0f
    private lateinit var type: Type
    val damage: Int get() = type.damage

    private var hitting = false
    private var hitTime = 0f
    private val hitSrcRect = Rect()
    private val hitDstRect = RectF()

    private val _collisionRect = RectF()
    override val collisionRect: RectF
        get() {
            if (hitting) {
                _collisionRect.setEmpty()
                return _collisionRect
            }
            val halfW = width * COLLISION_INSET_RATIO_X / 2f
            val halfH = height * COLLISION_INSET_RATIO_Y / 2f
            _collisionRect.set(x - halfW, y - halfH, x + halfW, y + halfH)
            return _collisionRect
        }

    init {
        if (sharedHitBitmap == null) {
            sharedHitBitmap = gctx.res.getBitmap(R.mipmap.boss_hit_vfx)
        }
    }

    fun init(startX: Float, startY: Float, vx: Float, vy: Float, type: Type): BossBullet {
        x = startX
        y = startY
        this.vx = vx
        this.vy = vy
        this.type = type
        this.bitmap = gctx.res.getBitmap(type.resId)
        this.width = type.width
        this.height = type.height
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
                scene.world.remove(this, MainScene.Layer.ENEMY_BULLET)
            }
            return
        }
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

    override fun draw(canvas: Canvas) {
        if (hitting) {
            val bmp = sharedHitBitmap ?: return
            val frameW = bmp.width / HIT_FRAME_COUNT
            val elapsed = HIT_DURATION - hitTime
            val frameIndex = (elapsed * HIT_FPS).toInt().coerceIn(0, HIT_FRAME_COUNT - 1)
            hitSrcRect.set(frameIndex * frameW, 0, (frameIndex + 1) * frameW, bmp.height)
            hitDstRect.set(
                x - HIT_DISPLAY_SIZE / 2f,
                y - HIT_DISPLAY_SIZE / 2f,
                x + HIT_DISPLAY_SIZE / 2f,
                y + HIT_DISPLAY_SIZE / 2f,
            )
            canvas.drawBitmap(bmp, hitSrcRect, hitDstRect, null)
            return
        }
        val rotationDeg = if (vx == 0f && vy == 0f) {
            0f
        } else {
            Math.toDegrees(atan2(vy, vx).toDouble()).toFloat() - 90f
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
        private const val COLLISION_INSET_RATIO_X = 0.3f
        private const val COLLISION_INSET_RATIO_Y = 0.7f
        private const val HIT_DURATION = 0.4f
        private const val HIT_FRAME_COUNT = 8
        private const val HIT_FPS = 20f
        private const val HIT_DISPLAY_SIZE = 180f
        private var sharedHitBitmap: Bitmap? = null

        fun get(
            gctx: GameContext,
            x: Float,
            y: Float,
            vx: Float,
            vy: Float,
            type: Type,
        ): BossBullet {
            val scene = gctx.scene as? MainScene
                ?: return BossBullet(gctx).init(x, y, vx, vy, type)
            val bullet = scene.world.obtain(BossBullet::class.java) ?: BossBullet(gctx)
            return bullet.init(x, y, vx, vy, type)
        }
    }
}
