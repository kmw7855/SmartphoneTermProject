package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.collidesWith
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class CollisionChecker(private val gctx: GameContext) : IGameObject {
    private var gameOverTriggered = false

    private class DamagePopup {
        var x = 0f
        var startY = 0f
        var y = 0f
        var age = 0f
        var lifetime = POPUP_LIFETIME
        var power = 0
        var isCrit = false
    }

    private val popups = ArrayList<DamagePopup>()
    private val popupPool = ArrayList<DamagePopup>()

    fun spawnPopup(x: Float, y: Float, power: Int, isCrit: Boolean, lifetime: Float = POPUP_LIFETIME) {
        val p = if (popupPool.isNotEmpty()) popupPool.removeAt(popupPool.lastIndex) else DamagePopup()
        p.x = x + POPUP_X_OFFSET
        p.startY = y
        p.y = y
        p.age = 0f
        p.lifetime = lifetime
        p.power = power
        p.isCrit = isCrit
        popups.add(p)
    }

    override fun update(gctx: GameContext) {
        if (gameOverTriggered) return
        val scene = gctx.scene as? MainScene ?: return
        val player = scene.player

        scene.world.forEachReversedAt(MainScene.Layer.ENEMY) { enemyObject ->
            when (enemyObject) {
                is Enemy -> {
                    if (player.collidesWith(enemyObject)) {
                        player.decreaseLife(enemyObject.hitDamage)
                        enemyObject.startDying(scene)
                        if (player.dead) {
                            triggerGameOver()
                            return
                        }
                        return@forEachReversedAt
                    }

                    scene.world.forEachReversedAt(MainScene.Layer.BULLET) { bulletObject ->
                        val bullet = bulletObject as? Bullet ?: return@forEachReversedAt
                        if (bullet.collidesWith(enemyObject)) {
                            bullet.startHitting()
                            enemyObject.decreaseLife(bullet.power)
                            spawnPopup(enemyObject.x, enemyObject.y, bullet.power, bullet.isCrit)
                            if (enemyObject.dead) {
                                enemyObject.startDying(scene)
                                scene.addScore(enemyObject.score)
                            }
                        }
                    }
                }
                is Boss -> {
                    if (enemyObject.dead) return@forEachReversedAt
                    if (player.collidesWith(enemyObject)) {
                        player.decreaseLife(Boss.HIT_DAMAGE)
                        if (player.dead) {
                            triggerGameOver()
                            return
                        }
                    }
                    scene.world.forEachReversedAt(MainScene.Layer.BULLET) { bulletObject ->
                        val bullet = bulletObject as? Bullet ?: return@forEachReversedAt
                        if (bullet.collidesWith(enemyObject)) {
                            bullet.startHitting()
                            enemyObject.decreaseLife(bullet.power)
                            spawnPopup(enemyObject.x, enemyObject.y, bullet.power, bullet.isCrit)
                        }
                    }
                }
            }
        }

        scene.world.forEachReversedAt(MainScene.Layer.ENEMY_BULLET) { ebObject ->
            when (ebObject) {
                is EnemyBullet -> {
                    if (ebObject.collidesWith(player)) {
                        ebObject.startHitting()
                        player.decreaseLife(EnemyBullet.DAMAGE)
                        if (player.dead) {
                            triggerGameOver()
                            return
                        }
                    }
                }
                is BossBullet -> {
                    if (ebObject.collidesWith(player)) {
                        ebObject.startHitting()
                        player.decreaseLife(ebObject.damage)
                        if (player.dead) {
                            triggerGameOver()
                            return
                        }
                    }
                }
            }
        }

        scene.world.forEachReversedAt(MainScene.Layer.EXP_ORB) { orbObject ->
            val orb = orbObject as? ExpOrb ?: return@forEachReversedAt
            if (orb.collidesWith(player)) {
                player.gainExp(scene.expPerOrb())
                Sfx.playExpGain(gctx)
                scene.world.remove(orb, MainScene.Layer.EXP_ORB)
            }
        }

        for (i in popups.indices.reversed()) {
            val p = popups[i]
            p.age += gctx.frameTime
            if (p.age >= p.lifetime) {
                popups.removeAt(i)
                popupPool.add(p)
            } else {
                p.y = p.startY - p.age * POPUP_RISE_SPEED
            }
        }
    }

    override fun draw(canvas: Canvas) {
        for (p in popups) {
            val alpha = (255f * (1f - p.age / p.lifetime)).toInt().coerceIn(0, 255)
            val paint = if (p.isCrit) critPaint else normalPaint
            paint.alpha = alpha
            canvas.drawText(p.power.toString(), p.x, p.y, paint)
        }
    }

    private fun triggerGameOver() {
        gameOverTriggered = true
        val scene = gctx.scene as? MainScene ?: return
        GameOverScene(gctx, scene).push()
    }

    companion object {
        private const val POPUP_LIFETIME = 0.7f
        private const val POPUP_RISE_SPEED = 200f
        private const val POPUP_X_OFFSET = 60f
        private const val POPUP_NORMAL_TEXT_SIZE = 80f
        private const val POPUP_CRIT_TEXT_SIZE = 100f

        private val normalPaint = Paint().apply {
            color = Color.WHITE
            textSize = POPUP_NORMAL_TEXT_SIZE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        private val critPaint = Paint().apply {
            color = Color.rgb(255, 220, 0)
            textSize = POPUP_CRIT_TEXT_SIZE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
    }
}