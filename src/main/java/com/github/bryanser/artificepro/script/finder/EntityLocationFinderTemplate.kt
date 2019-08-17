package com.github.bryanser.artificepro.script.finder

import com.github.bryanser.artificepro.script.FinderManager
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

object EntityLocationFinderTemplate : LocationFinderTemplate("EntityLocation") {
    override fun read(args: Array<String>): Finder<Location> {
        val finder = FinderManager.readFinder(args[0])
        if (finder.template !is EntityFinderTemplate<*>) {
            throw IllegalArgumentException("查找器参数不正确: EntityLocation(EntityFinder)")
        }
        val f = finder.finder as Finder<LivingEntity>
        return Finder {
            f.finder(it).map(Entity::getLocation).toList()
        }
    }
}