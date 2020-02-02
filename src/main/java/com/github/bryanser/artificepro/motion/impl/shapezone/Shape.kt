package com.github.bryanser.artificepro.motion.impl.shapezone

import com.github.bryanser.artificepro.motion.impl.distanceSquared2
import com.github.bryanser.artificepro.particle.Particle
import com.github.bryanser.artificepro.particle.ParticleManager
import com.github.bryanser.brapi.Utils
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Entity
import org.bukkit.util.Vector

class Shape(config: ConfigurationSection) {
    data class Point(
            val offset: Vector,
            val char: Char,
            val particle: Particle?,
            val damage: Boolean
    )

    val points = mutableListOf<Point>()
    var maxLength: Double = 0.0
    val size: Double
    init {
        val particle = config.getConfigurationSection("particle")?.let {
            val map = mutableMapOf<Char, Particle>()
            for (key in it.getKeys(false)) {
                map[key[0]] = ParticleManager.readParticle(it.getString(key))
            }
            map
        } ?: mapOf<Char, Particle>()
        val damage = config.getStringList("damage").map { it[0] }
        size = config.getDouble("size")
        val center: Vector
        val shape = config.getString("shape").split("\n").let { list ->
            Array(list.size) {
                list[it].toCharArray()
            }
        }
        run {
            for ((oy, ca) in shape.withIndex()) {
                val y = shape.size - oy - 1
                for ((x, c) in ca.withIndex()) {
                    if (c == 'O') {
                        center = Vector(x, y, 0)
                        return@run
                    }
                }
            }
            throw IllegalStateException("Shape未定义原点")
        }
        for ((y, ca) in shape.withIndex()) {
            val y = shape.size - y - 1
            for ((x, c) in ca.withIndex()) {
                val offset = Vector(x, y, 0).subtract(center)
                offset.multiply(size)
                val l = offset.length()
                if (l > maxLength) {
                    maxLength = l
                }
                points.add(Point(
                        offset,
                        c,
                        particle[c],
                        damage.contains(c)
                ))
            }
        }
    }
    val squaredSize = size * size

    fun project(loc:Location):(Vector)->Location {
        val vec = loc.direction
        vec.y = 0.0
        vec.normalize()
        val right = Utils.getRight(vec)
        val loc = loc.clone()
        return {
            loc.clone().add(vec.clone().multiply(it.y)).add(right.clone().multiply(it.x))
        }
    }

    fun playEffect(loc: Location) {
        val proj = project(loc)
        for ((off, _, p, _) in points) {
            p?.play(proj(off))
        }
    }

    fun getDamageZoneEntities(loc: Location): List<Entity> {
        val proj = project(loc)
        val locations = points.map {
            proj(it.offset)
        }
        val list = mutableListOf<Entity>()
        for (e in loc.world.getNearbyEntities(loc, maxLength, maxLength, maxLength)) {
            val tloc = e.location
            for (p in locations) {
                if (p.distanceSquared2(tloc) < squaredSize) {
                    list += e
                }
            }
        }
        return list
    }

}