package com.github.bryanser.artificepro.motion

import com.github.bryanser.artificepro.Main
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
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

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
        particle = ParticleManager.readParticleManager(config.getString("Particle"))

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
        particle = ParticleManager.readParticleManager(config.getString("Particle"))
        r = ExpressionHelper.compileExpression(config.getString("r"))
        p = ExpressionHelper.compileExpression(config.getString("p"))
    }

}