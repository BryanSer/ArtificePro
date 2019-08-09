package com.github.bryanser.artificepro.script.finder

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/*
 * 参数: 搜寻半径, 搜寻数量(0表示无限), 是否搜索玩家, 是否包括自身
 */
object RangeEntityFinderTemplate : EntityFinderTemplate<LivingEntity>("RangeEntity") {
    val filters = arrayOf<(LivingEntity, Player) -> Boolean>(
            { e, c ->
                e !is Player && e != c
            },
            { e, c ->
                e is Player && e != c
            },
            { e, c ->
                e !is Player || e == c
            },
            { e, c ->
                e is Player || e != c
            }
    )

    override fun read(args: Array<String>): Finder<LivingEntity> {
        val r = args[0].toDouble()
        val max = args[1].toInt()
        val player = if (args[2].toBoolean()) 1 else 0
        val self = if (args[3].toBoolean()) 2 else 0
        val filter = filters[player + self]
        return { p ->
            val list = mutableListOf<LivingEntity>()
            var count = 0
            for (e in p.getNearbyEntities(r, r, r)) {
                if (max > 0 && count >= max) {
                    break
                }
                if (e !is LivingEntity) {
                    continue
                }
                if (filter(e, p)) {
                    list += e
                    count++
                }
            }
            list
        }
    }

}