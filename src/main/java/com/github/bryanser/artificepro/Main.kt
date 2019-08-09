package com.github.bryanser.artificepro

import com.github.bryanser.artificepro.motion.MotionManager
import com.github.bryanser.artificepro.script.FinderManager
import com.github.bryanser.artificepro.skill.SkillManager
import com.github.bryanser.brapi.ScriptManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    override fun onEnable() {
        Plugin = this
        if (!ScriptManager.hasNashorn) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        MotionManager.init()
        FinderManager.init()
        SkillManager.loadConfig()
    }

    override fun onDisable() {
    }

    companion object {
        lateinit var Plugin: Main
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty() || args[0].equals("help", true)) {
            return false
        }
        if (args[0].equals("cast", true) && args.size > 1 && sender is Player) {
            SkillManager.playerCastSkill(sender, args[1])
            return true
        }
        if(args[0].equals("reload",true)&& sender.isOp){
            SkillManager.loadConfig()
            sender.sendMessage("§6重载完成")
            return true
        }
        return false
    }
}
