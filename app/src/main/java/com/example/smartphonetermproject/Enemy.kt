package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IBoxCollidable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IRecyclable
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.Sprite
import kr.ac.tukorea.ge.spgp2026.a2dg.util.Gauge
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class Enemy private constructor(
    private val gctx: GameContext,
) : Sprite(gctx, Type.SUICIDE.resId), IBoxCollidable, IRecyclable {

    enum class Type(
        val resId: Int,
        val width: Float,
        val height: Float,
        val hp: Int,
        val speed: Float,
        val score: Int,
        val hitDamage: Int,
    ) {

        SUICIDE(R.mipmap.enemy_suicide, 130f, 130f, 1, 280f, 10, 5),
        RANGED(R.mipmap.enemy_ranged, 155f, 155f, 2, 150f, 20, 5),
        SPLIT(R.mipmap.enemy_split, 120f, 120f, 3, 220f, 30, 5),
    }

    val score: Int get() = type.score
    val hitDamage: Int get() = type.hitDamage

    private lateinit var type: Type
    var life = 0
        private set
    var maxLife = 0
        private set
    private var speed = 0f
    val dead: Boolean get() = life <= 0

    override var width = 0f
    override var height = 0f
    override var x = 0f
    override var y = 0f
    override val collisionRect = RectF()

    init {
        if (sharedGauge == null) {
            sharedGauge = Gauge(GAUGE_THICKNESS, GAUGE_FG_COLOR, GAUGE_BG_COLOR)
        }
    }

    fun init(x: Float, type: Type): Enemy {
        this.type = type
        this.bitmap = gctx.res.getBitmap(type.resId)
        this.width = type.width
        this.height = type.height
        this.life = type.hp
        this.maxLife = type.hp
        this.speed = type.speed
        this.x = x
        this.y = -type.height / 2f
        syncDstRect()
        updateCollisionRect()
        return this
    }

    override fun update(gctx: GameContext) {
        y += speed * gctx.frameTime
        syncDstRect()
        updateCollisionRect()
        if (y - height / 2f > gctx.metrics.height) {
            val scene = gctx.scene as? MainScene ?: return
            scene.world.remove(this, MainScene.Layer.ENEMY)
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val gauge = sharedGauge ?: return
        val gaugeWidth = width * 0.7f
        val gaugeX = x - gaugeWidth / 2f
        val gaugeY = y - height / 2f - GAUGE_OFFSET_FROM_TOP
        gauge.draw(canvas, gaugeX, gaugeY, gaugeWidth, life.toFloat() / maxLife)
    }

    fun decreaseLife(damage: Int) {
        life -= damage
    }

    private fun updateCollisionRect() {
        val halfW = width * COLLISION_INSET_RATIO / 2f
        val halfH = height * COLLISION_INSET_RATIO / 2f
        collisionRect.set(x - halfW, y - halfH, x + halfW, y + halfH)
    }

    override fun onRecycle() {}

    companion object {
        private var sharedGauge: Gauge? = null
        private const val GAUGE_THICKNESS = 0.12f
        private val GAUGE_FG_COLOR = Color.GREEN
        private val GAUGE_BG_COLOR = Color.argb(180, 0, 0, 0)
        private const val GAUGE_OFFSET_FROM_TOP = 8f
        private const val COLLISION_INSET_RATIO = 0.8f

        fun get(gctx: GameContext, x: Float, type: Type): Enemy {
            val scene = gctx.scene as? MainScene ?: return Enemy(gctx).init(x, type)
            val enemy = scene.world.obtain(Enemy::class.java) ?: Enemy(gctx)
            return enemy.init(x, type)
        }
    }
}