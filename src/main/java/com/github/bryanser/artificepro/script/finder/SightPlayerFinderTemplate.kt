package com.github.bryanser.artificepro.script.finder

import com.github.bryanser.artificepro.Tools
import org.bukkit.entity.Player

/*
 * 参数: 最远搜索长度
*/
object SightPlayerFinderTemplate : PlayerFinderTemplate("SightPlayer") {
    override fun read(args: Array<String>): Finder<Player> {
        val range = args[0].toDouble()
        return {
            val t = Tools.getLookAtEntity(it, range, range.toInt() * 2) {
                it is Player
            } as? Player
            if (t != null) {
                listOf(t)
            } else {
                listOf()
            }
        }
    }
}