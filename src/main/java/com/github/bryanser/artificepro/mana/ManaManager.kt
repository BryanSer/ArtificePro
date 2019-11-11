package com.github.bryanser.artificepro.mana

import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.script.ExpressionResult
import com.github.bryanser.brapi.Utils
import me.clip.placeholderapi.external.EZPlaceholderHook
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.util.function.Function

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

        @JvmField
        var maxManaHandler: Function<Player,Double>? = null
        @JvmField
        var manaRecoverHandler: Function<Player,Double>? = null

        fun isEnable(): Boolean {
            return usingManage === this
        }

        init {
            val f = File(Main.dataFolder, "config.yml")
            if (!f.exists()) {
                Utils.saveResource(Main.Plugin, "config.yml", Main.dataFolder)
            }
            val config = YamlConfiguration.loadConfiguration(f)
            val max = ExpressionHelper.compileExpression(config.getString("Mana.MaxMana"))
            maxMana = {
                val mm = max(it)
                var v = mm.value + (maxManaHandler?.apply(it) ?: 0.0)
                ExpressionResult(v)
            }
            val recover = ExpressionHelper.compileExpression(config.getString("Mana.ManaRecover"))
            manaRecover = {
                val mm = recover(it)
                var v = mm.value + (manaRecoverHandler?.apply(it) ?: 0.0)
                ExpressionResult(v)
            }
            this.runTaskTimer(Main.Plugin, 20, 20)
            object : EZPlaceholderHook(Main.Plugin, "artificepromana") {
                override fun onPlaceholderRequest(p0: Player, p1: String): String {
                    if (!isEnable()) {
                        return ""
                    }
                    return String.format("%.2f", when (p1) {
                        "mana" -> getMana(p0)
                        "max" -> maxMana(p0).toDouble()
                        "recover" -> manaRecover(p0).toDouble()
                        else -> 0.0
                    })
                }
            }.hook()
        }

        fun save() {
            val f = File(Main.dataFolder, "mana.yml")
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
