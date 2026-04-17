package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.util.LabelUtil
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class ScoreLabel(private val gctx: GameContext) : IGameObject {
    private val label = LabelUtil(TEXT_SIZE, Color.WHITE, Paint.Align.LEFT)
    private var displayScore = 0

    override fun update(gctx: GameContext) {
        val scene = gctx.scene as? MainScene ?: return
        val target = scene.score
        val diff = target - displayScore
        if (diff == 0) return
        // 작으면 1 씩, 크면 10분의 1 씩 따라잡는다.
        displayScore += when {
            diff in -9..-1 -> -1
            diff in 1..9 -> 1
            else -> diff / 10
        }
    }

    override fun draw(canvas: Canvas) {
        label.draw(canvas, "Score: $displayScore", MARGIN_X, MARGIN_Y)
    }

    companion object {
        private const val TEXT_SIZE = 60f
        private const val MARGIN_X = 30f
        private const val MARGIN_Y = 80f
    }
}