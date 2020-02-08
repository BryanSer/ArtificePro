package com.github.bryanser.artificepro.script.finder

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

object SectorEntityFinderTemplate : EntityFinderTemplate<LivingEntity>("SectorEntity") {
    override fun read(args: Array<String>): Finder<LivingEntity> {
        val r = args[0].toDouble()
        val angle = Math.toRadians(args[1].toDouble()) / 2
        val player = args[2].toBoolean()
        val self = args.getOrNull(3)?.toBoolean() ?: false
        return Finder {
            val pd = it.location.direction
            pd.y = 0.0
            val list = mutableListOf<LivingEntity>()
            if (self) {
                if (player && it is Player) {
                    list += it
                } else {
                    list += it
                }
            }
            for (e in it.getNearbyEntities(r, r, r)) {
                if (e !is LivingEntity) continue
                if (e is ArmorStand) {
                    continue
                }
                if (!player && e is Player) continue
                if (e == it) {
                    continue
                }
                val vec = e.location.toVector().subtract(it.location.toVector()).normalize()
                vec.y = 0.0
                if (pd.angle(vec) <= angle) {
                    list += e
                }
            }
            list
        }
    }
}
