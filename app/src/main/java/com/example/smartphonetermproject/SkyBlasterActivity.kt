package com.example.smartphonetermproject

import kr.ac.tukorea.ge.spgp2026.a2dg.activity.BaseGameActivity
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class SkyBlasterActivity : BaseGameActivity() {
    override fun createRootScene(gctx: GameContext): Scene = MainScene(gctx)
}