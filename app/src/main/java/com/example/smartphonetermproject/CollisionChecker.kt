package com.example.smartphonetermproject

import android.graphics.Canvas
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.collidesWith
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class CollisionChecker(private val gctx: GameContext) : IGameObject {
    override fun update(gctx: GameContext) {
        val scene = gctx.scene as? MainScene ?: return

        scene.world.forEachReversedAt(MainScene.Layer.ENEMY) { enemyObject ->
            val enemy = enemyObject as? Enemy ?: return@forEachReversedAt
            scene.world.forEachReversedAt(MainScene.Layer.BULLET) { bulletObject ->
                val bullet = bulletObject as? Bullet ?: return@forEachReversedAt
                if (bullet.collidesWith(enemy)) {
                    scene.world.remove(bullet, MainScene.Layer.BULLET)
                    enemy.decreaseLife(Bullet.DAMAGE)
                    if (enemy.dead) {
                        scene.world.remove(enemy, MainScene.Layer.ENEMY)
                        scene.addScore(enemy.score)
                    }
                }
            }
        }
    }

    override fun draw(canvas: Canvas) {}
}