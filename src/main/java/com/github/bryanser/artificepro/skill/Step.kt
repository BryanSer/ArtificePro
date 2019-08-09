package com.github.bryanser.artificepro.skill

import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.motion.MotionManager
import com.github.bryanser.artificepro.script.ExpressionHelper
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

class Step(
        config: ConfigurationSection
) {
    var next: Step? = null
    val level: Int = config.getInt("Level")
    val run: (Player, Int) -> Unit

    init {
        if (config.getString("Name") == "Delay") {
            val time = config.getLong("Config.Time")
            run = { it, lv ->
                Bukkit.getScheduler().runTaskLater(Main.Plugin, {
                    if (next != null) {
                        ExpressionHelper.levelHolder[it.entityId] = lv
                        next!!.cast(it, lv)
                    }
                }, time)
            }
        } else {
            val motion = MotionManager.loadMotion(config)
            run = { it, lv ->
                motion.cast(it)
                if (next != null) {
                    next!!.cast(it, lv)
                }
            }
        }
    }

    fun cast(p: Player, lv: Int) {
        if (lv < level) {
            if (next != null) {
                next!!.cast(p, lv)
                return
            }
        }
        this.run(p, lv)
    }

}