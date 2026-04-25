package com.example.smartphonetermproject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IRecyclable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.util.Gauge
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class Enemy private constructor(
    private val gctx: GameContext,
) : Sprite(gctx, Type.SUICIDE.resId), IBoxCollidable, IRecyclable {

    enum class Type(
        val resId: Int,
        val width: Float,
        val height: Float,
        val hp: Int,
        val speed: Float,
        val score: Int,
        val hitDamage: Int,
    ) {
        SUICIDE(R.mipmap.enemy_suicide, 180f, 180f, 1, 280f, 10, 2),
        RANGED(R.mipmap.enemy_ranged, 135f, 135f, 2, 150f, 20, 1),
        SPLIT(R.mipmap.enemy_split, 230f, 230f, 3, 220f, 30, 1),
        SPLIT_MINION(R.mipmap.enemy_split, 105f, 105f, 1, 240f, 10, 1),
    }

    private enum class RangedPhase { APPROACHING, ATTACKING }

    val score: Int get() = type.score
    val hitDamage: Int get() = type.hitDamage

    private lateinit var type: Type
    var life = 0
        private set
    var maxLife = 0
        private set
    private var speed = 0f
    val dead: Boolean get() = life <= 0

    override var width = 0f
    override var height = 0f
    override var x = 0f
    override var y = 0f
    override val collisionRect = RectF()

    private var diving = false
    private var diveVx = 0f
    private var diveVy = 0f

    private var rangedPhase = RangedPhase.APPROACHING
    private var rangedFireCooldown = 0f
    private var rangedStopRatio = 0f

    private var minionLockDelay = 0f
    private var minionLocked = false

    private var dying = false
    private var dyingTime = 0f
    private val dieRect = RectF()

    init {
        if (sharedGauge == null) {
            sharedGauge = Gauge(GAUGE_THICKNESS, GAUGE_FG_COLOR, GAUGE_BG_COLOR)
        }
        if (sharedDieBitmaps.isEmpty()) {
            for (t in Type.entries) {
                sharedDieBitmaps[t] = gctx.res.getBitmap(dieResIdFor(t))
            }
        }
    }

    fun init(
        x: Float,
        type: Type,
        startY: Float? = null,
        angleFromVerticalDeg: Float = 0f,
    ): Enemy {
        this.type = type
        this.bitmap = gctx.res.getBitmap(type.resId)
        this.width = type.width
        this.height = type.height
        this.life = type.hp
        this.maxLife = type.hp
        this.speed = type.speed
        this.x = x
        this.y = startY ?: -type.height / 2f
        diving = false
        diveVx = 0f
        diveVy = 0f
        rangedPhase = RangedPhase.APPROACHING
        rangedFireCooldown = 0f
        rangedStopRatio = RANGED_STOP_RATIO_MIN +
                Random.nextFloat() * (RANGED_STOP_RATIO_MAX - RANGED_STOP_RATIO_MIN)
        minionLockDelay = 0f
        minionLocked = false
        dying = false
        dyingTime = 0f
        if (type == Type.SPLIT_MINION) {
            val rad = Math.toRadians(angleFromVerticalDeg.toDouble())
            diveVx = sin(rad).toFloat() * speed
            diveVy = cos(rad).toFloat() * speed
            diving = true
            minionLockDelay = MINION_LOCK_DELAY
        }
        syncDstRect()
        updateCollisionRect()
        return this
    }

    override fun update(gctx: GameContext) {
        if (dying) {
            dyingTime -= gctx.frameTime
            if (dyingTime <= 0f) {
                val scene = gctx.scene as? MainScene ?: return
                scene.world.remove(this, MainScene.Layer.ENEMY)
            }
            return
        }
        when (type) {
            Type.SUICIDE, Type.SPLIT -> updateSuicide(gctx)
            Type.RANGED -> updateRanged(gctx)
            Type.SPLIT_MINION -> {
                moveByDiveVelocity(gctx)
                updateMinionLock(gctx)
            }
        }
        syncDstRect()
        updateCollisionRect()

        val outBottom = y - height / 2f > gctx.metrics.height
        val outRight = x - width / 2f > gctx.metrics.width
        val outLeft = x + width / 2f < 0f
        if (outBottom || outRight || outLeft) {
            val scene = gctx.scene as? MainScene ?: return
            scene.world.remove(this, MainScene.Layer.ENEMY)
        }
    }

    private fun updateCollisionRect() {
        if (dying) {
            collisionRect.setEmpty()
            return
        }
        val halfW = width * COLLISION_INSET_RATIO / 2f
        val halfH = height * COLLISION_INSET_RATIO / 2f
        collisionRect.set(x - halfW, y - halfH, x + halfW, y + halfH)
    }

    private fun updateSuicide(gctx: GameContext) {
        if (!diving) {
            y += speed * gctx.frameTime
            if (y >= gctx.metrics.height * SUICIDE_LOCK_RATIO) {
                lockDiveTarget(gctx)
            }
        } else {
            x += diveVx * gctx.frameTime
            y += diveVy * gctx.frameTime
        }
    }

    private fun lockDiveTarget(gctx: GameContext) {
        val player = (gctx.scene as? MainScene)?.player ?: return
        val dx = player.x - x
        val dy = player.y - y
        val len = hypot(dx, dy)
        if (len < 1f) return
        val diveSpeed = speed * SUICIDE_DIVE_MUL
        diveVx = dx / len * diveSpeed
        diveVy = dy / len * diveSpeed
        diving = true
    }

    private fun updateRanged(gctx: GameContext) {
        when (rangedPhase) {
            RangedPhase.APPROACHING -> {
                y += speed * gctx.frameTime
                if (y >= gctx.metrics.height * rangedStopRatio) {
                    rangedPhase = RangedPhase.ATTACKING
                    rangedFireCooldown = RANGED_FIRE_INTERVAL
                }
            }
            RangedPhase.ATTACKING -> {
                rangedFireCooldown -= gctx.frameTime
                if (rangedFireCooldown <= 0f) {
                    rangedFireCooldown = RANGED_FIRE_INTERVAL
                    fireRangedBullet(gctx)
                }
            }
        }
    }

    private fun fireRangedBullet(gctx: GameContext) {
        val scene = gctx.scene as? MainScene ?: return
        val muzzleX = x
        val muzzleY = y + height / 2f + ENEMY_BULLET_OFFSET
        val player = scene.player
        val dx = player.x - muzzleX
        val dy = player.y - muzzleY
        val len = hypot(dx, dy)
        val (vx, vy) = if (len < 1f) {
            0f to EnemyBullet.SPEED
        } else {
            (dx / len * EnemyBullet.SPEED) to (dy / len * EnemyBullet.SPEED)
        }
        val bullet = EnemyBullet.get(gctx, muzzleX, muzzleY, vx, vy)
        scene.world.add(bullet, MainScene.Layer.ENEMY_BULLET)
    }

    private fun moveByDiveVelocity(gctx: GameContext) {
        x += diveVx * gctx.frameTime
        y += diveVy * gctx.frameTime
    }

    private fun updateMinionLock(gctx: GameContext) {
        if (minionLocked) return
        minionLockDelay -= gctx.frameTime
        if (minionLockDelay <= 0f) {
            lockDiveTarget(gctx)
            minionLocked = true
        }
    }

    fun startDying(scene: MainScene) {
        if (dying) return
        dying = true
        dyingTime = DIE_DURATION
        updateCollisionRect()
        if (type == Type.SPLIT) {
            for (angleDeg in MINION_ANGLES) {
                val minion = get(
                    gctx,
                    x = x,
                    type = Type.SPLIT_MINION,
                    startY = y,
                    angleFromVerticalDeg = angleDeg,
                )
                scene.world.add(minion, MainScene.Layer.ENEMY)
            }
        }
    }

    private fun dieResIdFor(type: Type) = when (type) {
        Type.SUICIDE -> R.mipmap.vfx_suicide_die
        Type.RANGED -> R.mipmap.vfx_ranged_die
        Type.SPLIT -> R.mipmap.vfx_split_burst
        Type.SPLIT_MINION -> R.mipmap.vfx_minion_die
    }

    override fun draw(canvas: Canvas) {
        if (dying) {
            val bmp = sharedDieBitmaps[type] ?: return
            val size = width * DIE_SIZE_MUL
            dieRect.set(x - size / 2f, y - size / 2f, x + size / 2f, y + size / 2f)
            canvas.drawBitmap(bmp, null, dieRect, null)
            return
        }
        super.draw(canvas)
        val gauge = sharedGauge ?: return
        val gaugeWidth = width * 0.7f
        val gaugeX = x - gaugeWidth / 2f
        val gaugeY = y - height / 2f - GAUGE_OFFSET_FROM_TOP
        gauge.draw(canvas, gaugeX, gaugeY, gaugeWidth, life.toFloat() / maxLife)
    }

    fun decreaseLife(damage: Int) {
        life -= damage
    }

    override fun onRecycle() {}

    companion object {
        private var sharedGauge: Gauge? = null
        private const val GAUGE_THICKNESS = 0.12f
        private val GAUGE_FG_COLOR = Color.GREEN
        private val GAUGE_BG_COLOR = Color.argb(180, 0, 0, 0)
        private const val GAUGE_OFFSET_FROM_TOP = 8f
        private const val COLLISION_INSET_RATIO = 0.6f
        private const val SUICIDE_LOCK_RATIO = 0.4f
        private const val SUICIDE_DIVE_MUL = 1.6f
        private const val RANGED_STOP_RATIO_MIN = 0.22f
        private const val RANGED_STOP_RATIO_MAX = 0.35f
        private const val RANGED_FIRE_INTERVAL = 1.2f
        private const val ENEMY_BULLET_OFFSET = 8f
        private const val MINION_LOCK_DELAY = 0.3f
        private val MINION_ANGLES = listOf(-30f, 30f)
        private val sharedDieBitmaps = mutableMapOf<Type, Bitmap>()
        private const val DIE_DURATION = 0.1f
        private const val DIE_SIZE_MUL = 1.5f

        fun get(
            gctx: GameContext,
            x: Float,
            type: Type,
            startY: Float? = null,
            angleFromVerticalDeg: Float = 0f,
        ): Enemy {
            val scene = gctx.scene as? MainScene
                ?: return Enemy(gctx).init(x, type, startY, angleFromVerticalDeg)
            val enemy = scene.world.obtain(Enemy::class.java) ?: Enemy(gctx)
            return enemy.init(x, type, startY, angleFromVerticalDeg)
        }
    }
}
