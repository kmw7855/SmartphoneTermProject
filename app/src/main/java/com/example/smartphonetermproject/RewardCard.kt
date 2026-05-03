package com.example.smartphonetermproject

import kotlin.random.Random

sealed class RewardCard {
    abstract val title: String
    abstract val effect: String
    open val grade: WeaponGrade = WeaponGrade.UNCOMMON
    open val cardColor: Int = WeaponGrade.UNCOMMON.cardColor
    abstract fun apply(player: Player)
}

object AttackStatCard : RewardCard() {
    override val title = "공격력"
    override val effect = "x2"
    override fun apply(player: Player) {
        player.attackMul *= ATK_BOOST
    }

    private const val ATK_BOOST = 2.0f
}

object FireRateStatCard : RewardCard() {
    override val title = "공속"
    override val effect = "+30%"
    override fun apply(player: Player) {
        player.fireRateMul *= RATE_BOOST
    }

    private const val RATE_BOOST = 1.3f
}

object CritRateStatCard : RewardCard() {
    override val title = "치명타"
    override val effect = "+50%"
    override fun apply(player: Player) {
        player.critRate = (player.critRate + CRIT_BOOST).coerceAtMost(1f)
    }

    private const val CRIT_BOOST = 0.5f
}

class WeaponCard(
    val weapon: Weapon,
    override val grade: WeaponGrade,
) : RewardCard() {
    override val title = weapon.displayName
    override val effect = "장착"
    override val cardColor = grade.cardColor
    override fun apply(player: Player) {
        player.currentWeapon = weapon
        player.weaponGrade = grade
    }
}

class CardPool {
    private val statCards: List<RewardCard> = listOf(
        AttackStatCard,
        FireRateStatCard,
        CritRateStatCard,
    )

    private val weaponCards: MutableList<WeaponCard> = mutableListOf(
        WeaponCard(ShotgunWeapon, WeaponGrade.RARE),
        WeaponCard(ShotgunWeapon, WeaponGrade.EPIC),
        WeaponCard(LaserWeapon,   WeaponGrade.RARE),
        WeaponCard(LaserWeapon,   WeaponGrade.EPIC),
        WeaponCard(HomingWeapon,  WeaponGrade.RARE),
        WeaponCard(HomingWeapon,  WeaponGrade.EPIC),
    )

    fun pickThree(): List<RewardCard> {
        val pool = (statCards + weaponCards).toMutableList()
        val result = mutableListOf<RewardCard>()
        while (result.size < 3 && pool.isNotEmpty()) {
            val byGrade = pool.groupBy { it.grade }
            val grades = byGrade.keys.toList()
            val totalWeight = grades.sumOf { it.dropWeight.toDouble() }
            var roll = Random.nextDouble() * totalWeight
            var pickedGrade = grades.last()
            for (g in grades) {
                roll -= g.dropWeight
                if (roll <= 0) { pickedGrade = g; break }
            }
            val card = byGrade.getValue(pickedGrade).random()
            result.add(card)
            pool.remove(card)
        }
        return result
    }

    fun consume(card: RewardCard) {
        if (card !is WeaponCard) return
        weaponCards.remove(card)
        if (card.grade == WeaponGrade.EPIC) {
            weaponCards.removeAll { it.weapon == card.weapon && it.grade == WeaponGrade.RARE }
        }
    }
}
