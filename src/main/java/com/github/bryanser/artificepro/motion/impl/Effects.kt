package com.github.bryanser.artificepro.motion.impl

import com.github.bryanser.artificepro.motion.*
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.script.FinderManager
import com.github.bryanser.artificepro.script.finder.EntityFinderTemplate
import com.github.bryanser.artificepro.script.finder.Finder
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Effect : Motion("Effect") {
    var id: Int = 0
    lateinit var time: Expression
    lateinit var level: Expression
    lateinit var finder: Finder<LivingEntity>

    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val tick = time(p).toInt()
        val lv = level(p).toInt() - 1
        for (target in finder(ci)) {
            target.effect(PotionEffect(PotionEffectType.getById(id), tick, lv), ci.castId)
        }
    }

    override fun loadConfig(config: ConfigurationSection) {
        id = config.getInt("id")
        time = ExpressionHelper.compileExpression(config.getString("time"))
        level = ExpressionHelper.compileExpression(config.getString("level"))
        val (f, t) = FinderManager.readFinder(config.getString("Finder"))
        if (t !is EntityFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Entity")
        }
        finder = f as Finder<LivingEntity>

    }

}

class Heal : Motion("Heal") {
    lateinit var amount: Expression
    lateinit var percentage: Expression
    lateinit var finder: Finder<LivingEntity>

    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val per = percentage(p).toBoolean()
        val v = amount(p).toDouble()
        for (target in finder(ci)) {
            val max = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).value
            val h = if (per) {
                max * v
            } else {
                v
            }
            var health = p.health + h
            if (health > max) {
                health = max
            } else if (health < 0) {
                health = 0.0
            }
            target.health = health
        }
    }

    override fun loadConfig(config: ConfigurationSection) {
        amount = ExpressionHelper.compileExpression(config.getString("amount"))
        percentage = ExpressionHelper.compileExpression(config.getString("percentage"), true)
        val (f, t) = FinderManager.readFinder(config.getString("Finder"))
        if (t !is EntityFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Entity")
        }
        finder = f as Finder<LivingEntity>

    }
}

class Damage : Motion("Damage") {
    lateinit var damage: Expression
    lateinit var finder: Finder<LivingEntity>
    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val dmg = damage(p).toDouble()
        for (target in finder(ci)) {
            target.motionDamage(dmg, p, ci.castId)
        }
    }

    override fun loadConfig(config: ConfigurationSection) {
        damage = ExpressionHelper.compileExpression(config.getString("damage"))
        val (f, t) = FinderManager.readFinder(config.getString("Finder"))
        if (t !is EntityFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Entity")
        }
        finder = f as Finder<LivingEntity>

    }
}

class Knock : Motion("Knock") {
    lateinit var knock: Expression
    lateinit var finder: Finder<LivingEntity>

    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val k = knock(p).toDouble()
        for (target in finder(ci)) {
            val vec = target.location.toVector().subtract(p.location.toVector())
            vec.y = 1.0
            vec.normalize().multiply(k)
            target.knock(vec, ci.castId)
        }
    }


    override fun loadConfig(config: ConfigurationSection) {
        knock = ExpressionHelper.compileExpression(config.getString("knock"))
        val (f, t) = FinderManager.readFinder(config.getString("Finder"))
        if (t !is EntityFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Entity")
        }
        finder = f as Finder<LivingEntity>

    }

}