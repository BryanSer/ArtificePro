package com.github.bryanser.artificepro.script.finder

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/*
 * 无参数
 */
object SelfFinder : PlayerFinderTemplate("Self") {
    override fun read(args: Array<String>): Finder<Player> {
        return Finder {
            if (it is Player)
                listOf(it)
            else listOf()
        }
    }

}

object SelfEntityFinder : EntityFinderTemplate<LivingEntity>("SelfEntity") {
    override fun read(args: Array<String>): Finder<LivingEntity> {
        return Finder { listOf(it) }
    }

}