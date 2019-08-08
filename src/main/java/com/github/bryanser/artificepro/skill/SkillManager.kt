package com.github.bryanser.artificepro.skill

import com.github.bryanser.artificepro.Main
import com.github.bryanser.brapi.Utils
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.logging.Level

object SkillManager {
    val skills = mutableMapOf<String, Skill>()

    fun loadConfig() {
        val folder = File(Main.Plugin.dataFolder, "${File.separator}skills${File.separator}")
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
}