package com.example.smartphonetermproject

import android.graphics.Color
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext
import kotlin.math.cos
import kotlin.math.sin

enum class WeaponGrade(val displayName: String, val cardColor: Int, val dropWeight: Float) {
    UNCOMMON("고급", Color.rgb(34, 197, 94), 0.55f),
    RARE("희귀", Color.rgb(96, 165, 250), 0.35f),
    EPIC("영웅", Color.rgb(168, 85, 247), 0.15f),
}

sealed class Weapon {
    abstract val displayName: String
    abstract val fireInterval: Float
    abstract val cardSpriteResId: Int
    abstract fun fire(player: Player, scene: MainScene, gctx: GameContext, grade: WeaponGrade)
}

object DefaultWeapon : Weapon() {
    override val displayName = "직진"
    override val fireInterval = 0.6f
    override val cardSpriteResId = R.mipmap.bullet_placeholder

    override fun fire(player: Player, scene: MainScene, gctx: GameContext, grade: WeaponGrade) {
        Sfx.playShot(gctx)
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
    override val cardSpriteResId = R.mipmap.weapon_shotgun

    override fun fire(player: Player, scene: MainScene, gctx: GameContext, grade: WeaponGrade) {
        Sfx.playShot(gctx)
        val pelletCount = if (grade == WeaponGrade.EPIC) 5 else 3
        val totalSpreadDeg = if (grade == WeaponGrade.EPIC) 40f else 30f
        val muzzleY = player.y - Player.PLAYER_HEIGHT / 2f - Player.BULLET_OFFSET
        val (power, isCrit) = player.calculatePower()
        val pelletPower = (power * PELLET_DAMAGE_MUL).toInt().coerceAtLeast(1)

        val startAngle = -totalSpreadDeg / 2f
        val step = if (pelletCount > 1) totalSpreadDeg / (pelletCount - 1) else 0f
        for (i in 0 until pelletCount) {
            val rad = Math.toRadians((startAngle + i * step).toDouble())
            val vx = sin(rad).toFloat() * Bullet.SPEED
            val vy = -cos(rad).toFloat() * Bullet.SPEED
            val pellet = Bullet.get(
                gctx, player.x, muzzleY, pelletPower, isCrit,
                vx, vy,
                R.mipmap.weapon_shotgun,
                R.mipmap.vfx_shotgun_hit,
            )
            scene.world.add(pellet, MainScene.Layer.BULLET)
        }
    }

    private const val PELLET_DAMAGE_MUL = 0.6f
}

object HomingWeapon : Weapon() {
    override val displayName = "유도 미사일"
    override val fireInterval = 0.8f
    override val cardSpriteResId = R.mipmap.weapon_homing

    override fun fire(player: Player, scene: MainScene, gctx: GameContext, grade: WeaponGrade) {
        Sfx.playShot(gctx)
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

object LaserWeapon : Weapon() {
    override val displayName = "레이저"
    override val fireInterval = 2.2f
    override val cardSpriteResId = R.mipmap.weapon_laser

    override fun fire(player: Player, scene: MainScene, gctx: GameContext, grade: WeaponGrade) {
        Sfx.playLaser(gctx)
        val beamHalf = if (grade == WeaponGrade.EPIC) 150f else 60f
        val muzzleY = player.y - Player.PLAYER_HEIGHT / 2f - Player.BULLET_OFFSET
        scene.world.add(
            LaserBeam.get(gctx, muzzleY, LASER_LIFETIME, beamHalf),
            MainScene.Layer.LASER,
        )
    }

    private const val LASER_LIFETIME = 1.0f
}
