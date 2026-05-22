package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class BossClearLabel(
    private val gctx: GameContext,
    private val boss: Boss,
    private val scene: MainScene,
) : IGameObject {
    private val cx = gctx.metrics.width / 2f
    private val cy = gctx.metrics.height / 2f

    private val panelRect = RectF(
        cx - PANEL_WIDTH / 2f,
        cy - PANEL_HEIGHT / 2f,
        cx + PANEL_WIDTH / 2f,
        cy + PANEL_HEIGHT / 2f,
    )

    private val panelBgPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.argb(220, 18, 24, 38)
        isAntiAlias = true
    }
    private val panelStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.rgb(255, 200, 80)
        isAntiAlias = true
    }
    private val titlePaint = Paint().apply {
        color = Color.rgb(255, 220, 0)
        textSize = TITLE_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    private val titleStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = Color.BLACK
        textSize = TITLE_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = SCORE_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    private val loadout = LoadoutCardsView(gctx)

    override fun update(gctx: GameContext) {}

    override fun draw(canvas: Canvas) {
        if (!boss.dead) return

        canvas.drawRoundRect(panelRect, CORNER_RADIUS, CORNER_RADIUS, panelBgPaint)
        canvas.drawRoundRect(panelRect, CORNER_RADIUS, CORNER_RADIUS, panelStrokePaint)

        var lineY = panelRect.top + TITLE_OFFSET_FROM_TOP
        canvas.drawText(TITLE_LABEL, cx, lineY, titleStrokePaint)
        canvas.drawText(TITLE_LABEL, cx, lineY, titlePaint)

        lineY += TITLE_TO_SCORE_GAP
        canvas.drawText("SCORE  ${scene.score}", cx, lineY, scorePaint)

        loadout.draw(canvas, scene.player, cx, panelRect.top + CARD_ROW_TOP_OFFSET)
    }

    companion object {
        private const val PANEL_WIDTH = 820f
        private const val PANEL_HEIGHT = 620f
        private const val CORNER_RADIUS = 28f
        private const val TITLE_LABEL = "BOSS CLEAR!"
        private const val TITLE_TEXT_SIZE = 130f
        private const val SCORE_TEXT_SIZE = 72f
        private const val TITLE_OFFSET_FROM_TOP = 140f
        private const val TITLE_TO_SCORE_GAP = 110f
        private const val CARD_ROW_TOP_OFFSET = 320f
    }
}
