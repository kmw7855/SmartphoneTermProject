package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.ITouchable
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class RoundRectButton(
    private val gctx: GameContext,
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    private val text: String,
    fillColor: Int,
    strokeColor: Int = Color.WHITE,
    private val onTap: () -> Unit,
) : IGameObject, ITouchable {

    private val rectF = RectF(
        centerX - width / 2f, centerY - height / 2f,
        centerX + width / 2f, centerY + height / 2f,
    )

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL; color = fillColor; isAntiAlias = true
    }
    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE; strokeWidth = 4f; color = strokeColor; isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    private val textCenterY = run {
        val fm = textPaint.fontMetrics
        rectF.centerY() - (fm.ascent + fm.descent) / 2f
    }

    override fun update(gctx: GameContext) {}

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(rectF, CORNER, CORNER, fillPaint)
        canvas.drawRoundRect(rectF, CORNER, CORNER, strokePaint)
        canvas.drawText(text, rectF.centerX(), textCenterY, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val pt = gctx.metrics.fromScreen(event.x, event.y)
                if (!rectF.contains(pt.x, pt.y)) return false
                onTap()
                true
            }
            else -> true
        }
    }

    companion object {
        private const val CORNER = 24f
        private const val TEXT_SIZE = 56f
    }
}
