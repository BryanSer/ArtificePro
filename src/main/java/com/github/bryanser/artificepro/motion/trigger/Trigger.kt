package com.github.bryanser.artificepro.motion.trigger

import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.motion.Motion
import com.github.bryanser.artificepro.motion.MotionManager
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.skill.SkillManager
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.*

abstract class Trigger(
        val type: String
) {
    lateinit var maxTime: Expression
    var triggerFinder: Boolean = true

    val triggerId = UUID.randomUUID()
    fun load(config: ConfigurationSection) {
        maxTime = ExpressionHelper.compileExpression(config.getString("maxTime", "0"))
        triggerFinder = config.getBoolean("triggerFinder", true)
        loadConfig(config)
    }

    abstract fun loadConfig(config: ConfigurationSection)

    abstract fun onTrigger(entity: LivingEntity, caster: Player, castId: UUID)
}

class LaunchItemTrigger:Trigger("LaunchItemTrigger"){
    lateinit var motion: Motion
    lateinit var key:String
    override fun loadConfig(config: ConfigurationSection) {
        motion = MotionManager.loadMotion(config.getConfigurationSection("Motion"))
        key = config.getString("key")
    }

    override fun onTrigger(entity: LivingEntity, caster: Player, castId: UUID) {
        val cd = SkillManager.castingSkill[castId] ?: return
        if (super.triggerFinder) {
            this.motion.cast(CastInfo(caster, entity, castId))
        } else {
            this.motion.cast(CastInfo(caster, caster, castId))
        }
    }

}

class DamageTrigger : Trigger("DamageTrigger") {
    lateinit var minDamage: Expression
    lateinit var motion: Motion

    override fun loadConfig(config: ConfigurationSection) {
        minDamage = ExpressionHelper.compileExpression(config.getString("minDamage"))
        motion = MotionManager.loadMotion(config.getConfigurationSection("Motion"))
    }

    override fun onTrigger(entity: LivingEntity, caster: Player, castId: UUID) {
        val cd = SkillManager.castingSkill[castId] ?: return
        cd.skipTrigger = true
        if (super.triggerFinder) {
            this.motion.cast(CastInfo(caster, entity, castId))
        } else {
            this.motion.cast(CastInfo(caster, caster, castId))
        }
        cd.skipTrigger = false
    }
}

class KnockTrigger : Trigger("KnockTrigger") {
    lateinit var motion: Motion

    override fun loadConfig(config: ConfigurationSection) {
        motion = MotionManager.loadMotion(config.getConfigurationSection("Motion"))
    }

    override fun onTrigger(entity: LivingEntity, caster: Player, castId: UUID) {
        val cd = SkillManager.castingSkill[castId] ?: return
        cd.skipTrigger = true
        if (super.triggerFinder) {
            this.motion.cast(CastInfo(caster, entity, castId))
        } else {
            this.motion.cast(CastInfo(caster, caster, castId))
        }
        cd.skipTrigger = false
    }

}

class EffectTrigger : Trigger("EffectTrigger") {
    lateinit var motion: Motion

    override fun loadConfig(config: ConfigurationSection) {
        motion = MotionManager.loadMotion(config.getConfigurationSection("Motion"))
    }


    override fun onTrigger(entity: LivingEntity, caster: Player, castId: UUID) {
        val cd = SkillManager.castingSkill[castId] ?: return
        cd.skipTrigger = true
        if (super.triggerFinder) {
            this.motion.cast(CastInfo(caster, entity, castId))
        } else {
            this.motion.cast(CastInfo(caster, caster, castId))
        }
        cd.skipTrigger = false
    }
}