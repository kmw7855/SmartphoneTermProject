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
        CONTROLLER,
        STARS,
        UI,
    }

    private val background = VertScrollBackground(gctx, backgroundResId, BACKGROUND_SPEED)
    private val stars = VertScrollBackground(gctx, R.mipmap.sky_star, STARS_SPEED)
    val player = Player(gctx)
    private val bossTimerHud = BossTimerHud(gctx)

    var elapsedSec = 0f
        private set
    private var bossEntered = isBossStage

    private val enemyGenerator = EnemyGenerator(gctx)
    private val collisionChecker = CollisionChecker(gctx)

    private val scoreLabel = ScoreLabel(gctx)

    var score = 0
        private set
    fun addScore(amount: Int) {
        score += amount
    }

    override val world = World(Layer.entries.toTypedArray()).apply {
        add(background, Layer.BACKGROUND)
        add(player, Layer.PLAYER)
        add(stars, Layer.STARS)
        if (!isBossStage) add(enemyGenerator, Layer.CONTROLLER)
        add(scoreLabel, Layer.UI)
        add(bossTimerHud, Layer.UI)
        add(collisionChecker, Layer.CONTROLLER)
    }

    override fun update(gctx: GameContext) {
        super.update(gctx)
        if (bossEntered) return
        elapsedSec += gctx.frameTime
        if (elapsedSec >= BOSS_ENTER_TIME) {
            bossEntered = true
            BossScene(gctx).change()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return player.onTouchEvent(event)
    }

    override val clipsRect = true
    
    companion object {
        private const val BACKGROUND_SPEED = 80f
        private const val BOSS_ENTER_TIME = 10f
        private const val STARS_SPEED = 150f
    }
}