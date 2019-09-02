package com.github.bryanser.artificepro.passive

import com.github.bryanser.artificepro.Main
import com.github.bryanser.brapi.Utils
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.io.File
import java.util.*

object PassiveManager {
    val holdingPassive = mutableMapOf<String, MutableMap<String, Int>>()
    var extraPassive = mutableMapOf<String, MutableMap<String, Int>>()
    val passive = mutableListOf<Passive>()

    operator fun get(name: String): Passive? = passive.find { it.name == name }

    val attackEntity = mutableMapOf<UUID, LivingEntity>()
    val defenceEntity = mutableMapOf<UUID, LivingEntity>()
    var init = false
    fun init() {
        if (!init) {
            for (t in Type.values()) {
                t.init()
            }
//            Bukkit.getScheduler().runTaskTimer(Main.Plugin, {
//                for (p in Utils.getOnlinePlayers()) {
//                    checkPassive(p)
//                }
//            }, 100, 100)
            init = true
        }
        val folder = File(Main.dataFolder, "${File.separator}passive${File.separator}")
        if (!folder.exists()) {
            folder.mkdirs()
            Utils.saveResource(Main.Plugin, "passive_Example.yml", folder)
        }
        passive.clear()
        for (f in folder.listFiles()) {
            val config = YamlConfiguration.loadConfiguration(f)
            passive += Passive(config)
        }
    }

    fun checkPassive(p: Player) {
        val list = holdingPassive.getOrPut(p.name, ::mutableMapOf)
        val pd = extraPassive[p.name]
        if (pd == null) {
//            for (pass in passive) {
//                if (p.hasPermission("artificepro.passive.${pass.name}")) {
//                    var lv = pass.maxLevel
//                    while (lv > 0) {
//                        if (p.hasPermission("artificepro.plevel.${pass.name}.$lv")) {
//                            break
//                        }
//                        lv--
//                    }
//                    list[pass.name] = lv
//                } else {
//                    list.remove(pass.name)
//                }
//            }
        } else {
            list.clear()
            list.putAll(pd)
        }
    }
}