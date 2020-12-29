package com.github.bryanser.artificepro.skill

import org.bukkit.entity.Player

interface Castable {
    val name: String
    fun cast(p: Player, level: Int = -1)

    fun cooldown(p:Player, level:Int = -1):Double

    fun inCooldown(p: Player, leveL: Int = -1): Boolean {
        return false
    }
}