package com.example.smartphonetermproject

import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.cos
import kotlin.math.sin

enum class WeaponGrade(val displayName: String) {
    RARE("희귀"),
    EPIC("영웅"),
}

sealed class Weapon {
    abstract val displayName: String
    abstract val fireInterval: Float
    abstract fun fire(player: Player, scene: MainScene, gctx: GameContext, grade: WeaponGrade)
}

object DefaultWeapon : Weapon() {
    override val displayName = "직진"
    override val fireInterval = 0.3f

    override fun fire(player: Player, scene: MainScene, gctx: GameContext, grade: WeaponGrade) {
        val muzzleY = player.y - Player.PLAYER_HEIGHT / 2f - Player.BULLET_OFFSET
        val (power, isCrit) = player.calculatePower()
        scene.world.add(
            Bullet.get(gctx, player.x, muzzleY, power, isCrit),
            MainScene.Layer.BULLET,
        )
    }
}

object ShotgunWeapon : Weapon() {
    override val displayName = "샷건"
    override val fireInterval = 0.6f

    override fun fire(player: Player, scene: MainScene, gctx: GameContext, grade: WeaponGrade) {
        val pelletCount = if (grade == WeaponGrade.EPIC) 5 else 3
        val totalSpreadDeg = if (grade == WeaponGrade.EPIC) 40f else 30f
        val muzzleY = player.y - Player.PLAYER_HEIGHT / 2f - Player.BULLET_OFFSET
        val (power, isCrit) = player.calculatePower()

        val startAngle = -totalSpreadDeg / 2f
        val step = if (pelletCount > 1) totalSpreadDeg / (pelletCount - 1) else 0f
        for (i in 0 until pelletCount) {
            val rad = Math.toRadians((startAngle + i * step).toDouble())
            val vx = sin(rad).toFloat() * Bullet.SPEED
            val vy = -cos(rad).toFloat() * Bullet.SPEED
            val pellet = Bullet.get(
                gctx, player.x, muzzleY, power, isCrit,
                vx, vy,
                R.mipmap.weapon_shotgun,
                R.mipmap.vfx_shotgun_hit,
            )
            scene.world.add(pellet, MainScene.Layer.BULLET)
        }
    }
}

object HomingWeapon : Weapon() {
    override val displayName = "유도 미사일"
    override val fireInterval = 0.8f

    override fun fire(player: Player, scene: MainScene, gctx: GameContext, grade: WeaponGrade) {
        val HomingCount = if (grade == WeaponGrade.EPIC) 2 else 1
        val muzzleY = player.y - Player.PLAYER_HEIGHT / 2f - Player.BULLET_OFFSET
        val (power, isCrit) = player.calculatePower()
        for (i in 0 until HomingCount) {
            val offsetX = if (HomingCount == 2) (i * 2 - 1) * 30f else 0f
            scene.world.add(
                Bullet.get(
                    gctx, player.x + offsetX, muzzleY, power, isCrit,
                    vx = 0f, vy = -Homing_INITIAL_SPEED,
                    spriteResId = R.mipmap.weapon_homing,
                    hitVfxResId = R.mipmap.vfx_homing_hit,
                    turnRate = Homing_TURN_RATE,
                    targetSpeed = Homing_TARGET_SPEED,
                ),
                MainScene.Layer.BULLET,
            )
        }
    }

    private const val Homing_INITIAL_SPEED = 600f
    private const val Homing_TARGET_SPEED = 1100f
    private const val Homing_TURN_RATE = 6f
}