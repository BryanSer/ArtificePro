package com.github.bryanser.artificepro.motion.impl

import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.motion.Motion
import com.github.bryanser.artificepro.motion.trigger.ShapeTrigger
import com.github.bryanser.artificepro.particle.Particle
import com.github.bryanser.artificepro.particle.ParticleManager
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.skill.SkillManager
import com.github.bryanser.artificepro.tools.ParticleEffect
import com.github.bryanser.brapi.Main
import com.github.bryanser.brapi.Utils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class CustomClaw : Claw("CustomClaw") {
    lateinit var speed: Expression
    lateinit var height: Expression
    lateinit var particle: Particle
    lateinit var time: Expression
    override fun cast(ci: CastInfo) {
        val spd = speed(ci.caster).toDouble()
        val height = height(ci.caster).toDouble()
        val time = time(ci.caster).toDouble()
        val mtick = (time * 20).toInt()
        val f = ci.caster.location.direction.also { it.y = 0.0 }.normalize()
        val r = Utils.getRight(f)
        val rad = Math.toRadians(angle(ci.caster).toDouble())
        val x = cos(rad)
        val z = sin(rad)
        val vec = f.multiply(x).add(r.multiply(z)).normalize().multiply(spd / 20.0)
        val max = length(ci.caster).toDouble().pow(2.0)
        object : BukkitRunnable() {
            val from = ci.caster.location
            val display = mutableListOf<Data>()
            var curr: Location = from.clone()
            val damaged = hashSetOf<Int>()
            val by = from.y

            override fun run() {
                val it = display.iterator()
                val cache = mutableListOf<Location>()

                val cd = SkillManager.castingSkill[ci.castId]
                while (it.hasNext()) {
                    val data = it.next()
                    data.times++
                    if (data.times >= mtick) {
                        it.remove()
                    }
                    data.at.y = by + height(time, data.times / 20.0, height)
                    cache.add(data.at.clone())
                    if (cd != null) {
                        for (e in data.at.world.getNearbyEntities(data.at, 0.2, 0.2, 0.2)) {
                            if (damaged.contains(e.entityId) || e == ci.caster || e !is LivingEntity) {
                                continue
                            }
                            damaged.add(e.entityId)
                            for (t in cd.triggers) {
                                if (t is ShapeTrigger && t.key == key) {
                                    t.onTrigger(e, ci.caster, ci.castId)
                                }
                            }
                        }
                    }
                }
                Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin()) {
                    for (loc in cache) {
                        particle.play(loc)
                    }
                }
                if (curr.distanceSquared(from) >= max) {
                    if (display.isEmpty()) {
                        cancel()
                    }
                    return
                }
                val loc = curr.clone()
                loc.add(vec)
                display.add(Data(loc.clone(), 0))
                curr = loc

                Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin()) {
                    particle.play(loc)
//                    ParticleEffect.REDSTONE.display(ParticleEffect.OrdinaryColor(255, 0, 0), loc, 50.0)
                }
            }
        }.runTaskTimer(Main.getPlugin(), 1, 1)
    }

    private fun height(time: Double, at: Double, height: Double): Double {
        val x = (1.0 - at / time)
        val b = -height + 1
        return -x.pow(2) + b * x + height
    }

    override fun loadConfig(config: ConfigurationSection) {
        super.loadConfig(config)
        speed = ExpressionHelper.compileExpression(config.getString("speed"))
        height = ExpressionHelper.compileExpression(config.getString("height"))
        time = ExpressionHelper.compileExpression(config.getString("time"))
        particle = ParticleManager.readParticle(config.getString("particle"))
    }

}

open class Claw(s: String) : Motion(s) {
    data class Data(
            val at: Location,
            var times: Int
    )

    constructor() : this("Claw")

    lateinit var angle: Expression
    lateinit var length: Expression
    lateinit var key: String
    override fun cast(ci: CastInfo) {
        val f = ci.caster.location.direction.also { it.y = 0.0 }.normalize()
        val r = Utils.getRight(f)
        val rad = Math.toRadians(angle(ci.caster).toDouble())
        val x = cos(rad)
        val z = sin(rad)
        val vec = f.multiply(x).add(r.multiply(z)).normalize().multiply(1 / 8.0)
        val max = length(ci.caster).toDouble().pow(2.0)
        object : BukkitRunnable() {
            val from = ci.caster.location
            val display = mutableListOf<Data>()
            var curr: Location = from.clone()
            val damaged = hashSetOf<Int>()

            override fun run() {
                val it = display.iterator()
                val cache = mutableListOf<Location>()

                val cd = SkillManager.castingSkill[ci.castId]
                while (it.hasNext()) {
                    val data = it.next()
                    data.times++
                    if (data.times >= 30) {
                        it.remove()
                    }
                    data.at.y += 0.1
                    cache.add(data.at.clone())
                    if (cd != null) {
                        for (e in data.at.world.getNearbyEntities(data.at, 0.2, 0.2, 0.2)) {
                            if (damaged.contains(e.entityId) || e == ci.caster || e !is LivingEntity) {
                                continue
                            }
                            damaged.add(e.entityId)
                            for (t in cd.triggers) {
                                if (t is ShapeTrigger && t.key == key) {
                                    t.onTrigger(e, ci.caster, ci.castId)
                                }
                            }
                        }
                    }
                }
                Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin()) {
                    for (loc in cache) {
                        ParticleEffect.REDSTONE.display(ParticleEffect.OrdinaryColor(0, 0, 255), loc, 50.0)
                    }
                }
                if (curr.distanceSquared(from) >= max) {
                    if (display.isEmpty()) {
                        cancel()
                    }
                    return
                }
                val loc = curr.clone()
                loc.add(vec)
                display.add(Data(loc.clone(), 0))
                curr = loc

                Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin()) {
                    ParticleEffect.REDSTONE.display(ParticleEffect.OrdinaryColor(255, 0, 0), loc, 50.0)
                }
            }
        }.runTaskTimer(Main.getPlugin(), 1, 1)
    }

    override fun loadConfig(config: ConfigurationSection) {
        angle = ExpressionHelper.compileExpression(config.getString("angle"))
        length = ExpressionHelper.compileExpression(config.getString("length"))
        key = config.getString("key")
    }
}