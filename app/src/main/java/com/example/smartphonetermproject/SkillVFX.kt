package com.example.smartphonetermproject

import android.graphics.Canvas
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.AnimSprite
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class SkillVfx(
    gctx: GameContext,
    resId: Int,
    fps: Float,
    private val durationSec: Float,
    explicitFrameCount: Int = 0,
) : AnimSprite(gctx, resId, fps, explicitFrameCount) {

    private var elapsed = 0f
    private var done = false

    override fun update(gctx: GameContext) {
        if (done) return
        elapsed += gctx.frameTime
        if (elapsed >= durationSec) {
            done = true
            (gctx.scene as? MainScene)?.world?.remove(this, MainScene.Layer.VFX)
        }
    }

    override fun draw(canvas: Canvas) {
        if (done) return
        val raw = (elapsed * fps).toInt()
        val frameIndex = raw.coerceIn(0, frameCount - 1)
        srcRect?.set(
            frameIndex * frameWidth,
            0,
            (frameIndex + 1) * frameWidth,
            frameHeight,
        )
        canvas.drawBitmap(bitmap, srcRect, dstRect, null)
    }

    companion object {
        fun spawn(
            gctx: GameContext,
            resId: Int,
            centerX: Float,
            centerY: Float,
            displaySize: Float,
            fps: Float,
            durationSec: Float,
            frameCount: Int = 0,
        ): SkillVfx {
            return SkillVfx(gctx, resId, fps, durationSec, frameCount).apply {
                setCenter(centerX, centerY)
                setSize(displaySize, displaySize)
            }
        }
    }
}