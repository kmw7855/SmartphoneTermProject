package com.example.smartphonetermproject

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.ITouchable
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class SkillButton(
    private val gctx: GameContext,
    private val centerX: Float,
    private val centerY: Float,
    private val radius: Float,
) : IGameObject, ITouchable {

    private var cooldown = 0f
    private var lastSkill: Skill? = null

    private val rectF = RectF(
        centerX - radius, centerY - radius,
        centerX + radius, centerY + radius,
    )

    private val iconCenterY = centerY + radius * ICON_OFFSET_Y_RATIO
    private val iconRect = run {
        val half = radius * ICON_FILL_RATIO
        RectF(centerX - half, iconCenterY - half, centerX + half, iconCenterY + half)
    }

    private val emptyFillPaint = Paint().apply {
        style = Paint.Style.FILL; color = Color.argb(120, 60, 60, 80); isAntiAlias = true
    }
    private val cooldownPaint = Paint().apply {
        style = Paint.Style.FILL; color = Color.argb(170, 0, 0, 0); isAntiAlias = true
    }
    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE; strokeWidth = 4f; color = Color.WHITE; isAntiAlias = true
    }
    private val labelPaint = Paint().apply {
        color = Color.WHITE
        textSize = LABEL_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    private val emptyLabelPaint = Paint().apply {
        color = Color.argb(220, 220, 220, 220)
        textSize = EMPTY_LABEL_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    private val cooldownTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = COOLDOWN_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    private val slotTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = SLOT_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    private val slotTextBaselineY: Float

    private val labelBaselineOffset = (labelPaint.fontMetrics.descent + labelPaint.fontMetrics.ascent) / 2f
    private val cooldownBaselineOffset = (cooldownTextPaint.fontMetrics.descent + cooldownTextPaint.fontMetrics.ascent) / 2f

    private val emptyLabelLine1Y: Float
    private val emptyLabelLine2Y: Float

    init {
        val fm = emptyLabelPaint.fontMetrics
        val lineHeight = fm.descent - fm.ascent
        val totalHeight = lineHeight * 2 + EMPTY_LINE_GAP
        val firstAscentY = centerY - totalHeight / 2f
        emptyLabelLine1Y = firstAscentY - fm.ascent
        emptyLabelLine2Y = emptyLabelLine1Y + lineHeight + EMPTY_LINE_GAP

        val slotFm = slotTextPaint.fontMetrics
        slotTextBaselineY = centerY + radius * SLOT_TEXT_BOTTOM_RATIO - slotFm.descent
    }

    override fun update(gctx: GameContext) {
        val skill = (gctx.scene as? MainScene)?.player?.currentSkill
        if (skill !== lastSkill) {
            cooldown = 0f
            lastSkill = skill
        }
        if (cooldown > 0f) {
            cooldown -= gctx.frameTime
            if (cooldown < 0f) cooldown = 0f
        }
    }

    override fun draw(canvas: Canvas) {
        val skill = (gctx.scene as? MainScene)?.player?.currentSkill
        if (skill == null) {
            canvas.drawCircle(centerX, centerY, radius, emptyFillPaint)
            canvas.drawCircle(centerX, centerY, radius, strokePaint)
            canvas.drawText("스킬", centerX, emptyLabelLine1Y, emptyLabelPaint)
            canvas.drawText("없음", centerX, emptyLabelLine2Y, emptyLabelPaint)
            return
        }

        canvas.drawCircle(centerX, centerY, radius, emptyFillPaint)
        if (skill.iconResId != 0) {
            val bmp = gctx.res.getBitmap(skill.iconResId)
            canvas.drawBitmap(bmp, null, iconRect, null)
        }
        if (cooldown > 0f) {
            val ratio = (cooldown / skill.cooldownTime).coerceIn(0f, 1f)
            canvas.drawArc(rectF, -90f, ratio * 360f, true, cooldownPaint)
        }
        canvas.drawCircle(centerX, centerY, radius, strokePaint)

        if (cooldown > 0f) {
            val secs = "%.1f".format(cooldown)
            canvas.drawText(secs, centerX, iconCenterY - cooldownBaselineOffset, cooldownTextPaint)
        } else if (skill.iconResId == 0) {
            canvas.drawText(skill.displayName, centerX, iconCenterY - labelBaselineOffset, labelPaint)
        }
        canvas.drawText(skill.displayName, centerX, slotTextBaselineY, slotTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> handleDown(event)
            else -> true
        }
    }

    private fun handleDown(event: MotionEvent): Boolean {
        val scene = gctx.scene as? MainScene ?: return false
        val pt = gctx.metrics.fromScreen(event.x, event.y)
        val dx = pt.x - centerX
        val dy = pt.y - centerY

        if (dx * dx + dy * dy > radius * radius) return false

        val skill = scene.player.currentSkill ?: return true
        if (cooldown > 0f) return true
        if (!skill.canActivate(scene.player)) return true

        skill.activate(scene.player, scene)
        cooldown = skill.cooldownTime
        return true
    }

    companion object {
        private const val LABEL_TEXT_SIZE = 32f
        private const val COOLDOWN_TEXT_SIZE = 36f
        private const val EMPTY_LABEL_SIZE = 26f
        private const val EMPTY_LINE_GAP = 2f
        private const val ICON_FILL_RATIO = 0.50f
        private const val ICON_OFFSET_Y_RATIO = -0.30f
        private const val SLOT_TEXT_SIZE = 22f
        private const val SLOT_TEXT_BOTTOM_RATIO = 0.70f
    }
}
