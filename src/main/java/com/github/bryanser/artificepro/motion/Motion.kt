package com.github.bryanser.artificepro.motion

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

abstract class Motion(
        val name: String
) {
    abstract fun cast(p: Player): Boolean

    abstract fun loadConfig(config: ConfigurationSection?): ConfigurationSection
    abstract fun saveConfig(config: ConfigurationSection)
}