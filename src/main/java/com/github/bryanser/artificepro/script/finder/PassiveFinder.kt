package com.github.bryanser.artificepro.script.finder

import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.passive.PassiveManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

object AttackTargetEntityTemplate : EntityFinderTemplate<LivingEntity>("AttackTargetEntity") {
    override fun read(args: Array<String>): Finder<LivingEntity> {
        return object : Finder<LivingEntity> {
            val cache = mutableMapOf<Int, LivingEntity>()
            override fun finder(p: LivingEntity): Collection<LivingEntity> {
                val target = cache[p.entityId] ?: return listOf()
                return listOf(target)
            }

            override fun invoke(p: CastInfo): Collection<LivingEntity> {
                val target = PassiveManager.attackEntity[p.castId] ?: return listOf()
                cache[p.caster.entityId] = target
                return listOf(target)
            }
        }
    }
}
object DefenceFromEntityTemplate : EntityFinderTemplate<LivingEntity>("DefenceFromEntityTemplate") {
    override fun read(args: Array<String>): Finder<LivingEntity> {
        return object : Finder<LivingEntity> {
            val cache = mutableMapOf<Int, LivingEntity>()
            override fun finder(p: LivingEntity): Collection<LivingEntity> {
                val target = cache[p.entityId] ?: return listOf()
                return listOf(target)
            }

            override fun invoke(p: CastInfo): Collection<LivingEntity> {
                val target = PassiveManager.defenceEntity[p.castId] ?: return listOf()
                cache[p.caster.entityId] = target
                return listOf(target)
            }
        }
    }
}

object AttackTargetPlayerTemplate : PlayerFinderTemplate("AttackTargetPlayer") {
    override fun read(args: Array<String>): Finder<Player> {
        return object : Finder<Player> {
            val cache = mutableMapOf<Int, Player>()
            override fun finder(p: LivingEntity): Collection<Player> {
                val target = cache[p.entityId] ?: return listOf()
                return listOf(target)
            }

            override fun invoke(p: CastInfo): Collection<Player> {
                val target = PassiveManager.attackEntity[p.castId] as? Player ?: return listOf()
                cache[p.caster.entityId] = target
                return listOf(target)
            }
        }
    }
}

object DefenceFromPlayerTemplate : PlayerFinderTemplate("DefenceFromPlayer") {
    override fun read(args: Array<String>): Finder<Player> {
        return object : Finder<Player> {
            val cache = mutableMapOf<Int, Player>()
            override fun finder(p: LivingEntity): Collection<Player> {
                val target = cache[p.entityId] ?: return listOf()
                return listOf(target)
            }

            override fun invoke(p: CastInfo): Collection<Player> {
                val target = PassiveManager.defenceEntity[p.castId] as? Player ?: return listOf()
                cache[p.caster.entityId] = target
                return listOf(target)
            }
        }
    }
}