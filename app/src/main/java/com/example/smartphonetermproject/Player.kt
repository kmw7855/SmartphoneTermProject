package com.example.smartphonetermproject

import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.hypot
import kotlin.random.Random

class Player(val gctx: GameContext) : Sprite(gctx, R.mipmap.player_placeholder), IBoxCollidable {
    override var width = PLAYER_WIDTH
    override var height = PLAYER_HEIGHT
    override var x = gctx.metrics.width / 2f
    override var y = gctx.metrics.height - PLAYER_HEIGHT * 1.5f

    override val collisionRect = RectF()

    var life = MAX_LIFE
        private set
    val maxLife: Int
        get() = MAX_LIFE
    val dead: Boolean
        get() = life <= 0

    private val minX = PLAYER_WIDTH / 2f
    private val maxX = gctx.metrics.width - PLAYER_WIDTH / 2f
    private val minY = PLAYER_HEIGHT / 2f
    private val maxY = gctx.metrics.height - PLAYER_HEIGHT / 2f

    private var targetX = x
    private var targetY = y

    private var fireCooldown = 0f

    var exp = 0
        private set
    var level = 1
        private set
    var maxExp =  LEVEL_UP_EXP_BASE
        private set

    var attackMul: Float = 1f
    var fireRateMul: Float = 1f
    var critRate: Float = 0f

    var currentWeapon: Weapon = ShotgunWeapon
    var weaponGrade: WeaponGrade = WeaponGrade.RARE

    fun gainExp(amount: Int) {
        exp += amount
    }

    fun levelUp() {
        exp -= maxExp
        if (exp < 0) exp = 0
        level += 1
        maxExp = (maxExp * 1.5f).toInt()
    }

    fun calculatePower(): Pair<Int, Boolean> {
        val basePower = (Bullet.DAMAGE * attackMul).toInt().coerceAtLeast(1)
        val isCrit = Random.nextFloat() < critRate
        val power = if (isCrit) basePower * CRIT_MUL else basePower
        return power to isCrit
    }

    init {
        syncDstRect()
        updateCollisionRect()
    }

    override fun update(gctx: GameContext) {
        val step = SPEED * gctx.frameTime
        val dx = targetX - x
        val dy = targetY - y
        val dist = hypot(dx, dy)
        if (dist <= step || dist < 0.5f) {
            x = targetX
            y = targetY
        } else {
            x += dx / dist * step
            y += dy / dist * step
        }
        x = x.coerceIn(minX, maxX)
        y = y.coerceIn(minY, maxY)
        syncDstRect()
        updateCollisionRect()
        fireBullet(gctx)
    }

    private fun updateCollisionRect() {
        val halfW = width * COLLISION_INSET_RATIO / 2f
        val halfH = height * COLLISION_INSET_RATIO / 2f
        collisionRect.set(x - halfW, y - halfH, x + halfW, y + halfH)
    }

    private fun fireBullet(gctx: GameContext) {
        fireCooldown -= gctx.frameTime
        if (fireCooldown > 0f) return
        fireCooldown = currentWeapon.fireInterval / fireRateMul
        val scene = gctx.scene as? MainScene ?: return
        currentWeapon.fire(this, scene, gctx, weaponGrade)
    }

    fun decreaseLife(damage: Int) {
        life -= damage
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val pt = gctx.metrics.fromScreen(event.x, event.y)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                targetX = pt.x.coerceIn(minX, maxX)
                targetY = pt.y.coerceIn(minY, maxY)
            }
        }
        return true
    }

    companion object {
        const val SPEED = 1100f
        const val PLAYER_WIDTH = 200f
        const val PLAYER_HEIGHT = 200f
        const val MAX_LIFE = 10
        private const val COLLISION_INSET_RATIO = 0.6f
        const val BULLET_OFFSET = 8f
        private const val LEVEL_UP_EXP_BASE = 3
        private const val CRIT_MUL = 3
    }
}