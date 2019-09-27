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
                field = value % maxSequence
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
        val cost = manaCost(p).toDouble()
        if (!ManaManager.usingManage.hasMana(p, cost)) {
            p.sendMessage("§c你没有足够的蓝释放这个技能")
            return
        }
        val last = lastCast[p.uniqueId]
        if (last != null) {
            val time = last.time
            var pass = System.currentTimeMillis() - time
            if (time > maxHoldingTime) {
                last.stage = 0
                last.time += maxHoldingTime
                pass -= maxHoldingTime
            }
            val cd: Long =
                    when (last.stage) {
                        0, maxSequence -> {
                            cooldown(p).toLong()
                        }
                        else -> {
                            sequence[last.stage - 1].cooldown(p).toLong()
                        }
                    }
            if (pass < cd) {
                p.sendMessage(String.format("§c技能还在冷却中 还需要%.1f秒", (cd - pass).toDouble() / 1000.0))
                return
            }
        }
        var next = last?.stage ?: 0 + 1
        if (next > maxSequence) {
            next = 1
        }
        sequence[next - 1]!!.cast(p, level)
        lastCast[p.uniqueId] =
                if (last == null) {
                    val t = CastInfo(System.currentTimeMillis())
                    t.stage = 2
                    t
                } else {
                    last.time = System.currentTimeMillis()
                    last.stage++
                    last
                }

    }


}

