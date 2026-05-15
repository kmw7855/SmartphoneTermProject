package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class BossClearLabel(
    gctx: GameContext,
    private val boss: Boss,
) : IGameObject {
    private val cx = gctx.metrics.width / 2f
    private val cy = gctx.metrics.height / 2f

    private val textPaint = Paint().apply {
        color = TEXT_COLOR
        textSize = TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = Color.BLACK
        textSize = TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    private val baselineY: Float = run {
        val fm = textPaint.fontMetrics
        cy - (fm.ascent + fm.descent) / 2f
    }

    override fun update(gctx: GameContext) {}

    override fun draw(canvas: Canvas) {
        if (!boss.dead) return
        canvas.drawText(LABEL, cx, baselineY, strokePaint)
        canvas.drawText(LABEL, cx, baselineY, textPaint)
    }

    companion object {
        private const val LABEL = "BOSS CLEAR!"
        private const val TEXT_SIZE = 160f
        private val TEXT_COLOR = Color.rgb(255, 220, 0)
    }
}
