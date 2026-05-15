package com.example.smartphonetermproject

import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

sealed class BossPattern {
    abstract val cooldown: Float
    abstract val burstCount: Int
    abstract val burstInterval: Float
    abstract fun fireTick(gctx: GameContext, boss: Boss, scene: MainScene, tickIndex: Int)

    object CrownShard : BossPattern() {
        override val cooldown: Float = COOLDOWN
        override val burstCount: Int = 1
        override val burstInterval: Float = 0f

        override fun fireTick(gctx: GameContext, boss: Boss, scene: MainScene, tickIndex: Int) {
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

    object EmperorScythe : BossPattern() {
        override val cooldown: Float = COOLDOWN
        override val burstCount: Int = VOLLEY_COUNT
        override val burstInterval: Float = BURST_INTERVAL

        override fun fireTick(gctx: GameContext, boss: Boss, scene: MainScene, tickIndex: Int) {
            val player = scene.player
            val w = gctx.metrics.width.toFloat()
            val h = gctx.metrics.height.toFloat()
            val fromLeft = tickIndex % 2 == 0
            val startX: Float
            val ctrlX: Float
            val spin: Float
            if (fromLeft) {
                startX = boss.x - boss.width / 2f
                ctrlX = -w * CTRL_OUTSIDE_RATIO
                spin = SPIN_DEG_PER_SEC
            } else {
                startX = boss.x + boss.width / 2f
                ctrlX = w + w * CTRL_OUTSIDE_RATIO
                spin = -SPIN_DEG_PER_SEC
            }
            val endX = player.x + (Random.nextFloat() * 2f - 1f) * END_SPREAD_X
            val endY = player.y + (Random.nextFloat() * 2f - 1f) * END_SPREAD_Y
            scene.world.add(
                BossBullet.getBezier(
                    gctx,
                    startX, boss.y,
                    ctrlX, h * CTRL_Y_RATIO,
                    endX, endY,
                    DURATION,
                    BossBullet.Type.SCYTHE,
                    spin,
                ),
                MainScene.Layer.ENEMY_BULLET,
            )
        }

        private const val COOLDOWN = 2.2f
        private const val VOLLEY_COUNT = 5
        private const val BURST_INTERVAL = 0.25f
        private const val CTRL_OUTSIDE_RATIO = 0.1f
        private const val CTRL_Y_RATIO = 0.55f
        private const val END_SPREAD_X = 220f
        private const val END_SPREAD_Y = 120f
        private const val DURATION = 2.5f
        private const val SPIN_DEG_PER_SEC = 900f
    }
}
