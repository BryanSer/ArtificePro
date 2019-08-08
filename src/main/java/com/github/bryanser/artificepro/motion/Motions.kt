package com.github.bryanser.artificepro.motion

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

class Scattering:Motion(
        "Scattering"
){
    val amount = 10
    val damage = 10

    override fun cast(p: Player): Boolean {
        TODO("not implemented")
    }

    override fun loadConfig(config: ConfigurationSection?): ConfigurationSection {
        TODO("not implemented")
    }

    override fun saveConfig(config: ConfigurationSection) {
        TODO("not implemented")
    }

}