package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.util.LabelUtil
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class ExpLabel(private val gctx: GameContext) : IGameObject {
    private val label = LabelUtil(TEXT_SIZE, Color.rgb(34, 211, 238), Paint.Align.LEFT)
    private val drawX = HP_GAUGE_X + gctx.metrics.width * HP_GAUGE_WIDTH_RATIO + GAP_FROM_GAUGE
    private val drawY = gctx.metrics.height - HP_MARGIN_BOTTOM

    override fun update(gctx: GameContext) {}

    override fun draw(canvas: Canvas) {
        val player = (gctx.scene as? MainScene)?.player ?: return
        label.draw(canvas, "Lv.${player.level}  EXP ${player.exp}/${player.maxExp}", drawX, drawY)
    }

    companion object {
        private const val TEXT_SIZE = 40f
        private const val HP_GAUGE_X = 30f
        private const val HP_GAUGE_WIDTH_RATIO = 0.35f
        private const val HP_MARGIN_BOTTOM = 60f
        private const val GAP_FROM_GAUGE = 25f
    }
}