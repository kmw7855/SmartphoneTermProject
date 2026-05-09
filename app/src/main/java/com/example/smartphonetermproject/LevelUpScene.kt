package com.example.smartphonetermproject

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import kr.ac.tukorea.ge.spgp2026.a2dg.scene.Scene
import kr.ac.tukorea.ge.spgp2026.a2dg.view.GameContext

class LevelUpScene(
    gctx: GameContext,
    private val mainScene: MainScene,
    private val cards: List<RewardCard>,
) : Scene(gctx) {

    private val backgroundPaint = Paint().apply { style = Paint.Style.FILL; color = Color.argb(160, 0, 0, 0) }
    private val cardFillPaint = Paint().apply { style = Paint.Style.FILL; color = Color.rgb(28, 36, 56); isAntiAlias = true }
    private val cardStrokePaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 4f; isAntiAlias = true }
    private val titlePaint = Paint().apply {
        color = Color.rgb(34, 211, 238); textSize = TITLE_TEXT_SIZE; textAlign = Paint.Align.CENTER
        isAntiAlias = true; isFakeBoldText = true
    }
    private val cardTextPaint = Paint().apply {
        color = Color.WHITE; textSize = CARD_TEXT_SIZE; textAlign = Paint.Align.CENTER; isAntiAlias = true
    }
    private val gradeTextPaint = Paint().apply {
        textSize = GRADE_TEXT_SIZE; textAlign = Paint.Align.CENTER; isAntiAlias = true; isFakeBoldText = true
    }

    private val titleX = gctx.metrics.width / 2f
    private val titleY = gctx.metrics.height * 0.30f

    private val cardRects: List<RectF> = run {
        val totalWidth = CARD_WIDTH * 3 + CARD_GAP * 2
        val startX = (gctx.metrics.width - totalWidth) / 2f
        val cardY = gctx.metrics.height * 0.42f
        (0..2).map { i ->
            val left = startX + i * (CARD_WIDTH + CARD_GAP)
            RectF(left, cardY, left + CARD_WIDTH, cardY + CARD_HEIGHT)
        }
    }

    private val weaponBitmaps = mutableMapOf<Int, Bitmap>()
    private val weaponSpriteRect = RectF()

    private val skillBitmaps = mutableMapOf<Int, Bitmap>()
    private val skillIconRect = RectF()

    private val skillIconFillPaint = Paint().apply {
        style = Paint.Style.FILL; isAntiAlias = true
    }
    private val skillIconStrokePaint = Paint().apply {
        style = Paint.Style.STROKE; strokeWidth = 4f; color = Color.WHITE; isAntiAlias = true
    }
    private val skillIconLabelPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    override fun update(gctx: GameContext) {}

    override fun draw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, gctx.metrics.width, gctx.metrics.height, backgroundPaint)
        canvas.drawText("Level Up!", titleX, titleY, titlePaint)
        for ((i, rect) in cardRects.withIndex()) {
            val card = cards.getOrNull(i) ?: continue
            canvas.drawRoundRect(rect, CARD_CORNER, CARD_CORNER, cardFillPaint)
            cardStrokePaint.color = card.cardColor
            canvas.drawRoundRect(rect, CARD_CORNER, CARD_CORNER, cardStrokePaint)
            when (card) {
                is WeaponCard -> drawWeaponCard(canvas, rect, card)
                is SkillCard -> drawSkillCard(canvas, rect, card)
                else -> drawStatCard(canvas, rect, card)
            }
        }
    }

    private fun drawStatCard(canvas: Canvas, rect: RectF, card: RewardCard) {
        gradeTextPaint.color = card.grade.cardColor
        drawFitText(canvas, card.grade.displayName,
            rect.centerX(), rect.top + CARD_HEIGHT * 0.30f, gradeTextPaint, GRADE_TEXT_SIZE)
        drawFitText(canvas, card.title,  rect.centerX(), rect.centerY() - CARD_TEXT_SIZE * 0.3f, cardTextPaint, CARD_TEXT_SIZE)
        drawFitText(canvas, card.effect, rect.centerX(), rect.centerY() + CARD_TEXT_SIZE * 0.9f, cardTextPaint, CARD_TEXT_SIZE)
    }

    private fun drawSkillCard(canvas: Canvas, rect: RectF, card: SkillCard) {
        val iconRadius = CARD_WIDTH * 0.275f
        val iconCx = rect.centerX()
        val iconCy = rect.top + CARD_HEIGHT * 0.30f

        if (card.skill.iconResId != 0) {
            val bmp = skillBitmaps.getOrPut(card.skill.iconResId) {
                gctx.res.getBitmap(card.skill.iconResId)
            }
            skillIconRect.set(
                iconCx - iconRadius, iconCy - iconRadius,
                iconCx + iconRadius, iconCy + iconRadius,
            )
            canvas.drawBitmap(bmp, null, skillIconRect, null)
        } else {
            skillIconFillPaint.color = card.skill.color
            canvas.drawCircle(iconCx, iconCy, iconRadius, skillIconFillPaint)
            canvas.drawCircle(iconCx, iconCy, iconRadius, skillIconStrokePaint)

            skillIconLabelPaint.textSize = iconRadius * 0.85f
            val fm = skillIconLabelPaint.fontMetrics
            canvas.drawText(card.skill.displayName, iconCx, iconCy - (fm.ascent + fm.descent) / 2f, skillIconLabelPaint)
        }

        gradeTextPaint.color = card.grade.cardColor
        drawFitText(canvas, card.grade.displayName,
            rect.centerX(), rect.top + CARD_HEIGHT * 0.55f, gradeTextPaint, GRADE_TEXT_SIZE)
        drawFitText(canvas, card.effect,
            rect.centerX(), rect.top + CARD_HEIGHT * 0.70f, cardTextPaint, CARD_TEXT_SIZE)
    }

    private fun drawWeaponCard(canvas: Canvas, rect: RectF, card: WeaponCard) {
        val bmp = weaponBitmaps.getOrPut(card.weapon.cardSpriteResId) {
            gctx.res.getBitmap(card.weapon.cardSpriteResId)
        }
        val size = CARD_WIDTH * 0.55f
        val cy = rect.top + CARD_HEIGHT * 0.28f
        weaponSpriteRect.set(
            rect.centerX() - size / 2f, cy - size / 2f,
            rect.centerX() + size / 2f, cy + size / 2f,
        )
        canvas.drawBitmap(bmp, null, weaponSpriteRect, null)

        gradeTextPaint.color = card.grade.cardColor
        drawFitText(canvas, card.grade.displayName,
            rect.centerX(), rect.top + CARD_HEIGHT * 0.55f, gradeTextPaint, GRADE_TEXT_SIZE)
        drawFitText(canvas, card.weapon.displayName,
            rect.centerX(), rect.top + CARD_HEIGHT * 0.70f, cardTextPaint, CARD_TEXT_SIZE)
        drawFitText(canvas, card.effect,
            rect.centerX(), rect.top + CARD_HEIGHT * 0.85f, cardTextPaint, CARD_TEXT_SIZE)
    }

    private fun drawFitText(canvas: Canvas, text: String, cx: Float, y: Float, paint: Paint, baseSize: Float) {
        paint.textSize = baseSize
        val measured = paint.measureText(text)
        val maxWidth = CARD_WIDTH - CARD_TEXT_PADDING * 2f
        if (measured > maxWidth) paint.textSize = baseSize * (maxWidth / measured)
        canvas.drawText(text, cx, y, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_UP) return true
        val pt = gctx.metrics.fromScreen(event.x, event.y)
        for ((i, rect) in cardRects.withIndex()) {
            if (rect.contains(pt.x, pt.y)) {
                onCardSelected(i)
                return true
            }
        }
        return true
    }

    private fun onCardSelected(idx: Int) {
        val card = cards.getOrNull(idx) ?: return
        card.apply(mainScene.player)
        mainScene.cardPool.consume(card)
        mainScene.player.levelUp()
        pop()
    }

    companion object {
        private const val TITLE_TEXT_SIZE = 110f
        private const val CARD_WIDTH = 260f
        private const val CARD_HEIGHT = 420f
        private const val CARD_GAP = 40f
        private const val CARD_CORNER = 32f
        private const val CARD_TEXT_SIZE = 40f
        private const val GRADE_TEXT_SIZE = 40f
        private const val CARD_TEXT_PADDING = 16f
    }
}