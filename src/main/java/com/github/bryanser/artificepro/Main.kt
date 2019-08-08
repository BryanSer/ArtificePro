package com.github.bryanser.artificepro

import com.github.bryanser.brapi.ScriptManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    override fun onEnable() {
        Plugin = this
        if(!ScriptManager.hasNashorn){
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    companion object {
        lateinit var Plugin: Main
    }
}
