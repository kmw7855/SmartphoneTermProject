package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.DrawableSprite
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.util.LabelUtil
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class PauseScene(
    gctx: GameContext,
    private val source: MainScene,
) : Scene(gctx) {

    enum class Layer { BG, CONTENT, TOUCH }

    override val isTransparent = true
    override val clipsRect = true

    private val cx = gctx.metrics.width / 2f
    private val cy = gctx.metrics.height / 2f
    private val panelTop = cy - PANEL_HEIGHT / 2f

    override val world = World(Layer.entries.toTypedArray()).apply {
        add(
            DrawableSprite(ColorDrawable(Color.argb(170, 0, 0, 0))).apply {
                setCenter(cx, cy)
                setSize(gctx.metrics.width, gctx.metrics.height)
            },
            Layer.BG,
        )
        add(
            DrawableSprite(gctx.res.getDrawable(R.drawable.pause_panel)).apply {
                setCenter(cx, cy)
                setSize(PANEL_WIDTH, PANEL_HEIGHT)
            },
            Layer.BG,
        )
        add(PauseContent(), Layer.CONTENT)
        add(
            RoundRectButton(
                gctx,
                cx,
                panelTop + RESUME_OFFSET_FROM_TOP,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "계속하기",
                fillColor = Color.rgb(40, 90, 160),
                strokeColor = Color.rgb(120, 180, 255),
            ) { pop() },
            Layer.TOUCH,
        )
        add(
            RoundRectButton(
                gctx,
                cx,
                panelTop + EXIT_OFFSET_FROM_TOP,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "나가기",
                fillColor = Color.rgb(120, 40, 50),
                strokeColor = Color.rgb(255, 120, 120),
            ) { gctx.sceneStack.popAll() },
            Layer.TOUCH,
        )
    }

    override fun touchObjects(): List<IGameObject> {
        return world.objectsAt(Layer.TOUCH)
    }

    private inner class PauseContent : IGameObject {
        private val titleLabel = LabelUtil(TITLE_TEXT_SIZE, Color.WHITE, Paint.Align.CENTER, Typeface.DEFAULT_BOLD)
        private val scoreLabel = LabelUtil(SCORE_TEXT_SIZE, Color.WHITE, Paint.Align.CENTER, Typeface.DEFAULT_BOLD)
        private val loadout = LoadoutCardsView(gctx)
        private val titleY = panelTop + TITLE_OFFSET_FROM_TOP
        private val scoreY = panelTop + SCORE_OFFSET_FROM_TOP
        private val cardsTopY = panelTop + CARDS_OFFSET_FROM_TOP

        override fun update(gctx: GameContext) {}

        override fun draw(canvas: Canvas) {
            titleLabel.draw(canvas, "일시정지", cx, titleY)
            scoreLabel.draw(canvas, "SCORE  ${source.score}", cx, scoreY)
            loadout.draw(canvas, source.player, cx, cardsTopY)
        }
    }

    companion object {
        private const val PANEL_WIDTH = 840f
        private const val PANEL_HEIGHT = 860f
        private const val TITLE_TEXT_SIZE = 88f
        private const val SCORE_TEXT_SIZE = 60f
        private const val TITLE_OFFSET_FROM_TOP = 110f
        private const val SCORE_OFFSET_FROM_TOP = 210f
        private const val CARDS_OFFSET_FROM_TOP = 270f
        private const val RESUME_OFFSET_FROM_TOP = 590f
        private const val EXIT_OFFSET_FROM_TOP = 740f
        private const val BUTTON_WIDTH = 380f
        private const val BUTTON_HEIGHT = 130f
    }
}
