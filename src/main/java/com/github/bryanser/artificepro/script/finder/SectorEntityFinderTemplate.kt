package com.github.bryanser.artificepro.script.finder

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

object SectorEntityFinderTemplate : EntityFinderTemplate<LivingEntity>("SectorEntity") {
    override fun read(args: Array<String>): Finder<LivingEntity> {
        val r = args[0].toDouble()
        val angle = Math.toRadians(args[1].toDouble()) / 2
        val player = args[2].toBoolean()
        return Finder{
            val pd = it.location.direction
            pd.y = 0.0
            val list = mutableListOf<LivingEntity>()
            for (e in it.getNearbyEntities(r, r, r)) {
                if (e !is LivingEntity) continue
                if(e is ArmorStand){
                    continue
                }
                if (!player && e is Player) continue
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
