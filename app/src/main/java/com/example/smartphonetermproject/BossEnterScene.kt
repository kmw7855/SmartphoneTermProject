package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class BossEntryScene(
    gctx: GameContext,
    private val mainScene: MainScene,
) : Scene(gctx) {

    enum class Layer { TOUCH }

    override val isTransparent = true
    override val clipsRect = true

    private val dimPaint = Paint().apply {
        style = Paint.Style.FILL; color = Color.argb(170, 0, 0, 0)
    }
    private val titlePaint = Paint().apply {
        color = Color.rgb(255, 170, 80)
        textSize = TITLE_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    private val subtitlePaint = Paint().apply {
        color = Color.WHITE
        textSize = SUBTITLE_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val titleX = gctx.metrics.width / 2f
    private val titleY = gctx.metrics.height * 0.32f
    private val subtitleY = titleY + SUBTITLE_OFFSET

    override val world = World(Layer.entries.toTypedArray()).apply {
        val cx = gctx.metrics.width / 2f
        val buttonY = gctx.metrics.height * 0.55f

        add(
            RoundRectButton(
                gctx,
                cx - BUTTON_WIDTH / 2f - BUTTON_GAP / 2f,
                buttonY,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "보스 도전",
                fillColor = Color.rgb(180, 40, 70),
                strokeColor = Color.rgb(255, 120, 140),
            ) { onChooseBoss() },
            Layer.TOUCH,
        )
        add(
            RoundRectButton(
                gctx,
                cx + BUTTON_WIDTH / 2f + BUTTON_GAP / 2f,
                buttonY,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "계속 사냥",
                fillColor = Color.rgb(40, 90, 160),
                strokeColor = Color.rgb(120, 180, 255),
            ) { onChooseStay() },
            Layer.TOUCH,
        )
    }

    override fun touchObjects(): List<IGameObject> {
        return world.objectsAt(Layer.TOUCH)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, gctx.metrics.width, gctx.metrics.height, dimPaint)
        canvas.drawText("WARNING", titleX, titleY, titlePaint)
        canvas.drawText("보스 스테이지로 진입하시겠습니까?", titleX, subtitleY, subtitlePaint)
        super.draw(canvas)
    }

    private fun onChooseBoss() {
        pop()
        BossScene(gctx).change()
    }

    private fun onChooseStay() {
        mainScene.dismissBossPrompt()
        pop()
    }

    companion object {
        private const val TITLE_TEXT_SIZE = 130f
        private const val SUBTITLE_TEXT_SIZE = 56f
        private const val SUBTITLE_OFFSET = 100f
        private const val BUTTON_WIDTH = 320f
        private const val BUTTON_HEIGHT = 130f
        private const val BUTTON_GAP = 60f
    }
}
