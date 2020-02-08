package com.github.bryanser.artificepro.script.finder

import net.citizensnpcs.nms.v1_10_R1.entity.nonliving.ItemController
import org.bukkit.entity.Player

/*
 * 参数: 搜寻半径, 搜寻数量(0表示无限), 是否包括自身
 */
object RangePlayerFinderTemplate : PlayerFinderTemplate("RangePlayer") {
    override fun read(args: Array<String>): Finder<Player> {
        val r = args[0].toDouble()
        val max = args[1].toInt()
        val self = args[2].toBoolean()
        return Finder{
            val list = mutableListOf<Player>()
            var count = 0
            if(self && it is Player){
                list += it
                count++
            }
            for (e in it.getNearbyEntities(r, r, r)) {
                if (max > 0 && count >= max) {
                    break
                }
                if (e !is Player) {
                    continue
                }
                if (!self) {
                    if (e === it) {
                        continue
                    }
                }
                list += e
                count++
            }
            list
        }

    }

}