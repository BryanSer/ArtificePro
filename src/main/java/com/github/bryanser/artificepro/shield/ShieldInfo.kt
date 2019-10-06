package com.github.bryanser.artificepro.shield

import org.bukkit.Color

data class ShieldInfo(
        val maxShield: Double,
        val endTime: Long,
        val color: Color = Color.BLUE
) {
    var shield = maxShield
}