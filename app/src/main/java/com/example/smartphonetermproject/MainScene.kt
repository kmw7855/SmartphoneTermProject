package com.example.smartphonetermproject

import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.VertScrollBackground
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import android.view.MotionEvent

open class MainScene(
    gctx: GameContext,
    backgroundResId: Int = R.mipmap.sky_bg,
    val isBossStage: Boolean = false,
) : Scene(gctx) {
    enum class Layer {
        BACKGROUND,
        PLAYER,
        BULLET,
        ENEMY,
        ENEMY_BULLET,
        LASER,
        CONTROLLER,
        STARS,
        EXP_ORB,
        VFX,
        UI,
        SKILL_BUTTON,
    }

    private val background = VertScrollBackground(gctx, backgroundResId, BACKGROUND_SPEED)
    private val stars = VertScrollBackground(gctx, R.mipmap.sky_star, STARS_SPEED)
    val player = Player(gctx)
    private val bossTimerHud = BossTimerHud(gctx)

    var elapsedSec = 0f
        private set
    private var bossPromptPending = false
    private var nextPromptAt = BOSS_ENTER_TIME

    private val enemyGenerator = EnemyGenerator(gctx)
    private val collisionChecker = CollisionChecker(gctx)

    private val scoreLabel = ScoreLabel(gctx)
    private val playerHpHud = PlayerHpHud(gctx)

    var score = 0
        private set
    fun addScore(amount: Int) {
        score += amount
    }

    fun spawnDamagePopup(x: Float, y: Float, power: Int, isCrit: Boolean) {
        collisionChecker.spawnPopup(x, y, power, isCrit)
    }

    private val expLabel = ExpLabel(gctx)
    private val debugStatLabel = DebugStatLabel(gctx)

    val cardPool = CardPool()

    private val skillButton = SkillButton(
        gctx,
        centerX = gctx.metrics.width - SKILL_BUTTON_MARGIN_RIGHT,
        centerY = gctx.metrics.height - SKILL_BUTTON_MARGIN_BOTTOM,
        radius = SKILL_BUTTON_RADIUS,
    )

    override val world = World(Layer.entries.toTypedArray()).apply {
        add(background, Layer.BACKGROUND)
        add(player, Layer.PLAYER)
        add(stars, Layer.STARS)
        if (!isBossStage) add(enemyGenerator, Layer.CONTROLLER)
        add(scoreLabel, Layer.UI)
        add(bossTimerHud, Layer.UI)
        add(collisionChecker, Layer.UI)
        add(playerHpHud, Layer.UI)
        add(expLabel, Layer.UI)
        add(debugStatLabel, Layer.UI)
        add(skillButton, Layer.SKILL_BUTTON)
    }

    override fun update(gctx: GameContext) {
        super.update(gctx)

        if (player.exp >= player.maxExp) {
            LevelUpScene(gctx, this, cardPool.pickThree(player.currentSkill)).push()
            return
        }

        if (isBossStage) return
        if (bossPromptPending) return
        elapsedSec += gctx.frameTime
        if (elapsedSec >= nextPromptAt) {
            bossPromptPending = true
            BossEntryScene(gctx, this).push()
        }
    }

    fun dismissBossPrompt() {
        bossPromptPending = false
        nextPromptAt += BOSS_ENTER_TIME
    }

    fun spawnVfx(
        resId: Int,
        x: Float,
        y: Float,
        size: Float,
        fps: Float,
        duration: Float,
        frameCount: Int = 0,
        loops: Boolean = false,
        followTarget: Player? = null,
    ) {
        world.add(
            SkillVfx.spawn(gctx, resId, x, y, size, fps, duration, frameCount, loops, followTarget),
            Layer.VFX,
        )
    }

    fun applyAreaDamage(
        centerX: Float,
        centerY: Float,
        radius: Float,
        damage: Int,
        onHit: ((hitX: Float, hitY: Float) -> Unit)? = null,
    ) {
        val r2 = radius * radius
        world.forEachReversedAt(Layer.ENEMY) { obj ->
            val enemy = obj as? Enemy ?: return@forEachReversedAt
            val dx = enemy.x - centerX
            val dy = enemy.y - centerY
            if (dx * dx + dy * dy > r2) return@forEachReversedAt
            enemy.decreaseLife(damage)
            spawnDamagePopup(enemy.x, enemy.y, damage, true)
            if (enemy.dead) {
                enemy.startDying(this)
                addScore(enemy.score)
            }
            onHit?.invoke(enemy.x, enemy.y)
        }
    }

    override fun touchObjects(): List<IGameObject> {
        return world.objectsAt(Layer.SKILL_BUTTON)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (super.onTouchEvent(event)) return true
        return player.onTouchEvent(event)
    }

    override val clipsRect = true

    companion object {
        private const val BACKGROUND_SPEED = 80f
        private const val BOSS_ENTER_TIME = 15f
        private const val STARS_SPEED = 150f
        private const val SKILL_BUTTON_RADIUS = 80f
        private const val SKILL_BUTTON_MARGIN_RIGHT = 110f
        private const val SKILL_BUTTON_MARGIN_BOTTOM = 200f
    }
}
