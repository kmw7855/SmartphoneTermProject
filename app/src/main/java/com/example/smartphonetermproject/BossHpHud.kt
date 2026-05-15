package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.util.Gauge
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class BossHpHud(
    gctx: GameContext,
    private val boss: Boss,
) : IGameObject {
    private val barX = gctx.metrics.width / 2f - BAR_WIDTH / 2f
    private val gauge = Gauge(GAUGE_THICKNESS, FG_COLOR, BG_COLOR)

    private val labelPaint = Paint().apply {
        color = LABEL_COLOR
        textSize = LABEL_TEXT_SIZE
        textAlign = Paint.Align.LEFT
        isAntiAlias = true
        isFakeBoldText = true
    }

    override fun update(gctx: GameContext) {}

    override fun draw(canvas: Canvas) {
        if (boss.dead) return
        canvas.drawText(LABEL, barX, BAR_Y - LABEL_OFFSET, labelPaint)
        val ratio = (boss.life.toFloat() / boss.maxLife).coerceIn(0f, 1f)
        gauge.draw(canvas, barX, BAR_Y, BAR_WIDTH, ratio)
    }

    companion object {
        private const val BAR_WIDTH = 900f
        private const val BAR_Y = 175f
        private const val GAUGE_THICKNESS = 0.04f
        private val FG_COLOR = Color.rgb(220, 50, 80)
        private val BG_COLOR = Color.argb(180, 0, 0, 0)
        private const val LABEL = "BOSS"
        private const val LABEL_TEXT_SIZE = 42f
        private const val LABEL_OFFSET = 14f
        private val LABEL_COLOR = Color.rgb(255, 160, 180)
    }
}
