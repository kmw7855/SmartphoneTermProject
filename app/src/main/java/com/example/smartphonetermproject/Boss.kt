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

    init {
        syncDstRect()
        updateCollisionRect()
    }

    override fun update(gctx: GameContext) {
        when (phase) {
            Phase.APPROACHING -> {
                y += APPROACH_SPEED * gctx.frameTime
                if (y >= stopY) {
                    y = stopY
                    phase = Phase.ATTACKING
                }
            }
            Phase.ATTACKING -> { }
        }
        syncDstRect()
        updateCollisionRect()
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
        private const val BOSS_WIDTH = 540f
        private const val BOSS_HEIGHT = 540f
        private const val MAX_LIFE = 5000
        private const val STOP_RATIO = 0.22f
        private const val APPROACH_SPEED = 180f
        private const val COLLISION_INSET_RATIO = 0.75f
    }
}
