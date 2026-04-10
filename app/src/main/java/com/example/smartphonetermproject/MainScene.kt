package com.example.smartphonetermproject

import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.World
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class MainScene(gctx: GameContext) : Scene(gctx) {
    enum class Layer {
        BACKGROUND,
        PLAYER,
        BULLET,
        ENEMY,
        CONTROLLER,
        UI,
    }
    override val world = World(Layer.entries.toTypedArray())
}