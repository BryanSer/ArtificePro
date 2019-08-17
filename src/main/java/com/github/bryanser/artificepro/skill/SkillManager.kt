package com.github.bryanser.artificepro.skill

import com.github.bryanser.artificepro.CastData
import com.github.bryanser.artificepro.Main
import com.github.bryanser.brapi.Utils
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*
import java.util.logging.Level


object SkillManager {
    var enableCommandSkillCast = true
    val skills = mutableMapOf<String, Castable>()
    val castingSkill = mutableMapOf<UUID, CastData>()

    fun loadConfig() {
        skills.clear()
        val folder = File(Main.dataFolder, "${File.separator}skills${File.separator}")
        if (!folder.exists()) {
            folder.mkdirs()
            Utils.saveResource(Main.Plugin, "example.yml", folder)
        }
        for (f in folder.listFiles()) {
            val config = YamlConfiguration.loadConfiguration(f)
            try {
                val skill = Skill(config)
                skills[skill.name] = skill
            } catch (e: Exception) {
                Bukkit.getLogger().log(Level.WARNING, e, { "[ArtificePro] 读取文件: ${f.path}时出错" })
            }
        }
    }

    fun playerCastSkill(p: Player, skill: String) {
        val skill = skills[skill]
        if (skill == null) {
            p.sendMessage("§c找不到这个技能")
            return
        }
        if (!p.hasPermission("artificepro.cast.${skill.name}")) {
            p.sendMessage("§c你不能使用这个技能")
            return
        }
        skill.cast(p)
    }
}