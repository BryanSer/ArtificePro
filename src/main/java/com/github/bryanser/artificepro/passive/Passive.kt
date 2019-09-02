package com.github.bryanser.artificepro.passive

import com.github.bryanser.artificepro.CastData
import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.skill.Castable
import com.github.bryanser.artificepro.skill.IStep
import com.github.bryanser.artificepro.skill.SkillManager
import com.github.bryanser.artificepro.skill.Step
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*

class Passive(config: ConfigurationSection)  {
    val name: String = config.getString("Name")
    val cooldown: Expression = ExpressionHelper.compileExpression(config.getString("Cooldown"))
    val maxLevel: Int = config.getInt("MaxLevel")
    val firstStep: Step
    val type:Type = Type.valueOf(config.getString("Type"))
    private val lastCast = mutableMapOf<String, Long>()

    init {
        val steps = mutableListOf<Step>()
        val mcs = config.getConfigurationSection("Motions")
        for (key in mcs.getKeys(false)) {
            val cs = mcs.getConfigurationSection(key)
            steps += Step(cs)
        }
        for (i in 0 until (steps.size - 1)) {
            steps[i].next = steps[i + 1]
        }
        val lastStep = steps[steps.size - 1]
        lastStep.next = object : IStep {
            override fun cast(p: Player, lv: Int, castId: UUID) {
                Bukkit.getScheduler().runTaskLater(Main.Plugin, {
                    SkillManager.castingSkill.remove(castId)
                    PassiveManager.attackEntity.remove(castId)
                    PassiveManager.defenceEntity.remove(castId)

                }, 600)
            }
        }
        firstStep = steps.first()
    }
    fun cast(p: Player,data:CastData, level: Int= -1) {
        val last = lastCast[p.name] ?: 0L
        val cd = cooldown(p).toLong()
        val pass = System.currentTimeMillis() - last
        if (pass < cd) {
            return
        }
        var lv = level
        ExpressionHelper.levelHolder[p.entityId] = lv
        val castId = data.castId
        data.level = lv
        SkillManager.castingSkill[castId] = data
        firstStep.cast(p, lv, castId)
        lastCast[p.name] = System.currentTimeMillis()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Passive

        if (name != other.name) return false
        if (maxLevel != other.maxLevel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + maxLevel
        return result
    }


}