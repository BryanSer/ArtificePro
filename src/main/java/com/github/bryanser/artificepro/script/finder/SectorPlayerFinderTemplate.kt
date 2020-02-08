package com.github.bryanser.artificepro.script.finder

import org.bukkit.entity.Player

object SectorPlayerFinderTemplate : PlayerFinderTemplate("SectorPlayer") {
    override fun read(args: Array<String>): Finder<Player> {
        val r = args[0].toDouble()
        val angle = Math.toRadians(args[1].toDouble()) / 2
        val self = args.getOrNull(3)?.toBoolean() ?: false
        return Finder{
            val pd = it.location.direction
            pd.y = 0.0
            val list = mutableListOf<Player>()
            if(self && it is Player){
                list += it
            }
            for (e in it.getNearbyEntities(r, r, r)) {
                if (e !is Player) continue
                val vec = e.location.toVector().subtract(it.location.toVector()).normalize()
                vec.y = 0.0
                if(e === it){
                    continue
                }
                if (pd.angle(vec) <= angle) {
                    list += e
                }
            }
            list
        }
    }
}