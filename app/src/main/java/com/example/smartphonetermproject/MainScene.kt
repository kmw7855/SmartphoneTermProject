package com.example.smartphonetermproject

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
        UI,
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
    }

    override fun update(gctx: GameContext) {
        super.update(gctx)

        if (player.exp >= player.maxExp) {
            LevelUpScene(gctx, this, cardPool.pickThree()).push()
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return player.onTouchEvent(event)
    }

    override val clipsRect = true

    companion object {
        private const val BACKGROUND_SPEED = 80f
        private const val BOSS_ENTER_TIME = 15f
        private const val STARS_SPEED = 150f
    }
}
