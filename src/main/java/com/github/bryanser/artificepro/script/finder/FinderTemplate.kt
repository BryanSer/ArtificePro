package com.github.bryanser.artificepro.script.finder

import com.github.bryanser.artificepro.motion.CastInfo
import net.citizensnpcs.api.CitizensAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player


val enableCitizensAPI: Boolean by lazy {
    Bukkit.getPluginManager().getPlugin("Citizens") != null
}

fun isCitizens(e: Entity): Boolean {
    if (enableCitizensAPI) {
        for (r in CitizensAPI.getNPCRegistries()) {
            val npc = r.getNPC(e)
            if (npc != null) {
                return true
            }
        }
    }
    return false
}

abstract class FinderTemplate<out F : Any>(
        val name: String
) {
    abstract fun read(args: Array<String>): Finder<F>
}


interface Finder<out F> {
    fun finder(p:LivingEntity): Collection<F>
    operator fun invoke(p: CastInfo): Collection<F>
}

fun <F> Finder(f: (LivingEntity) -> Collection<F>): Finder<F> {
    return object : Finder<F> {
        override fun finder(p: LivingEntity): Collection<F> {
            return f(p)
        }

        override fun invoke(p: CastInfo): Collection<F> {
            if (enableCitizensAPI) {
                return f(p.finder()).filter { t ->
                    if (t is Entity)
                        if (isCitizens(t)) {
                            return@filter false
                        }
                    true
                }
            }
            return f(p.finder())
        }

    }
}


abstract class LocationFinderTemplate(name: String) : FinderTemplate<Location>(name)


abstract class EntityFinderTemplate<out T : LivingEntity>(name: String) : FinderTemplate<T>(name)
abstract class PlayerFinderTemplate(name: String) : EntityFinderTemplate<Player>(name)