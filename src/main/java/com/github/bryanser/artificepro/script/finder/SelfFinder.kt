package com.github.bryanser.artificepro.script.finder

import org.bukkit.entity.Player

/*
 * 无参数
 */
object SelfFinder : PlayerFinderTemplate("Self") {
    override fun read(args: Array<String>): Finder<Player> {
        return Finder {
            listOf(it)
        }
    }

}