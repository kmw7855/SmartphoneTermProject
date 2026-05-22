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
    private val bgmResId: Int = R.raw.normalstage,
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
        PAUSE_BUTTON,
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

    private val pauseButton = PauseButton(gctx) { PauseScene(gctx, this@MainScene).push() }

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
        add(pauseButton, Layer.PAUSE_BUTTON)
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

    fun spawnRateMul(): Float = (1f + elapsedSec / 10f).coerceAtMost(SPAWN_RATE_MAX)
    fun enemyStatMul(): Float = (1f + elapsedSec / 15f).coerceAtMost(ENEMY_STAT_MAX)
    fun expPerOrb(): Int = (elapsedSec / EXP_STEP_SEC).toInt() + 1

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
            when (obj) {
                is Enemy -> {
                    val dx = obj.x - centerX
                    val dy = obj.y - centerY
                    if (dx * dx + dy * dy > r2) return@forEachReversedAt
                    obj.decreaseLife(damage)
                    spawnDamagePopup(obj.x, obj.y, damage, true)
                    if (obj.dead) {
                        obj.startDying(this)
                        addScore(obj.score)
                    }
                    onHit?.invoke(obj.x, obj.y)
                }
                is Boss -> {
                    if (obj.dead) return@forEachReversedAt
                    val dx = obj.x - centerX
                    val dy = obj.y - centerY
                    if (dx * dx + dy * dy > r2) return@forEachReversedAt
                    obj.decreaseLife(damage)
                    spawnDamagePopup(obj.x, obj.y, damage, true)
                    onHit?.invoke(obj.x, obj.y)
                }
            }
        }
    }

    override fun touchObjects(): List<IGameObject> {
        return world.objectsAt(Layer.SKILL_BUTTON) + world.objectsAt(Layer.PAUSE_BUTTON)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (super.onTouchEvent(event)) return true
        return player.onTouchEvent(event)
    }

    override fun onBackPressed(): Boolean {
        PauseScene(gctx, this).push()
        return true
    }

    override fun onEnter() {
        gctx.res.sound.playMusic(bgmResId)
    }

    override fun onExit() {
        gctx.res.sound.stopMusic()
        Sfx.stopBuffLoop()
    }

    override fun onPause() {
        gctx.res.sound.pauseMusic()
        Sfx.pauseBuffLoop()
    }

    override fun onResume() {
        gctx.res.sound.resumeMusic()
        Sfx.resumeBuffLoop()
    }

    override val clipsRect = true

    companion object {
        private const val BACKGROUND_SPEED = 80f
        private const val BOSS_ENTER_TIME = 15f
        private const val STARS_SPEED = 150f
        private const val SKILL_BUTTON_RADIUS = 60f
        private const val SKILL_BUTTON_MARGIN_RIGHT = 90f
        private const val SKILL_BUTTON_MARGIN_BOTTOM = 180f

        private const val SPAWN_RATE_MAX = 5f
        private const val ENEMY_STAT_MAX = 4f
        private const val EXP_STEP_SEC = 5f
    }
}
