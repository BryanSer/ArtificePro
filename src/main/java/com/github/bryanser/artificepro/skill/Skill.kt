package com.github.bryanser.artificepro.skill

import com.github.bryanser.artificepro.mana.ManaManager
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

class Skill(
        config: ConfigurationSection
) {
    val name: String = config.getString("Name")
    val cooldown: Expression = ExpressionHelper.compileExpression(config.getString("Cooldown"))
    val manaCost: Expression = ExpressionHelper.compileExpression(config.getString("ManaCost"))
    val maxLevel: Int = config.getInt("MaxLevel")
    val firstStep: Step
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
        firstStep = steps.first()
    }

    fun cast(p: Player) {
        val cost = manaCost(p).toDouble()
        if (!ManaManager.usingManage.hasMana(p, cost)) {
            p.sendMessage("§c你没有足够的蓝释放这个技能")
            return
        }
        val last = lastCast[p.name] ?: 0L
        val cd = cooldown(p).toLong()
        val pass = System.currentTimeMillis() - last
        if (pass < cd) {
            p.sendMessage(String.format("§c技能还在冷却中 还需要%.2f秒", (cd - pass) / 1000.0))
            return
        }
        ManaManager.usingManage.costMana(p, cost)
        var level = maxLevel
        while (level > 0) {
            if (p.hasPermission("artificepro.level.${name}.$level")) {
                break
            }
            level--
        }
        ExpressionHelper.levelHolder[p.entityId] = level
        firstStep.cast(p, level)
        lastCast[p.name] = System.currentTimeMillis()
    }
}