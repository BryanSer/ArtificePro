package com.github.bryanser.artificepro.motion.impl

import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.motion.Motion
import com.github.bryanser.artificepro.particle.Particle
import com.github.bryanser.artificepro.particle.ParticleManager
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.script.FinderManager
import com.github.bryanser.artificepro.script.finder.Finder
import com.github.bryanser.artificepro.script.finder.LocationFinderTemplate
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.*

class Lightning : Motion("Lightning") {
    lateinit var finder: Finder<Location>
    override fun cast(ci: CastInfo) {
        for (loc in finder(ci)) {
            loc.world.strikeLightningEffect(loc)
        }
    }

    override fun loadConfig(config: ConfigurationSection) {
        val (f0, t0) = FinderManager.readFinder(config.getString("Finder"))
        if (t0 !is LocationFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Location")
        }
        finder = f0 as Finder<Location>
    }


}

class ParticleLine : Motion("ParticleLine") {
    lateinit var finder0: Finder<Location>
    lateinit var finder1: Finder<Location>
    lateinit var particle: Particle
    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val loc0 = finder0(ci).firstOrNull() ?: return
        val loc1 = finder1(ci).firstOrNull() ?: return
        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
            var p = loc0.distance(loc1)
            val vec = loc1.toVector().subtract(loc0.toVector()).normalize()
            while (p >= 0) {
                val t = vec.clone().multiply(p)
                val loc = loc0.clone().add(t)
                particle.play(loc)
                p -= 0.25
            }
        }
    }

    override fun loadConfig(config: ConfigurationSection) {
        val (f0, t0) = FinderManager.readFinder(config.getString("Finder0"))
        if (t0 !is LocationFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder0类型不是Location")
        }
        finder0 = f0 as Finder<Location>
        val (f1, t1) = FinderManager.readFinder(config.getString("Finder1"))
        if (t1 !is LocationFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder1类型不是Location")
        }
        finder1 = f1 as Finder<Location>
        particle = ParticleManager.readParticle(config.getString("Particle"))

    }

}

class ParticleCircle : Motion("ParticleCircle") {
    lateinit var finder: Finder<Location>
    lateinit var particle: Particle
    lateinit var r: Expression
    lateinit var p: Expression
    override fun cast(ci: CastInfo) {
        val pp = ci.caster
        val loc = finder(ci).firstOrNull() ?: return
        val r = r(pp).toDouble()
        val p = this.p(pp).toDouble()
        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
            var st = 0.0
            val add = Math.PI / p
            while (st <= Math.PI * 2) {
                val x = cos(st) * r
                val z = sin(st) * r
                val loc = loc.clone().add(x, 0.0, z)
                particle.play(loc)
                st += add
            }
        }
    }

    override fun loadConfig(config: ConfigurationSection) {
        val (f0, t0) = FinderManager.readFinder(config.getString("Finder"))
        if (t0 !is LocationFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Location")
        }
        finder = f0 as Finder<Location>
        particle = ParticleManager.readParticle(config.getString("Particle"))
        r = ExpressionHelper.compileExpression(config.getString("r"))
        p = ExpressionHelper.compileExpression(config.getString("p"))
    }

}


class ParticleOval : Motion("ParticleOval") {
    lateinit var finder: Finder<Location>
    lateinit var particle: Particle
    lateinit var r: Expression
    lateinit var p: Expression
    lateinit var h: Expression
    lateinit var speed: Expression
    lateinit var time: Expression
    override fun cast(ci: CastInfo) {
        val pp = ci.caster
        val r = r(pp).toDouble()
        val p = this.p(pp).toDouble()
        val h = h(pp).toDouble()
        val speed = speed(pp).toDouble()
        val time = time(pp).toInt()
        object : BukkitRunnable() {
            var spawn = 0
            var angle = 0.0
            val add = Math.PI / p
            override fun run() {
                if (spawn >= time) {
                    this.cancel()
                    return
                }
                val loc = finder(ci).firstOrNull() ?: return
                var st = 0.0
                while (st <= Math.PI * 2) {
                    val x = cos(st) * r
                    val z = sin(st) * r

                    val loc = loc.clone().add(x, y(st), z)
                    particle.play(loc)
                    st += add
                }
                angle += speed
                angle %= (Math.PI * 2)
                spawn += 10
            }

            private fun y(st: Double): Double {
                val sub1 = abs(st - angle)
                val sub2 = abs(st - (angle + Math.PI * 2))
                return (h / Math.PI) * (Math.PI - min(sub1, sub2))
            }

        }.runTaskTimerAsynchronously(Main.Plugin, 10, 10)
    }

    override fun loadConfig(config: ConfigurationSection) {
        val (f0, t0) = FinderManager.readFinder(config.getString("Finder"))
        if (t0 !is LocationFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Location")
        }
        finder = f0 as Finder<Location>
        particle = ParticleManager.readParticle(config.getString("Particle"))
        r = ExpressionHelper.compileExpression(config.getString("r"))
        p = ExpressionHelper.compileExpression(config.getString("p"))
        h = ExpressionHelper.compileExpression(config.getString("h"))
        speed = ExpressionHelper.compileExpression(config.getString("speed"))
        time = ExpressionHelper.compileExpression(config.getString("time"))
    }

}