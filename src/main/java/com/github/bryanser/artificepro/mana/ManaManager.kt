package com.github.bryanser.artificepro.mana

import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.brapi.Utils
import me.clip.placeholderapi.external.EZPlaceholderHook
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.io.File

interface ManaManager {
    fun getMana(p: Player): Double

    fun getMaxMana(p: Player): Double

    fun getManRecover(p: Player): Double

    fun costMana(p: Player, mana: Double)

    fun hasMana(p: Player, mana: Double) = getMana(p) >= mana

    companion object {
        var usingManage: ManaManager = DefaultManager
    }

    object DefaultManager : ManaManager, BukkitRunnable() {
        override fun getMaxMana(p: Player): Double = maxMana(p).toDouble()

        override fun getManRecover(p: Player): Double = manaRecover(p).toDouble()

        override fun run() {
            if (!isEnable()) {
                this.cancel()
                return
            }
            for (p in Utils.getOnlinePlayers()) {
                val r = manaRecover(p).toDouble()
                val max = maxMana(p).toDouble()
                var curr = mana[p.name] ?: max
                curr += r
                if (curr > max) {
                    curr = max
                }
                mana[p.name] = curr
            }
        }

        val maxMana: Expression
        val manaRecover: Expression
        val mana = mutableMapOf<String, Double>()

        fun isEnable(): Boolean {
            return usingManage === this
        }

        init {
            val f = File(Main.Plugin.dataFolder, "config.yml")
            if (!f.exists()) {
                Utils.saveResource(Main.Plugin, "config.yml")
            }
            val config = YamlConfiguration.loadConfiguration(f)
            maxMana = ExpressionHelper.compileExpression(config.getString("Mana.MaxMana"))
            manaRecover = ExpressionHelper.compileExpression(config.getString("Mana.ManaRecover"))
            this.runTaskTimer(Main.Plugin, 20, 20)
            object : EZPlaceholderHook(Main.Plugin, "artificepromana") {
                override fun onPlaceholderRequest(p0: Player, p1: String): String {
                    if (!isEnable()) {
                        return ""
                    }
                    return when (p1) {
                        "mana" -> getMana(p0).toString()
                        "max" -> maxMana(p0).toDouble().toString()
                        "recover" -> manaRecover(p0).toDouble().toString()
                        else -> ""
                    }
                }
            }.hook()
        }

        fun save() {
            val f = File(Main.Plugin.dataFolder, "mana.yml")
            val data = YamlConfiguration()
            for ((p, m) in mana) {
                data.set(p, m)
            }
            data.save(f)
        }

        override fun costMana(p: Player, mana: Double) {
            val max = maxMana(p).toDouble()
            var curr = this.mana[p.name] ?: max
            curr -= mana
            if (curr < 0) {
                curr = 0.0
            }
            if (curr > max) {
                curr = max
            }
            this.mana[p.name] = curr
        }

        override fun getMana(p: Player): Double = this.mana[p.name] ?: maxMana(p).toDouble()
    }
}
