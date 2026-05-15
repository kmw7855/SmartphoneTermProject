package com.example.smartphonetermproject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kr.ac.tukorea.ge.spgp2026.a2dg.objects.IGameObject
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class BossClearLabel(
    private val gctx: GameContext,
    private val boss: Boss,
    private val scene: MainScene,
) : IGameObject {
    private val cx = gctx.metrics.width / 2f
    private val cy = gctx.metrics.height / 2f

    private val panelRect = RectF(
        cx - PANEL_WIDTH / 2f,
        cy - PANEL_HEIGHT / 2f,
        cx + PANEL_WIDTH / 2f,
        cy + PANEL_HEIGHT / 2f,
    )

    private val panelBgPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.argb(220, 18, 24, 38)
        isAntiAlias = true
    }
    private val panelStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.rgb(255, 200, 80)
        isAntiAlias = true
    }
    private val titlePaint = Paint().apply {
        color = Color.rgb(255, 220, 0)
        textSize = TITLE_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    private val titleStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = Color.BLACK
        textSize = TITLE_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = SCORE_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    private val cardFillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.rgb(28, 36, 56)
        isAntiAlias = true
    }
    private val cardStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    private val cardTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = CARD_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    private val gradeTextPaint = Paint().apply {
        textSize = GRADE_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    private val badgeFillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.rgb(40, 50, 70)
        isAntiAlias = true
    }
    private val badgeStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    private val badgeTextPaint = Paint().apply {
        color = Color.rgb(235, 240, 250)
        textSize = BADGE_TEXT_SIZE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    private val weaponBitmaps = mutableMapOf<Int, Bitmap>()
    private val skillBitmaps = mutableMapOf<Int, Bitmap>()
    private val spriteRect = RectF()
    private val iconRect = RectF()

    override fun update(gctx: GameContext) {}

    override fun draw(canvas: Canvas) {
        if (!boss.dead) return

        canvas.drawRoundRect(panelRect, CORNER_RADIUS, CORNER_RADIUS, panelBgPaint)
        canvas.drawRoundRect(panelRect, CORNER_RADIUS, CORNER_RADIUS, panelStrokePaint)

        var lineY = panelRect.top + TITLE_OFFSET_FROM_TOP
        canvas.drawText(TITLE_LABEL, cx, lineY, titleStrokePaint)
        canvas.drawText(TITLE_LABEL, cx, lineY, titlePaint)

        lineY += TITLE_TO_SCORE_GAP
        canvas.drawText("SCORE  ${scene.score}", cx, lineY, scorePaint)

        val items = collectItems(scene.player)
        if (items.isEmpty()) return
        val totalW = items.size * CARD_WIDTH + (items.size - 1) * CARD_GAP
        val rowStartX = cx - totalW / 2f
        val rowY = panelRect.top + CARD_ROW_TOP_OFFSET
        for ((i, item) in items.withIndex()) {
            val left = rowStartX + i * (CARD_WIDTH + CARD_GAP)
            val rect = RectF(left, rowY, left + CARD_WIDTH, rowY + CARD_HEIGHT)
            drawMiniCard(canvas, rect, item.card, item.count)
        }
    }

    private fun collectItems(player: Player): List<DisplayItem> {
        val list = mutableListOf<DisplayItem>()
        if (player.attackCardCount > 0) list += DisplayItem(AttackStatCard, player.attackCardCount)
        if (player.fireRateCardCount > 0) list += DisplayItem(FireRateStatCard, player.fireRateCardCount)
        if (player.critCardCount > 0) list += DisplayItem(CritRateStatCard, player.critCardCount)
        list += DisplayItem(WeaponCard(player.currentWeapon, player.weaponGrade), 0)
        player.currentSkill?.let { list += DisplayItem(SkillCard(it), 0) }
        return list
    }

    private data class DisplayItem(val card: RewardCard, val count: Int)

    private fun drawMiniCard(canvas: Canvas, rect: RectF, card: RewardCard, count: Int) {
        canvas.drawRoundRect(rect, CARD_CORNER, CARD_CORNER, cardFillPaint)
        cardStrokePaint.color = card.cardColor
        canvas.drawRoundRect(rect, CARD_CORNER, CARD_CORNER, cardStrokePaint)

        when (card) {
            is WeaponCard -> drawWeaponMini(canvas, rect, card)
            is SkillCard -> drawSkillMini(canvas, rect, card)
            else -> drawStatMini(canvas, rect, card)
        }

        if (count > 0) drawCountBadge(canvas, rect, count, card.cardColor)
    }

    private fun drawStatMini(canvas: Canvas, rect: RectF, card: RewardCard) {
        gradeTextPaint.color = card.grade.cardColor
        drawFitText(canvas, card.grade.displayName,
            rect.centerX(), rect.top + CARD_HEIGHT * 0.30f, gradeTextPaint, GRADE_TEXT_SIZE)
        drawFitText(canvas, card.title,
            rect.centerX(), rect.centerY() - CARD_TEXT_SIZE * 0.3f, cardTextPaint, CARD_TEXT_SIZE)
        drawFitText(canvas, card.effect,
            rect.centerX(), rect.centerY() + CARD_TEXT_SIZE * 0.9f, cardTextPaint, CARD_TEXT_SIZE)
    }

    private fun drawWeaponMini(canvas: Canvas, rect: RectF, card: WeaponCard) {
        val bmp = weaponBitmaps.getOrPut(card.weapon.cardSpriteResId) {
            gctx.res.getBitmap(card.weapon.cardSpriteResId)
        }
        val size = CARD_WIDTH * 0.55f
        val spriteCy = rect.top + CARD_HEIGHT * 0.28f
        spriteRect.set(
            rect.centerX() - size / 2f, spriteCy - size / 2f,
            rect.centerX() + size / 2f, spriteCy + size / 2f,
        )
        canvas.drawBitmap(bmp, null, spriteRect, null)

        gradeTextPaint.color = card.grade.cardColor
        drawFitText(canvas, card.grade.displayName,
            rect.centerX(), rect.top + CARD_HEIGHT * 0.60f, gradeTextPaint, GRADE_TEXT_SIZE)
        drawFitText(canvas, card.weapon.displayName,
            rect.centerX(), rect.top + CARD_HEIGHT * 0.76f, cardTextPaint, CARD_TEXT_SIZE)
    }

    private fun drawSkillMini(canvas: Canvas, rect: RectF, card: SkillCard) {
        val iconRadius = CARD_WIDTH * 0.27f
        val iconCx = rect.centerX()
        val iconCy = rect.top + CARD_HEIGHT * 0.30f

        if (card.skill.iconResId != 0) {
            val bmp = skillBitmaps.getOrPut(card.skill.iconResId) {
                gctx.res.getBitmap(card.skill.iconResId)
            }
            iconRect.set(
                iconCx - iconRadius, iconCy - iconRadius,
                iconCx + iconRadius, iconCy + iconRadius,
            )
            canvas.drawBitmap(bmp, null, iconRect, null)
        }

        gradeTextPaint.color = card.grade.cardColor
        drawFitText(canvas, card.grade.displayName,
            rect.centerX(), rect.top + CARD_HEIGHT * 0.60f, gradeTextPaint, GRADE_TEXT_SIZE)
        drawFitText(canvas, card.skill.displayName,
            rect.centerX(), rect.top + CARD_HEIGHT * 0.76f, cardTextPaint, CARD_TEXT_SIZE)
    }

    private fun drawFitText(canvas: Canvas, text: String, cx: Float, y: Float, paint: Paint, baseSize: Float) {
        paint.textSize = baseSize
        val measured = paint.measureText(text)
        val maxWidth = CARD_WIDTH - CARD_TEXT_PADDING * 2f
        if (measured > maxWidth) paint.textSize = baseSize * (maxWidth / measured)
        canvas.drawText(text, cx, y, paint)
    }

    private fun drawCountBadge(canvas: Canvas, rect: RectF, count: Int, gradeColor: Int) {
        val radius = CARD_WIDTH * 0.18f
        val centerX = rect.right - radius * 0.55f
        val centerY = rect.top + radius * 0.55f
        canvas.drawCircle(centerX, centerY, radius, badgeFillPaint)
        badgeStrokePaint.color = gradeColor
        canvas.drawCircle(centerX, centerY, radius, badgeStrokePaint)
        val fm = badgeTextPaint.fontMetrics
        canvas.drawText("×$count", centerX, centerY - (fm.ascent + fm.descent) / 2f, badgeTextPaint)
    }

    companion object {
        private const val PANEL_WIDTH = 820f
        private const val PANEL_HEIGHT = 620f
        private const val CORNER_RADIUS = 28f
        private const val TITLE_LABEL = "BOSS CLEAR!"
        private const val TITLE_TEXT_SIZE = 130f
        private const val SCORE_TEXT_SIZE = 72f
        private const val TITLE_OFFSET_FROM_TOP = 140f
        private const val TITLE_TO_SCORE_GAP = 110f
        private const val CARD_ROW_TOP_OFFSET = 320f
        private const val CARD_WIDTH = 130f
        private const val CARD_HEIGHT = 220f
        private const val CARD_GAP = 16f
        private const val CARD_CORNER = 16f
        private const val CARD_TEXT_SIZE = 22f
        private const val GRADE_TEXT_SIZE = 22f
        private const val CARD_TEXT_PADDING = 6f
        private const val BADGE_TEXT_SIZE = 26f
    }
}
