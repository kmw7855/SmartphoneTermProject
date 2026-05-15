package com.example.smartphonetermproject

import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class Boss(
    gctx: GameContext,
) : Sprite(gctx, R.mipmap.enemy_boss), IBoxCollidable {

    enum class Phase { APPROACHING, ATTACKING }

    override var width = BOSS_WIDTH
    override var height = BOSS_HEIGHT
    override var x = gctx.metrics.width / 2f
    override var y = -BOSS_HEIGHT / 2f

    override val collisionRect = RectF()

    var life = MAX_LIFE
        private set
    val maxLife = MAX_LIFE
    val dead: Boolean get() = life <= 0

    var phase: Phase = Phase.APPROACHING
        private set
    private val stopY = gctx.metrics.height * STOP_RATIO

    private var attackCooldown = 0f
    private var patternIndex = 0
    private val attackCycle: List<BossPattern> = listOf(BossPattern.CrownShard)

    init {
        syncDstRect()
        updateCollisionRect()
    }

    override fun update(gctx: GameContext) {
        if (dead) {
            val scene = gctx.scene as? MainScene ?: return
            scene.world.remove(this, MainScene.Layer.ENEMY)
            return
        }
        when (phase) {
            Phase.APPROACHING -> {
                y += APPROACH_SPEED * gctx.frameTime
                if (y >= stopY) {
                    y = stopY
                    phase = Phase.ATTACKING
                    attackCooldown = INITIAL_ATTACK_DELAY
                }
            }
            Phase.ATTACKING -> updateAttack(gctx)
        }
        syncDstRect()
        updateCollisionRect()
    }

    private fun updateAttack(gctx: GameContext) {
        attackCooldown -= gctx.frameTime
        if (attackCooldown > 0f) return
        val scene = gctx.scene as? MainScene ?: return
        val pattern = attackCycle[patternIndex % attackCycle.size]
        pattern.fire(gctx, this, scene)
        attackCooldown = pattern.cooldown
        patternIndex++
    }

    private fun updateCollisionRect() {
        val halfW = width * COLLISION_INSET_RATIO / 2f
        val halfH = height * COLLISION_INSET_RATIO / 2f
        collisionRect.set(x - halfW, y - halfH, x + halfW, y + halfH)
    }

    fun decreaseLife(damage: Int) {
        life -= damage
    }

    companion object {
        const val HIT_DAMAGE = 5
        private const val BOSS_WIDTH = 540f
        private const val BOSS_HEIGHT = 540f
        private const val MAX_LIFE = 300
        private const val STOP_RATIO = 0.22f
        private const val APPROACH_SPEED = 180f
        private const val COLLISION_INSET_RATIO = 0.75f
        private const val INITIAL_ATTACK_DELAY = 0.8f
    }
}
