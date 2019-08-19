package com.github.bryanser.artificepro.motion

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.*

abstract class Motion(
        val name: String
) {
    abstract fun cast(ci: CastInfo)

    abstract fun loadConfig(config: ConfigurationSection)
}

data class CastInfo(
        val caster: Player,
        val finderTarget: LivingEntity,
        val castId: UUID
){
    fun finder():LivingEntity = finderTarget
}