package com.github.bryanser.artificepro.motion

import com.github.bryanser.artificepro.motion.impl.*
import com.github.bryanser.artificepro.motion.trigger.DamageTrigger
import com.github.bryanser.artificepro.motion.trigger.EffectTrigger
import com.github.bryanser.artificepro.motion.trigger.KnockTrigger
import com.github.bryanser.artificepro.script.finder.isCitizens
import com.github.bryanser.artificepro.skill.SkillManager
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.util.Vector
import java.util.*

val ignoreAttack = mutableSetOf<Int>()
fun LivingEntity.motionDamage(dmg: Double, from: Player, castId: UUID) {
    if (isCitizens(this)) {
        return
    }
    if(from === this){
        return
    }
    MotionManager.motionDamage += this.entityId
    val cd = SkillManager.castingSkill[castId]
    if (cd != null && cd.passive) {
        ignoreAttack += this.entityId
    }
    this.damage(dmg, from)
    if (cd != null && cd.passive) {
        ignoreAttack -= this.entityId
    }
    MotionManager.motionDamage -= this.entityId
    if (cd != null && !cd.skipTrigger) {
        for (t in cd.triggers) {
            if (t is DamageTrigger) {
                if (dmg < t.minDamage(cd.player).toDouble()) {
                    continue
                }
                val maxTime = t.maxTime(cd.player).toInt()
                if (maxTime > 0) {
                    val time = cd.triggerTimes.getOrPut(t.triggerId) {
                        0
                    }
                    if (time >= maxTime) {
                        continue
                    }
                    cd.triggerTimes[t.triggerId] = time + 1
                }
                t.onTrigger(this, cd.player, castId)
            }
        }
    }

}

fun LivingEntity.knock(vec: Vector, castId: UUID) {
    if (isCitizens(this)) {
        return
    }
    this.velocity = vec
    val cd = SkillManager.castingSkill[castId] ?: return
    if (!cd.skipTrigger) {
        for (t in cd.triggers) {
            if (t is KnockTrigger) {
                val maxTime = t.maxTime(cd.player).toInt()
                if (maxTime > 0) {
                    val time = cd.triggerTimes.getOrPut(t.triggerId) {
                        0
                    }
                    if (time >= maxTime) {
                        continue
                    }
                    cd.triggerTimes[t.triggerId] = time + 1
                }
                t.onTrigger(this, cd.player, castId)
            }
        }
    }
}

fun LivingEntity.effect(effect: PotionEffect, castId: UUID) {
    if (isCitizens(this)) {
        return
    }
    this.addPotionEffect(effect)
    val cd = SkillManager.castingSkill[castId] ?: return
    if (!cd.skipTrigger) {
        for (t in cd.triggers) {
            if (t is EffectTrigger) {
                val maxTime = t.maxTime(cd.player).toInt()
                if (maxTime > 0) {
                    val time = cd.triggerTimes.getOrPut(t.triggerId) {
                        0
                    }
                    if (time >= maxTime) {
                        continue
                    }
                    cd.triggerTimes[t.triggerId] = time + 1
                }
                t.onTrigger(this, cd.player, castId)
            }
        }
    }
}

object MotionManager {
    private val motions = mutableMapOf<String, Class<out Motion>>()
    val motionDamage = mutableSetOf<Int>()
    fun init() {
        registerMotion("Scattering", Scattering::class.java)
        registerMotion("Command", Command::class.java)
        registerMotion("GuidedArrow", GuidedArrow::class.java)
        registerMotion("Charge", Charge::class.java)
        registerMotion("Heal", Heal::class.java)
        registerMotion("Flash", Flash::class.java)
        registerMotion("Jump", Jump::class.java)
        registerMotion("Effect", Effect::class.java)
        registerMotion("FlamesColumn", FlamesColumn::class.java)
        registerMotion("Damage", Damage::class.java)
        registerMotion("Knock", Knock::class.java)
        registerMotion("ShockWave", ShockWave::class.java)
        registerMotion("ParticleLine", ParticleLine::class.java)
        registerMotion("ParticleCircle", ParticleCircle::class.java)
        registerMotion("Trigger", TriggerMotion::class.java)
        registerMotion("Launch", Launch::class.java)
        registerMotion("LaunchGuided", LaunchGuided::class.java)
        registerMotion("ShockWavePull", ShockWavePull::class.java)
        registerMotion("LaunchRain", LaunchRain::class.java)
        registerMotion("Lightning", Lightning::class.java)
        registerMotion("ParticleOval", ParticleOval::class.java)
        registerMotion("GreatLight",GreatLight::class.java)
        registerMotion("BuffZone",BuffZone::class.java)
    }

    fun registerMotion(name: String, cls: Class<out Motion>) {
        motions[name] = cls
    }

    fun loadMotion(config: ConfigurationSection): Motion {
        val name = config.getString("Name")
        val m = motions[name] ?: throw IllegalArgumentException("找不到动作: $name")
        val t: Motion?
        try {
            t = m.newInstance()
            t.loadConfig(config.getConfigurationSection("Config")
                    ?: throw IllegalArgumentException("缺少配置项"))
        } catch (e: Exception) {
            throw IllegalArgumentException("读取动作失败@ ${config}", e)
        }
        return t
    }


}