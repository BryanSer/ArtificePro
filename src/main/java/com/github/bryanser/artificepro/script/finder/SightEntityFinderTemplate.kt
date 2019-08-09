package com.github.bryanser.artificepro.script.finder

import com.github.bryanser.artificepro.Tools
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/*
 * 参数: 最远搜索长度, 是否选择玩家
*/
object SightEntityFinderTemplate : EntityFinderTemplate<LivingEntity>("SightEntity") {
    override fun read(args: Array<String>): Finder<LivingEntity> {
        val range = args[0].toDouble()
        val player = args[1].toBoolean()
        return if (player) {
            {
                val t = Tools.getLookAtEntity(it, range, range.toInt() * 2)
                if (t != null) {
                    listOf(t)
                } else {
                    listOf()
                }
            }
        } else {
            {
                val t = Tools.getLookAtEntity(it, range, range.toInt() * 2) {
                    it is LivingEntity && it !is Player
                }
                if (t != null) {
                    listOf(t)
                } else {
                    listOf()
                }
            }
        }
    }
}