package com.example.smartphonetermproject

import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class BossScene(
    gctx: GameContext,
    sourcePlayer: Player? = null,
    sourceScore: Int = 0,
) : MainScene(gctx, R.mipmap.boss_bg, isBossStage = true) {

    val boss = Boss(gctx)

    init {
        sourcePlayer?.let { player.copyStateFrom(it) }
        if (sourceScore > 0) addScore(sourceScore)
        world.add(boss, Layer.ENEMY)
        world.add(BossHpHud(gctx, boss), Layer.UI)
        world.add(BossClearLabel(gctx, boss, this), Layer.UI)
    }
}
