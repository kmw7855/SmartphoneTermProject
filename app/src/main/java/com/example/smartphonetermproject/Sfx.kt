package com.example.smartphonetermproject

import android.content.Context
import android.media.MediaPlayer
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

object Sfx {
    private val shotSounds = intArrayOf(R.raw.shoot1, R.raw.shoot2, R.raw.shoot3)
    private var buffPlayer: MediaPlayer? = null
    private var buffContext: Context? = null

    fun preloadAll(gctx: GameContext) {
        val sound = gctx.res.sound
        for (resId in shotSounds) sound.preload(resId)
        sound.preload(R.raw.laser)
        sound.preload(R.raw.hit)
        sound.preload(R.raw.die)
        sound.preload(R.raw.bossshoot)
        sound.preload(R.raw.exp)
        sound.preload(R.raw.heal)
        sound.preload(R.raw.skill_exp)
    }

    fun playShot(gctx: GameContext) {
        gctx.res.sound.playEffect(shotSounds.random(), SHOT_VOLUME)
    }

    fun playLaser(gctx: GameContext) {
        gctx.res.sound.playEffect(R.raw.laser, LASER_VOLUME)
    }

    fun playPlayerHit(gctx: GameContext) {
        gctx.res.sound.playEffect(R.raw.hit, HIT_VOLUME)
    }

    fun playEnemyDie(gctx: GameContext) {
        gctx.res.sound.playEffect(R.raw.die, DIE_VOLUME)
    }

    fun playBossShot(gctx: GameContext) {
        gctx.res.sound.playEffect(R.raw.bossshoot, BOSS_SHOT_VOLUME)
    }

    fun playExpGain(gctx: GameContext) {
        gctx.res.sound.playEffect(R.raw.exp, EXP_VOLUME)
    }

    fun playHeal(gctx: GameContext) {
        gctx.res.sound.playEffect(R.raw.heal, HEAL_VOLUME)
    }

    fun playExplosion(gctx: GameContext) {
        gctx.res.sound.playEffect(R.raw.skill_exp, EXPLOSION_VOLUME)
    }

    fun startBuffLoop(gctx: GameContext) {
        stopBuffLoop()
        buffContext = gctx.view.context.applicationContext
        buffPlayer = createBuffPlayer()
    }

    private fun createBuffPlayer(): MediaPlayer? {
        val ctx = buffContext ?: return null
        return MediaPlayer.create(ctx, R.raw.boost)?.apply {
            isLooping = true
            setVolume(BUFF_VOLUME, BUFF_VOLUME)
            setOnErrorListener { _, _, _ ->
                recoverBuffLoop()
                true
            }
            start()
        }
    }

    private fun recoverBuffLoop() {
        val dead = buffPlayer
        buffPlayer = if (buffContext != null) createBuffPlayer() else null
        dead?.release()
    }

    fun pauseBuffLoop() {
        buffPlayer?.let { if (it.isPlaying) it.pause() }
    }

    fun resumeBuffLoop() {
        buffPlayer?.let { if (!it.isPlaying) it.start() }
    }

    fun stopBuffLoop() {
        buffPlayer?.release()
        buffPlayer = null
        buffContext = null
    }

    private const val SHOT_VOLUME = 0.15f
    private const val LASER_VOLUME = 0.3f
    private const val HIT_VOLUME = 0.6f
    private const val DIE_VOLUME = 0.35f
    private const val BOSS_SHOT_VOLUME = 0.4f
    private const val EXP_VOLUME = 0.4f
    private const val HEAL_VOLUME = 1.0f
    private const val EXPLOSION_VOLUME = 1.0f
    private const val BUFF_VOLUME = 0.9f
}
