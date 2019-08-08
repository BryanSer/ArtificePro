package com.github.bryanser.artificepro

import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

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
}