package com.github.bryanser.artificepro.script.finder

import com.github.bryanser.artificepro.script.FinderManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

typealias Finder<F> = (Player) -> Collection<F>

abstract class FinderTemplate<out F:Any>(
        val name: String
) {
    abstract fun read(args: Array<String>): Finder<F>
}

abstract class EntityFinderTemplate<out T : LivingEntity>(name: String) : FinderTemplate<T>(name)
abstract class PlayerFinderTemplate(name: String) : EntityFinderTemplate<Player>(name)