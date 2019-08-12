package com.github.bryanser.artificepro.script.finder

import com.github.bryanser.artificepro.script.FinderManager
import net.citizensnpcs.api.CitizensAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player


val enableCitizensAPI: Boolean by lazy {
    Bukkit.getPluginManager().getPlugin("Citizens") != null
}

abstract class FinderTemplate<out F : Any>(
        val name: String
) {
    abstract fun read(args: Array<String>): Finder<F>
}

data class Finder<out F>(
        val finder: (Player) -> Collection<F>
) {
    operator fun invoke(p: Player): Collection<F> {
        if (enableCitizensAPI) {
            return finder(p).filter { t ->
                if (t is Entity)
                    for (r in CitizensAPI.getNPCRegistries()) {
                        val npc = r.getNPC(t)
                        if (npc != null) {
                            return@filter false
                        }
                    }
                true
            }
        }
        return finder(p)

    }

}


abstract class EntityFinderTemplate<out T : LivingEntity>(name: String) : FinderTemplate<T>(name)
abstract class PlayerFinderTemplate(name: String) : EntityFinderTemplate<Player>(name)