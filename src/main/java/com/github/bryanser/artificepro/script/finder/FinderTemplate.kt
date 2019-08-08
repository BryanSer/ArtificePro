package com.github.bryanser.artificepro.script.finder

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

typealias Finder<F> = (Player) -> Collection<F>

abstract class FinderTemplate<out F>(
        val name: String
) {
    abstract fun read(args: Array<String>): Finder<F>
}

abstract class EntityFinder<out T : LivingEntity>(name: String) : FinderTemplate<T>(name)
abstract class PlayerFinder(name: String) : EntityFinder<Player>(name)