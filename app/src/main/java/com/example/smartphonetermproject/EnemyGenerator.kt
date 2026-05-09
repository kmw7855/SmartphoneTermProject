package com.example.smartphonetermproject

import android.graphics.Canvas
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.random.Random

class EnemyGenerator(private val gctx: GameContext) : IGameObject {
    private var enemyTime = GEN_INTERVAL

    override fun update(gctx: GameContext) {
        enemyTime -= gctx.frameTime
        if (enemyTime > 0f) return
        val mul = (gctx.scene as? MainScene)?.spawnRateMul() ?: 1f
        enemyTime = GEN_INTERVAL / mul
        spawn()
    }

    private fun spawn() {
        val scene = gctx.scene as? MainScene ?: return
        val type = pickType(scene.elapsedSec)
        val margin = type.width / 2f
        val x = margin + Random.nextFloat() * (gctx.metrics.width - 2 * margin)
        val enemy = Enemy.get(gctx, x, type)
        scene.world.add(enemy, MainScene.Layer.ENEMY)
    }

    private fun pickType(elapsedSec: Float): Enemy.Type {
        val suicideWeight = SUICIDE_BASE_WEIGHT
        val rangedWeight = ((elapsedSec - RANGED_START_SEC) / RANGED_RAMP_SEC)
            .coerceIn(0f, RANGED_MAX_WEIGHT)
        val splitWeight = ((elapsedSec - SPLIT_START_SEC) / SPLIT_RAMP_SEC)
            .coerceIn(0f, SPLIT_MAX_WEIGHT)

        val total = suicideWeight + rangedWeight + splitWeight
        val r = Random.nextFloat() * total
        return when {
            r < suicideWeight -> Enemy.Type.SUICIDE
            r < suicideWeight + rangedWeight -> Enemy.Type.RANGED
            else -> Enemy.Type.SPLIT
        }
    }

    override fun draw(canvas: Canvas) {}

    companion object {
        const val GEN_INTERVAL = 1.0f

        private const val SUICIDE_BASE_WEIGHT = 1f

        private const val RANGED_START_SEC = 5f
        private const val RANGED_RAMP_SEC = 10f
        private const val RANGED_MAX_WEIGHT = 1f

        private const val SPLIT_START_SEC = 10f
        private const val SPLIT_RAMP_SEC = 20f
        private const val SPLIT_MAX_WEIGHT = 1f
    }
}