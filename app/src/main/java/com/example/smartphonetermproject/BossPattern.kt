package com.example.smartphonetermproject

import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

sealed class BossPattern {
    abstract fun cooldown(phase: Int): Float
    abstract fun burstCount(phase: Int): Int
    abstract fun burstInterval(phase: Int): Float
    abstract fun fireTick(gctx: GameContext, boss: Boss, scene: MainScene, tickIndex: Int, phase: Int)

    object CrownShard : BossPattern() {
        override fun cooldown(phase: Int): Float = if (phase == 1) 2.0f else 1.4f
        override fun burstCount(phase: Int): Int = 1
        override fun burstInterval(phase: Int): Float = 0f

        override fun fireTick(gctx: GameContext, boss: Boss, scene: MainScene, tickIndex: Int, phase: Int) {
            val count = if (phase == 1) 5 else 7
            val speed = if (phase == 1) 450f else 540f
            val spreadDeg = if (phase == 1) 60f else 75f
            val type = if (phase == 1) BossBullet.Type.SHARD else BossBullet.Type.SHARD_P2

            val startAngle = -spreadDeg / 2f
            val step = if (count > 1) spreadDeg / (count - 1) else 0f
            val muzzleX = boss.x
            val muzzleY = boss.y + boss.height / 2f * MUZZLE_Y_RATIO

            for (i in 0 until count) {
                val rad = Math.toRadians((startAngle + i * step).toDouble())
                val vx = sin(rad).toFloat() * speed
                val vy = cos(rad).toFloat() * speed
                scene.world.add(
                    BossBullet.get(gctx, muzzleX, muzzleY, vx, vy, type),
                    MainScene.Layer.ENEMY_BULLET,
                )
            }
        }

        private const val MUZZLE_Y_RATIO = 0.6f
    }

    object EmperorScythe : BossPattern() {
        override fun cooldown(phase: Int): Float = if (phase == 1) 2.2f else 1.6f
        override fun burstCount(phase: Int): Int = if (phase == 1) 5 else 7
        override fun burstInterval(phase: Int): Float = if (phase == 1) 0.25f else 0.18f

        override fun fireTick(gctx: GameContext, boss: Boss, scene: MainScene, tickIndex: Int, phase: Int) {
            val type = if (phase == 1) BossBullet.Type.SCYTHE else BossBullet.Type.SCYTHE_P2
            val spinSpeed = if (phase == 1) 900f else 1200f
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
                spin = spinSpeed
            } else {
                startX = boss.x + boss.width / 2f
                ctrlX = w + w * CTRL_OUTSIDE_RATIO
                spin = -spinSpeed
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
                    type,
                    spin,
                ),
                MainScene.Layer.ENEMY_BULLET,
            )
        }

        private const val CTRL_OUTSIDE_RATIO = 0.1f
        private const val CTRL_Y_RATIO = 0.55f
        private const val END_SPREAD_X = 220f
        private const val END_SPREAD_Y = 120f
        private const val DURATION = 2.5f
    }

    object AimedBurst : BossPattern() {
        override fun cooldown(phase: Int): Float = if (phase == 1) 2.8f else 2.0f
        override fun burstCount(phase: Int): Int = if (phase == 1) 7 else 10
        override fun burstInterval(phase: Int): Float = if (phase == 1) 0.12f else 0.09f

        override fun fireTick(gctx: GameContext, boss: Boss, scene: MainScene, tickIndex: Int, phase: Int) {
            val type = if (phase == 1) BossBullet.Type.AIMED else BossBullet.Type.AIMED_P2
            val speed = if (phase == 1) 700f else 820f
            val muzzleX = boss.x
            val muzzleY = boss.y + boss.height / 2f * MUZZLE_Y_RATIO
            val targetX = scene.player.x
            val targetY = scene.player.y
            val dx = targetX - muzzleX
            val dy = targetY - muzzleY
            val len = hypot(dx, dy)
            val vx = if (len < 1f) 0f else dx / len * speed
            val vy = if (len < 1f) speed else dy / len * speed
            scene.world.add(
                BossBullet.get(gctx, muzzleX, muzzleY, vx, vy, type),
                MainScene.Layer.ENEMY_BULLET,
            )
        }

        private const val MUZZLE_Y_RATIO = 0.6f
    }

    object CorePulse : BossPattern() {
        override fun cooldown(phase: Int): Float = if (phase == 1) 4.5f else 3.4f
        override fun burstCount(phase: Int): Int = 1
        override fun burstInterval(phase: Int): Float = 0f

        override fun fireTick(gctx: GameContext, boss: Boss, scene: MainScene, tickIndex: Int, phase: Int) {
            val coreType = if (phase == 1) BossBullet.Type.CORE else BossBullet.Type.CORE_P2
            val shardType = if (phase == 1) BossBullet.Type.SHARD else BossBullet.Type.SHARD_P2
            val shardCount = if (phase == 1) 8 else 12
            val shardSpeed = if (phase == 1) 380f else 460f
            val sprayInterval = if (phase == 1) 0.12f else 0.08f
            val travelTime = if (phase == 1) 1.3f else 1.0f

            val muzzleX = boss.x
            val muzzleY = boss.y + boss.height / 2f * MUZZLE_Y_RATIO
            val targetX = gctx.metrics.width / 2f
            val targetY = gctx.metrics.height / 2f
            val vx = (targetX - muzzleX) / travelTime
            val vy = (targetY - muzzleY) / travelTime
            scene.world.add(
                BossBullet.getExploding(
                    gctx,
                    muzzleX, muzzleY,
                    vx, vy,
                    coreType,
                    travelTime,
                    shardCount,
                    shardSpeed,
                    shardType,
                    sprayInterval,
                ),
                MainScene.Layer.ENEMY_BULLET,
            )
        }

        private const val MUZZLE_Y_RATIO = 0.5f
    }
}
