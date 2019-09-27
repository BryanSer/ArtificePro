package com.github.bryanser.artificepro.motion.impl

import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.motion.Motion
import com.github.bryanser.artificepro.motion.MotionManager
import com.github.bryanser.artificepro.particle.Particle
import com.github.bryanser.artificepro.particle.ParticleManager
import com.github.bryanser.artificepro.passive.PassiveManager
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.cos
import kotlin.math.sin

class BuffZone : Motion("BuffZone") {
    lateinit var length: Expression
    lateinit var radius: Expression
    lateinit var time: Expression
    lateinit var triggerTick: Expression
    val motion = mutableListOf<Motion>()
    lateinit var once: Expression
    val particle = mutableListOf<Pair<Particle, Double>>()

    override fun cast(ci: CastInfo) {
        val pp = ci.caster
        val rng = length(pp).toInt()
        val target = pp.getTargetBlock(setOf(Material.AIR), rng)?.location ?: return

        val ttime = this.time(pp).toInt()
        val r = radius(pp).toDouble()
        val tt = triggerTick(pp).toInt()
        val once = this.once(pp).toBoolean()
        val p = 8
        val task = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.Plugin, {
            var st = 0.0
            val add = Math.PI / p
            while (st <= Math.PI * 2) {
                val x = cos(st) * r
                val z = sin(st) * r
                for ((par,y) in particle) {
                    val loc = target.clone().add(x, y, z)
                    par.play(loc)
                }
                st += add
            }
        }, 5, 5)
        object : BukkitRunnable() {
            val casted = mutableSetOf<Int>()
            var time = -1
            override fun run() {
                if (time++ >= ttime) {
                    this.cancel()
                    task.cancel()
                    return
                }
                if (time % tt == 0) {
                    for (t in target.world.getNearbyEntities(target, r, r, r)) {
                        if (once && casted.contains(t.entityId) || t !is LivingEntity) {
                            continue
                        }
                        casted += t.entityId
                        for (m in motion) {
                            PassiveManager.attackEntity[ci.castId] = t
                            m.cast(CastInfo(pp, t, ci.castId))
                        }
                    }
                }
            }

        }.runTaskTimer(Main.Plugin, 1, 1)
    }

    override fun loadConfig(config: ConfigurationSection) {
        for (s in config.getStringList("Particle")) {
            val str = s.split(",".toRegex(), 2)
            particle += ParticleManager.readParticle(str[1]) to str[0].toDouble()
        }
        length = ExpressionHelper.compileExpression(config.getString("length"))
        radius = ExpressionHelper.compileExpression(config.getString("radius"))
        triggerTick = ExpressionHelper.compileExpression(config.getString("triggerTick"))
        once = ExpressionHelper.compileExpression(config.getString("once"), true)
        time = ExpressionHelper.compileExpression(config.getString("time"))
        val mcs = config.getConfigurationSection("Motions")
        for (key in mcs.getKeys(false)) {
            val cs = mcs.getConfigurationSection(key)
            motion += MotionManager.loadMotion(cs)
        }
    }
}