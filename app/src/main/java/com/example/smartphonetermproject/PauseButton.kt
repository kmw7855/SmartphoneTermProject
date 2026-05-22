package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.ITouchable
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class PauseButton(
    private val gctx: GameContext,
    private val onTap: () -> Unit,
) : IGameObject, ITouchable {
    private val centerX = gctx.metrics.width - MARGIN_RIGHT
    private val centerY = MARGIN_TOP

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.argb(160, 0, 0, 0)
        isAntiAlias = true
    }
    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.rgb(220, 230, 245)
        isAntiAlias = true
    }
    private val barPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.rgb(220, 230, 245)
        isAntiAlias = true
    }

    private val leftBar = RectF(
        centerX - BAR_GAP / 2f - BAR_WIDTH,
        centerY - BAR_HEIGHT / 2f,
        centerX - BAR_GAP / 2f,
        centerY + BAR_HEIGHT / 2f,
    )
    private val rightBar = RectF(
        centerX + BAR_GAP / 2f,
        centerY - BAR_HEIGHT / 2f,
        centerX + BAR_GAP / 2f + BAR_WIDTH,
        centerY + BAR_HEIGHT / 2f,
    )

    override fun update(gctx: GameContext) {}

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, RADIUS, fillPaint)
        canvas.drawCircle(centerX, centerY, RADIUS, strokePaint)
        canvas.drawRoundRect(leftBar, BAR_CORNER, BAR_CORNER, barPaint)
        canvas.drawRoundRect(rightBar, BAR_CORNER, BAR_CORNER, barPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_DOWN) return true
        val pt = gctx.metrics.fromScreen(event.x, event.y)
        val dx = pt.x - centerX
        val dy = pt.y - centerY
        if (dx * dx + dy * dy > RADIUS * RADIUS) return false
        onTap()
        return true
    }

    companion object {
        private const val RADIUS = 52f
        private const val MARGIN_RIGHT = 90f
        private const val MARGIN_TOP = 90f
        private const val BAR_WIDTH = 14f
        private const val BAR_HEIGHT = 46f
        private const val BAR_GAP = 18f
        private const val BAR_CORNER = 4f
    }
}
