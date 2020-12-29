package com.github.bryanser.artificepro.skill

import com.github.bryanser.artificepro.CastData
import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.mana.ManaManager
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*

class Skill(
        config: ConfigurationSection
) : Castable {
    override fun inCooldown(p: Player, leveL: Int): Boolean {
        val last = lastCast[p.uniqueId] ?: 0L
        val cd = cooldownExp(p).toLong()
        val pass = System.currentTimeMillis() - last
        if (pass < cd) {
            return true
        }
        return false
    }

    override val name: String = config.getString("Name")
    val cooldownExp: Expression = ExpressionHelper.compileExpression(config.getString("Cooldown"))
    val manaCost: Expression = ExpressionHelper.compileExpression(config.getString("ManaCost"))
    val maxLevel: Int = config.getInt("MaxLevel")
    val firstStep: Step
    private val lastCast = mutableMapOf<UUID, Long>()

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

    override fun cooldown(p: Player, level: Int ): Double {
        val last = lastCast[p.uniqueId] ?: 0L
        val cd = cooldownExp(p).toLong()
        val pass = System.currentTimeMillis() - last
        if (pass < cd) {
            return 1 - pass.toDouble() / cd
        }
        return 0.0
    }

    override fun cast(p: Player, level: Int) {
        val cost = manaCost(p).toDouble()
        if (!ManaManager.usingManage.hasMana(p, cost)) {
            p.sendMessage("§f[§c系统§f] §c你没有足够的蓝释放这个技能")
            return
        }
        val last = lastCast[p.uniqueId] ?: 0L
        val cd = cooldownExp(p).toLong()
        val pass = System.currentTimeMillis() - last
        if (pass < cd) {
            p.sendMessage(String.format("§f[§c系统§f] §c技能正在冷却,还剩§e %.2f §c秒!", (cd - pass).toDouble() / 1000.0))
            return
        }
        ManaManager.usingManage.costMana(p, cost)
        var lv = level
        if (lv == -1) {
            lv = maxLevel
            while (lv > 0) {
                if (p.hasPermission("artificepro.level.${name}.$lv")) {
                    break
                }
                lv--
            }
        }
        ExpressionHelper.levelHolder[p.entityId] = lv
        val castId = UUID.randomUUID()
        val data = CastData(p.name, castId)
        data.level = lv
        SkillManager.castingSkill[castId] = data
        firstStep.cast(p, lv, castId)
        lastCast[p.uniqueId] = System.currentTimeMillis()
    }
}