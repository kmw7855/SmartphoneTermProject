package com.example.smartphonetermproject

import android.graphics.Color

sealed class Skill(
    val displayName: String,
    val effect: String,
    val color: Int,
    val cooldownTime: Float,
) {
    open fun canActivate(player: Player): Boolean = true
    abstract fun activate(player: Player, scene: MainScene)
}

private object VfxSpec {
    const val FPS = 12f
    const val DURATION_ONE_SHOT = 1.0f
    const val SIZE_PLAYER = 240f
    const val SIZE_BUFF = 600f
    const val SIZE_EXPLOSION = 960f
}

object ExplosionSkill : Skill(
    displayName = "폭발",
    effect = "스킬 : 폭발",
    color = Color.rgb(245, 130, 60),
    cooldownTime = 12f,
) {
    private const val RADIUS = 480f
    private const val DAMAGE = 50
    private const val Y_OFFSET_UP = 700f

    override fun activate(player: Player, scene: MainScene) {
        val cx = player.x
        val cy = player.y - Y_OFFSET_UP
        scene.applyAreaDamage(cx, cy, RADIUS, DAMAGE)
        scene.spawnVfx(
            resId = R.mipmap.skill_explosion,
            x = cx, y = cy,
            size = VfxSpec.SIZE_EXPLOSION,
            fps = VfxSpec.FPS,
            duration = VfxSpec.DURATION_ONE_SHOT,
        )
    }
}