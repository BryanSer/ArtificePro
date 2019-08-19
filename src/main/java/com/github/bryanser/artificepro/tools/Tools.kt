package com.github.bryanser.artificepro.tools

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector
import kotlin.math.abs

object Tools {
    fun getLookAtEntity(e: LivingEntity, maxlength: Double, p: Int, filter: (Entity) -> Boolean = { it is LivingEntity }): LivingEntity? {
        val loc = e.eyeLocation
        val v = e.location.direction
        var l = maxlength / p
        while (l < maxlength) {
            val vd = v.clone().multiply(l)
            val nloc = loc.clone().add(vd)
            if (nloc.block.type != Material.AIR) {
                return null
            }
            for (eeee in nloc.world.getNearbyEntities(nloc, 0.25, 0.25, 0.25)) {
                if (eeee === e) {
                    continue
                }
                if (filter(eeee)) {
                    return eeee as LivingEntity
                }
            }
            l += maxlength / p
        }
        return null
    }

    fun sectorSearch(loc: Location, vec: Vector, range: Double, ignore: (LivingEntity) -> Boolean): List<LivingEntity> {
        val vec = vec.clone()
        vec.y = 0.0
        val list = mutableListOf<LivingEntity>()
        for(e in loc.world.getNearbyEntities(loc,range,range,range)){
            if(e !is LivingEntity){
                continue
            }
            if(ignore(e)){
                continue
            }
            val t = e.location.toVector().subtract(loc.toVector()).normalize()
            if(abs(vec.angle(t)) <=  Math.PI / 4){
                list += e
            }
        }
        return list
    }
}