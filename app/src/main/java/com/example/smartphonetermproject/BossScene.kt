package com.example.smartphonetermproject

import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class BossScene(gctx: GameContext) :
    MainScene(gctx, R.mipmap.boss_bg, isBossStage = true) {

    init {
        world.add(Boss(gctx), Layer.ENEMY)
    }
}
