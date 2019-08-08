package com.github.bryanser.artificepro

import com.github.bryanser.artificepro.motion.MotionManager
import com.github.bryanser.artificepro.script.FinderManager
import com.github.bryanser.artificepro.skill.SkillManager
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
        MotionManager.init()
        SkillManager.loadConfig()
        FinderManager.init()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    companion object {
        lateinit var Plugin: Main
    }
}
