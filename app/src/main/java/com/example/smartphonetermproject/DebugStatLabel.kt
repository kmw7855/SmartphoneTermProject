package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.util.LabelUtil
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class DebugStatLabel(private val gctx: GameContext) : IGameObject {
    private val label = LabelUtil(TEXT_SIZE, Color.WHITE, Paint.Align.LEFT, Typeface.MONOSPACE)
    private val drawY = gctx.metrics.height - BOTTOM_MARGIN

    override fun update(gctx: GameContext) {}

    override fun draw(canvas: Canvas) {
        val player = (gctx.scene as? MainScene)?.player ?: return
        val text = "ATK x%.2f RATE x%.2f CRIT %d%%".format(
            player.attackMul,
            player.fireRateMul,
            (player.critRate * 100).toInt(),
        )
        label.draw(canvas, text, MARGIN_X, drawY)
    }

    companion object {
        private const val TEXT_SIZE = 40f
        private const val MARGIN_X = 30f
        private const val BOTTOM_MARGIN = 14f
    }
}
