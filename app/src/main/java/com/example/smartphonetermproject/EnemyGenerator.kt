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
        enemyTime = GEN_INTERVAL
        spawn()
    }

    private fun spawn() {
        val scene = gctx.scene as? MainScene ?: return
        val type = Enemy.Type.SPLIT
        val margin = type.width / 2f
        val x = margin + Random.nextFloat() * (gctx.metrics.width - 2 * margin)
        val enemy = Enemy.get(gctx, x, type)
        scene.world.add(enemy, MainScene.Layer.ENEMY)
    }

    override fun draw(canvas: Canvas) {}

    companion object {
        const val GEN_INTERVAL = 1.0f
    }
}