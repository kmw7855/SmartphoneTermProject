package com.example.smartphonetermproject

import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.cos
import kotlin.math.sin

sealed class BossPattern {
    abstract val cooldown: Float
    abstract fun fire(gctx: GameContext, boss: Boss, scene: MainScene)

    object CrownShard : BossPattern() {
        override val cooldown: Float = COOLDOWN

        override fun fire(gctx: GameContext, boss: Boss, scene: MainScene) {
            val startAngle = -SPREAD_DEG / 2f
            val step = if (COUNT > 1) SPREAD_DEG / (COUNT - 1) else 0f
            val muzzleX = boss.x
            val muzzleY = boss.y + boss.height / 2f * MUZZLE_Y_RATIO

            for (i in 0 until COUNT) {
                val rad = Math.toRadians((startAngle + i * step).toDouble())
                val vx = sin(rad).toFloat() * SPEED
                val vy = cos(rad).toFloat() * SPEED
                scene.world.add(
                    BossBullet.get(gctx, muzzleX, muzzleY, vx, vy, BossBullet.Type.SHARD),
                    MainScene.Layer.ENEMY_BULLET,
                )
            }
        }

        private const val COOLDOWN = 2.0f
        private const val COUNT = 5
        private const val SPEED = 450f
        private const val SPREAD_DEG = 60f
        private const val MUZZLE_Y_RATIO = 0.6f
    }
}
