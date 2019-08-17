package com.github.bryanser.artificepro.script.finder

import org.bukkit.Location
import org.bukkit.Material

object SightLocationFinderTemplate : LocationFinderTemplate("SightLocation") {
    val transient = mutableSetOf(Material.AIR)
    override fun read(args: Array<String>): Finder<Location> {
        val range = args[0].toDouble()
        return Finder {
            listOf(it.getTargetBlock(transient, range.toInt()).location)
        }
    }
}