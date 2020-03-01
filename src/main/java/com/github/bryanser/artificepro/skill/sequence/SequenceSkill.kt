package com.github.bryanser.artificepro.skill.sequence

import com.github.bryanser.artificepro.mana.ManaManager
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.skill.Castable
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*

class SequenceSkill(
        config: ConfigurationSection
) : Castable {

    override fun inCooldown(p: Player, leveL: Int): Boolean {
        val last = lastCast[p.uniqueId] ?: return false
        val cd = cooldown(p).toLong()
        val pass = System.currentTimeMillis() - last.time
        if (pass < cd) {
            return true
        }
        return false
    }
    override val name: String = config.getString("Name")
    val cooldown: Expression = ExpressionHelper.compileExpression(config.getString("Cooldown"))
    val manaCost: Expression = ExpressionHelper.compileExpression(config.getString("ManaCost"))

    val maxHoldingTime: Int = config.getInt("MaxHoldingTime")
    val maxSequence: Int
    val sequence = mutableListOf<Sequence>()

    val lastCast = mutableMapOf<UUID, CastInfo>()


    inner class CastInfo(
            var time: Long
    ) {
        var stage: Int = 0
            set(value) {
                if (value >= maxSequence) {
                    field = -1
                } else {
                    field = value
                }
            }
    }

    init {
        val ss = config.getConfigurationSection("SequenceSetting")
        maxSequence = ss.getInt("MaxSequence")
        for (i in 1..maxSequence) {
            sequence += Sequence(ss.getConfigurationSection("Sequence.$i"))
        }
    }

    override fun cast(p: Player, level: Int) {
        val last = lastCast.getOrPut(p.uniqueId) {
            val ci = CastInfo(0)
            ci.stage = -1
            ci
        }
        val time = last.time
        var pass = System.currentTimeMillis() - time
        if (pass > maxHoldingTime && last.stage != -1 && maxHoldingTime != -1) {
            last.stage = -1
            last.time += maxHoldingTime
            pass -= maxHoldingTime
        }
        val cd: Long =
                when (last.stage) {
                    1, (maxSequence - 1) -> {
                        sequence[last.stage - 1].cooldown(p).toLong()
                    }
                    else -> {
                        cooldown(p).toLong()
                    }
                }
        if (pass < cd) {
            p.sendMessage(String.format("§c技能还在冷却中 还需要%.1f秒", (cd - pass).toDouble() / 1000.0))
            return
        }

        var next = last.stage
        if (next == -1) {
            val cost = manaCost(p).toDouble()
            if (!ManaManager.usingManage.hasMana(p, cost)) {
                p.sendMessage("§c你没有足够的蓝释放这个技能")
                return
            }
            ManaManager.usingManage.costMana(p, cost)
            next = 0
        }
        sequence[next].cast(p, level)
        last.time = System.currentTimeMillis()
        last.stage = next + 1
    }


}

