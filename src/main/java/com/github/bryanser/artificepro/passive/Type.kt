package com.github.bryanser.artificepro.passive

import com.github.bryanser.artificepro.CastData
import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.motion.ignoreAttack
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.*

enum class Type : Listener {
    PERMANENT {
        override fun init() {
            Bukkit.getScheduler().runTaskTimer(Main.Plugin, {
                for (p in Bukkit.getOnlinePlayers()) {
                    this.cast(p, UUID.randomUUID())
                }
            }, 1, 1)
        }
    },
    ATTACK {
        @EventHandler
        fun onAttack(evt: EntityDamageByEntityEvent) {
            if (ignoreAttack.contains(evt.entity.entityId)) return
            if (ignoreAttack.contains(evt.damager.entityId)) return
            val damager = evt.damager as? Player ?: return
            val entity = evt.entity as? LivingEntity ?: return
            val uuid = UUID.randomUUID()
            PassiveManager.attackEntity[uuid] = entity
            this.cast(damager, uuid)
        }

        override fun init() {
            Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
        }

    },
    DEFENCE {
        @EventHandler
        fun onAttack(evt: EntityDamageByEntityEvent) {
            if (ignoreAttack.contains(evt.entity.entityId)) return
            if (ignoreAttack.contains(evt.damager.entityId)) return
            val damager = evt.damager as? LivingEntity ?: return
            val entity = evt.entity as? Player ?: return
            val uuid = UUID.randomUUID()
            PassiveManager.defenceEntity[uuid] = damager
            this.cast(entity, uuid)
        }

        override fun init() {
            Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
        }
    };

    fun cast(p: Player, uid: UUID) {
        val list = (PassiveManager.extraPassive[p.name] ?: return).filter {
            PassiveManager[it.key]?.type == this
        }
        for ((pass, lv) in list) {
            val data = CastData(p.name, uid, this != Type.PERMANENT)
            PassiveManager[pass]?.cast(p, data, lv)
        }
    }

    abstract fun init();
}