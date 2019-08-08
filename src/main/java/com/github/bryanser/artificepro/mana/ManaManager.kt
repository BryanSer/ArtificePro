package com.github.bryanser.artificepro.mana

import org.bukkit.entity.Player

interface ManaManager {
    fun getMana(p: Player): Double

    fun costMana(p: Player, mana: Double)

    fun hasMana(p: Player, mana: Double) = getMana(p) >= mana
}
