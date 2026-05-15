package com.example.smartphonetermproject

import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.cos
import kotlin.math.hypot
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

    object CorePulse : BossPattern() {
        override val cooldown: Float = COOLDOWN
        override val burstCount: Int = 1
        override val burstInterval: Float = 0f

        override fun fireTick(gctx: GameContext, boss: Boss, scene: MainScene, tickIndex: Int) {
            val muzzleX = boss.x
            val muzzleY = boss.y + boss.height / 2f * MUZZLE_Y_RATIO
            val targetX = gctx.metrics.width / 2f
            val targetY = gctx.metrics.height / 2f
            val vx = (targetX - muzzleX) / TRAVEL_TIME
            val vy = (targetY - muzzleY) / TRAVEL_TIME
            scene.world.add(
                BossBullet.getExploding(
                    gctx,
                    muzzleX, muzzleY,
                    vx, vy,
                    BossBullet.Type.CORE,
                    TRAVEL_TIME,
                    SHARD_COUNT,
                    SHARD_SPEED,
                    BossBullet.Type.SHARD,
                    SPRAY_INTERVAL,
                ),
                MainScene.Layer.ENEMY_BULLET,
            )
        }

        private const val COOLDOWN = 4.5f
        private const val TRAVEL_TIME = 1.3f
        private const val SHARD_COUNT = 8
        private const val SHARD_SPEED = 380f
        private const val SPRAY_INTERVAL = 0.12f
        private const val MUZZLE_Y_RATIO = 0.5f
    }

    object AimedBurst : BossPattern() {
        override val cooldown: Float = COOLDOWN
        override val burstCount: Int = VOLLEY_COUNT
        override val burstInterval: Float = BURST_INTERVAL

        override fun fireTick(gctx: GameContext, boss: Boss, scene: MainScene, tickIndex: Int) {
            val muzzleX = boss.x
            val muzzleY = boss.y + boss.height / 2f * MUZZLE_Y_RATIO
            val targetX = scene.player.x
            val targetY = scene.player.y
            val dx = targetX - muzzleX
            val dy = targetY - muzzleY
            val len = hypot(dx, dy)
            val vx = if (len < 1f) 0f else dx / len * SPEED
            val vy = if (len < 1f) SPEED else dy / len * SPEED
            scene.world.add(
                BossBullet.get(gctx, muzzleX, muzzleY, vx, vy, BossBullet.Type.AIMED),
                MainScene.Layer.ENEMY_BULLET,
            )
        }

        private const val COOLDOWN = 2.8f
        private const val VOLLEY_COUNT = 7
        private const val BURST_INTERVAL = 0.12f
        private const val SPEED = 700f
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
