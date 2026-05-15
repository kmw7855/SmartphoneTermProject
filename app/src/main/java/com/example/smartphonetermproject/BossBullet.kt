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

    private enum class Move { STRAIGHT, QUAD_BEZIER }

    override var width = 0f
    override var height = 0f
    override var x = 0f
    override var y = 0f

    private var move = Move.STRAIGHT
    private var vx = 0f
    private var vy = 0f

    private var bezDuration = 0f
    private var bezElapsed = 0f
    private var bezP0X = 0f
    private var bezP0Y = 0f
    private var bezP1X = 0f
    private var bezP1Y = 0f
    private var bezP2X = 0f
    private var bezP2Y = 0f
    private var prevX = 0f
    private var prevY = 0f
    private var spinDegPerSec = 0f

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

    private fun applyType(type: Type) {
        this.type = type
        this.bitmap = gctx.res.getBitmap(type.resId)
        this.width = type.width
        this.height = type.height
    }

    fun init(startX: Float, startY: Float, vx: Float, vy: Float, type: Type): BossBullet {
        move = Move.STRAIGHT
        x = startX
        y = startY
        this.vx = vx
        this.vy = vy
        prevX = startX
        prevY = startY
        spinDegPerSec = 0f
        applyType(type)
        hitting = false
        hitTime = 0f
        syncDstRect()
        return this
    }

    fun initBezier(
        startX: Float, startY: Float,
        ctrlX: Float, ctrlY: Float,
        endX: Float, endY: Float,
        durationSec: Float,
        type: Type,
        spinDegPerSec: Float = 0f,
    ): BossBullet {
        move = Move.QUAD_BEZIER
        bezP0X = startX; bezP0Y = startY
        bezP1X = ctrlX;  bezP1Y = ctrlY
        bezP2X = endX;   bezP2Y = endY
        bezDuration = durationSec
        bezElapsed = 0f
        x = startX
        y = startY
        prevX = startX
        prevY = startY
        this.spinDegPerSec = spinDegPerSec
        applyType(type)
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
        when (move) {
            Move.STRAIGHT -> {
                prevX = x; prevY = y
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
            Move.QUAD_BEZIER -> {
                prevX = x; prevY = y
                bezElapsed += gctx.frameTime
                val t = (bezElapsed / bezDuration).coerceIn(0f, 1f)
                val mt = 1f - t
                x = mt * mt * bezP0X + 2f * mt * t * bezP1X + t * t * bezP2X
                y = mt * mt * bezP0Y + 2f * mt * t * bezP1Y + t * t * bezP2Y
                syncDstRect()
                if (bezElapsed >= bezDuration) {
                    val scene = gctx.scene as? MainScene ?: return
                    scene.world.remove(this, MainScene.Layer.ENEMY_BULLET)
                    return
                }
            }
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
        val rotationDeg = if (spinDegPerSec != 0f) {
            spinDegPerSec * bezElapsed
        } else {
            val dx = x - prevX
            val dy = y - prevY
            if (dx == 0f && dy == 0f) {
                if (move == Move.STRAIGHT && (vx != 0f || vy != 0f)) {
                    Math.toDegrees(atan2(vy, vx).toDouble()).toFloat() - 90f
                } else 0f
            } else {
                Math.toDegrees(atan2(dy, dx).toDouble()).toFloat() - 90f
            }
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

        fun getBezier(
            gctx: GameContext,
            startX: Float, startY: Float,
            ctrlX: Float, ctrlY: Float,
            endX: Float, endY: Float,
            durationSec: Float,
            type: Type,
            spinDegPerSec: Float = 0f,
        ): BossBullet {
            val scene = gctx.scene as? MainScene
                ?: return BossBullet(gctx).initBezier(startX, startY, ctrlX, ctrlY, endX, endY, durationSec, type, spinDegPerSec)
            val bullet = scene.world.obtain(BossBullet::class.java) ?: BossBullet(gctx)
            return bullet.initBezier(startX, startY, ctrlX, ctrlY, endX, endY, durationSec, type, spinDegPerSec)
        }
    }
}
