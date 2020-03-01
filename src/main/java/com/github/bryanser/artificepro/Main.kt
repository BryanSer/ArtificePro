package com.github.bryanser.artificepro

import com.github.bryanser.artificepro.mana.ManaManager
import com.github.bryanser.artificepro.mark.MarkManager
import com.github.bryanser.artificepro.motion.MotionManager
import com.github.bryanser.artificepro.motion.trigger.TriggerManager
import com.github.bryanser.artificepro.particle.ParticleManager
import com.github.bryanser.artificepro.passive.PassiveManager
import com.github.bryanser.artificepro.script.FinderManager
import com.github.bryanser.artificepro.shield.ShieldManager
import com.github.bryanser.artificepro.skill.Limit
import com.github.bryanser.artificepro.skill.SkillManager
import com.github.bryanser.artificepro.tools.ArmorStandManager
import com.github.bryanser.brapi.ScriptManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Main : JavaPlugin() {

    override fun onLoad() {
        Plugin = this
        MotionManager.init()
        FinderManager.init()
        ParticleManager.init()
        TriggerManager.init()
        Limit.load()
    }

    override fun onEnable() {
        ArmorStandManager.init()
        if (!ScriptManager.hasNashorn) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        Bukkit.getPluginManager().registerEvents(ShieldManager, this)
        SkillManager.loadConfig()
        PassiveManager.init()
        ManaManager.usingManage
        MarkManager.init()
    }

    override fun onDisable() {
        ArmorStandManager.removeAll()
        ManaManager.DefaultManager.save()
    }


    companion object {
        lateinit var Plugin: Main

        val dataFolder: File by lazy {
            val df = Plugin.description
            if (df.description == null || df.description.isEmpty()) {
                return@lazy Plugin.dataFolder
            }
            val f = File(df.description)
            if (!f.exists()) {
                f.mkdirs()
            }
            f
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty() || args[0].equals("help", true)) {
            return false
        }
        if (args[0].equals("cast", true) && args.size > 1 && sender is Player) {
            if (!SkillManager.enableCommandSkillCast) {
                return true
            }
            SkillManager.playerCastSkill(sender, args[1])
            return true
        }
        if (args[0].equals("reload", true) && sender.isOp) {
            SkillManager.loadConfig()
            PassiveManager.init()
            Limit.load()
            sender.sendMessage("§6重载完成")
            return true
        }
        return false
    }
}
