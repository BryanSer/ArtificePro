package com.github.bryanser.artificepro.motion.impl

import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.motion.Motion
import com.github.bryanser.artificepro.script.FinderManager
import com.github.bryanser.artificepro.script.finder.Finder
import com.github.bryanser.artificepro.script.finder.PlayerFinderTemplate
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

class Command : Motion("Command") {
    //target,from
    val commands: MutableList<(Player, Player) -> Unit> = mutableListOf()
    lateinit var finder: Finder<Player>
    override fun loadConfig(config: ConfigurationSection) {
        //commands = config.getStringList("Commands")
        for (cmd in config.getStringList("Commands")) {
            val arg = cmd.split(":".toRegex(), 2)
            if (arg.size < 2) {
                throw IllegalArgumentException("动作Command编写错误 缺少p,target,op,targetop或c")
            }
            val cmd = arg[1]
            when (arg[0].toLowerCase()) {
                "p" -> {
                    commands += { target, from ->
                        try {
                            Bukkit.dispatchCommand(from,
                                    cmd.replace("%target%", target.name).replace("%from%", from.name)
                            )
                        } catch (e: Exception) {
                        }
                    }
                }
                "target" -> {
                    commands += { target, from ->
                        try {
                            Bukkit.dispatchCommand(target,
                                    cmd.replace("%target%", target.name).replace("%from%", from.name)
                            )
                        } catch (e: Exception) {
                        }
                    }
                }
                "op" -> {
                    commands += { target, from ->
                        val op = from.isOp
                        try {
                            from.isOp = true
                            Bukkit.dispatchCommand(from,
                                    cmd.replace("%target%", target.name).replace("%from%", from.name)
                            )
                        } finally {
                            from.isOp = op
                        }
                    }
                }
                "targetop" -> {
                    commands += { target, from ->
                        val op = target.isOp
                        try {
                            target.isOp = true
                            Bukkit.dispatchCommand(target,
                                    cmd.replace("%target%", target.name).replace("%from%", from.name)
                            )
                        } finally {
                            target.isOp = op
                        }
                    }
                }
                "c" -> {
                    commands += { target, from ->
                        try {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                    cmd.replace("%target%", target.name).replace("%from%", from.name)
                            )
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        }
        val (f, t) = FinderManager.readFinder(config.getString("Finder", "Self()"))
        if (t !is PlayerFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Player")
        }
        finder = f as Finder<Player>
    }

    override fun cast(ci: CastInfo) {
        val p = ci.caster
        for (target in finder(ci)) {
            for (cmd in commands) {
                cmd(target, p)
            }
        }
    }


}