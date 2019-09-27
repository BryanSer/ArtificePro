package com.github.bryanser.artificepro.skill.sequence

import com.github.bryanser.artificepro.CastData
import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.skill.IStep
import com.github.bryanser.artificepro.skill.SkillManager
import com.github.bryanser.artificepro.skill.Step
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*

class Sequence(
        config:ConfigurationSection
) {
    val cooldown = ExpressionHelper.compileExpression(config.getString("Cooldown"))
    val firstStep: Step

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
                }, 600)
            }
        }
        firstStep = steps.first()
    }
    fun cast(p: Player, level: Int) {
        ExpressionHelper.levelHolder[p.entityId] = level
        val castId = UUID.randomUUID()
        val data = CastData(p.name, castId)
        data.level = level
        SkillManager.castingSkill[castId] = data
        firstStep.cast(p, level, castId)
    }
}