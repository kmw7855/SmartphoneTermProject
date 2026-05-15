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
    private var lastPatternIndex = -1
    private val attackCycle: List<BossPattern> = listOf(
        BossPattern.CrownShard,
        BossPattern.EmperorScythe,
        BossPattern.AimedBurst,
        BossPattern.CorePulse,
    )
    private var currentPhase = 1
    private var currentPattern: BossPattern? = null
    private var burstTicksRemaining = 0
    private var burstSubCooldown = 0f
    private var burstTickIndex = 0

    private var phase2Triggered = false
    private var transitioning = false
    private var transitionRemaining = 0f
    private var pendingTransition = false
    private var spriteSwapped = false

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
        if (pendingTransition) {
            pendingTransition = false
            startTransition(gctx)
        }
        if (transitioning) {
            transitionRemaining -= gctx.frameTime
            if (!spriteSwapped && transitionRemaining <= TRANSITION_DURATION / 2f) {
                bitmap = gctx.res.getBitmap(R.mipmap.enemy_boss_2phase)
                currentPhase = 2
                spriteSwapped = true
            }
            if (transitionRemaining <= 0f) {
                endTransition(gctx)
            }
            syncDstRect()
            updateCollisionRect()
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

    private fun startTransition(gctx: GameContext) {
        phase2Triggered = true
        transitioning = true
        transitionRemaining = TRANSITION_DURATION
        spriteSwapped = false
        currentPattern = null
        burstTicksRemaining = 0
        burstSubCooldown = 0f
        val scene = gctx.scene as? MainScene ?: return
        scene.spawnVfx(
            R.mipmap.boss_phase_change,
            x, y,
            TRANSITION_VFX_SIZE,
            TRANSITION_FPS,
            TRANSITION_DURATION,
            frameCount = TRANSITION_FRAME_COUNT,
        )
    }

    private fun endTransition(gctx: GameContext) {
        transitioning = false
        attackCooldown = POST_TRANSITION_DELAY
        lastPatternIndex = -1
    }

    private fun updateAttack(gctx: GameContext) {
        val scene = gctx.scene as? MainScene ?: return

        if (burstTicksRemaining > 0) {
            burstSubCooldown -= gctx.frameTime
            if (burstSubCooldown > 0f) return
            val pattern = currentPattern ?: return
            pattern.fireTick(gctx, this, scene, burstTickIndex, currentPhase)
            burstTickIndex++
            burstTicksRemaining--
            burstSubCooldown = if (burstTicksRemaining > 0) pattern.burstInterval(currentPhase) else 0f
            if (burstTicksRemaining == 0) {
                attackCooldown = pattern.cooldown(currentPhase)
                currentPattern = null
            }
            return
        }

        attackCooldown -= gctx.frameTime
        if (attackCooldown > 0f) return
        val nextIndex = pickNextPatternIndex()
        val pattern = attackCycle[nextIndex]
        currentPattern = pattern
        burstTicksRemaining = pattern.burstCount(currentPhase)
        burstSubCooldown = 0f
        burstTickIndex = 0
        lastPatternIndex = nextIndex
    }

    private fun pickNextPatternIndex(): Int {
        if (attackCycle.size <= 1) return 0
        val candidates = attackCycle.indices.filter { it != lastPatternIndex }
        return candidates.random()
    }

    private fun updateCollisionRect() {
        val halfW = width * COLLISION_INSET_RATIO / 2f
        val halfH = height * COLLISION_INSET_RATIO / 2f
        collisionRect.set(x - halfW, y - halfH, x + halfW, y + halfH)
    }

    fun decreaseLife(damage: Int) {
        if (transitioning) return
        val previousLife = life
        life -= damage
        if (!phase2Triggered && previousLife > maxLife / 2 && life <= maxLife / 2 && life > 0) {
            pendingTransition = true
        }
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
        private const val TRANSITION_DURATION = 1.0f
        private const val TRANSITION_FPS = 8f
        private const val TRANSITION_FRAME_COUNT = 8
        private const val TRANSITION_VFX_SIZE = 1100f
        private const val POST_TRANSITION_DELAY = 0.6f
    }
}
