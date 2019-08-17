package com.github.bryanser.artificepro

import com.github.bryanser.artificepro.motion.trigger.Trigger
import org.bukkit.Bukkit
import java.util.*

data class CastData(
        val playerName: String,
        val castId: UUID
) {
    val triggers = mutableListOf<Trigger>()
    var skipTrigger: Boolean = false

    val triggerTimes = mutableMapOf<UUID, Int>()
    val player
        get() = Bukkit.getPlayerExact(playerName)
}