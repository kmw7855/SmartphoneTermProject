package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class LevelUpScene(
    gctx: GameContext,
    private val mainScene: MainScene,
) : Scene(gctx) {

    private val backgroundPaint = Paint().apply { style = Paint.Style.FILL; color = Color.argb(160, 0, 0, 0) }
    private val cardFillPaint = Paint().apply { style = Paint.Style.FILL; color = Color.rgb(28, 36, 56); isAntiAlias = true }
    private val cardStrokePaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 4f; color = Color.rgb(34, 211, 238); isAntiAlias = true }
    private val titlePaint = Paint().apply {
        color = Color.rgb(34, 211, 238); textSize = TITLE_TEXT_SIZE; textAlign = Paint.Align.CENTER
        isAntiAlias = true; isFakeBoldText = true
    }
    private val cardTextPaint = Paint().apply { color = Color.WHITE; textSize = CARD_TEXT_SIZE; textAlign = Paint.Align.CENTER; isAntiAlias = true }

    private val titleX = gctx.metrics.width / 2f
    private val titleY = gctx.metrics.height * 0.30f

    private val cardRects: List<RectF> = run {
        val totalWidth = CARD_WIDTH * 3 + CARD_GAP * 2
        val startX = (gctx.metrics.width - totalWidth) / 2f
        val cardY = gctx.metrics.height * 0.42f
        (0..2).map { i ->
            val left = startX + i * (CARD_WIDTH + CARD_GAP)
            RectF(left, cardY, left + CARD_WIDTH, cardY + CARD_HEIGHT)
        }
    }

    private val cardLabels = listOf(
        "공격력" to "x2",
        "공속" to "+30%",
        "치명타" to "+50%",
    )

    override fun update(gctx: GameContext) {}

    override fun draw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, gctx.metrics.width, gctx.metrics.height, backgroundPaint)
        canvas.drawText("Level Up!", titleX, titleY, titlePaint)
        for ((i, rect) in cardRects.withIndex()) {
            canvas.drawRoundRect(rect, CARD_CORNER, CARD_CORNER, cardFillPaint)
            canvas.drawRoundRect(rect, CARD_CORNER, CARD_CORNER, cardStrokePaint)
            val (title, effect) = cardLabels[i]
            canvas.drawText(title, rect.centerX(), rect.centerY() - CARD_TEXT_SIZE * 0.3f, cardTextPaint)
            canvas.drawText(effect, rect.centerX(), rect.centerY() + CARD_TEXT_SIZE * 0.9f, cardTextPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_UP) return true
        val pt = gctx.metrics.fromScreen(event.x, event.y)
        for ((i, rect) in cardRects.withIndex()) {
            if (rect.contains(pt.x, pt.y)) {
                onCardSelected(i)
                return true
            }
        }
        return true
    }

    private fun onCardSelected(idx: Int) {
        val player = mainScene.player
        when (idx) {
            0 -> player.attackMul *= ATK_BOOST
            1 -> player.fireRateMul *= RATE_BOOST
            2 -> player.critRate = (player.critRate + CRIT_BOOST).coerceAtMost(1f)
        }
        player.levelUp()
        pop()
    }

    companion object {
        private const val TITLE_TEXT_SIZE = 90f
        private const val CARD_WIDTH = 230f
        private const val CARD_HEIGHT = 320f
        private const val CARD_GAP = 30f
        private const val CARD_CORNER = 24f
        private const val CARD_TEXT_SIZE = 56f

        private const val ATK_BOOST = 2.0f
        private const val RATE_BOOST = 1.3f
        private const val CRIT_BOOST = 0.5f
    }
}