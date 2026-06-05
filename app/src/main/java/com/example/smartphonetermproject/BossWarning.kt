package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class BossWarning(
    private val gctx: GameContext,
    private val onComplete: () -> Unit,
) : IGameObject {
    private var elapsed = 0f
    private var blinkTimer = 0f
    private var visible = true
    private var done = false

    private val cx = gctx.metrics.width / 2f
    private val cy = gctx.metrics.height * LINE_Y_RATIO

    private val fillPaint = Paint().apply {
        color = Color.rgb(255, 40, 40)
        textSize = TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.BLACK
        textSize = TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    override fun update(gctx: GameContext) {
        if (done) return
        elapsed += gctx.frameTime
        blinkTimer += gctx.frameTime
        if (blinkTimer >= BLINK_INTERVAL) {
            blinkTimer -= BLINK_INTERVAL
            visible = !visible
        }
        if (elapsed >= DURATION) {
            done = true
            (gctx.scene as? MainScene)?.world?.remove(this, MainScene.Layer.UI)
            onComplete()
        }
    }

    override fun draw(canvas: Canvas) {
        if (!visible) return
        canvas.drawText(LABEL, cx, cy, strokePaint)
        canvas.drawText(LABEL, cx, cy, fillPaint)
    }

    companion object {
        private const val LABEL = "WARNING"
        private const val TEXT_SIZE = 170f
        private const val DURATION = 2.0f
        private const val BLINK_INTERVAL = 0.25f
        private const val LINE_Y_RATIO = 0.4f
    }
}
