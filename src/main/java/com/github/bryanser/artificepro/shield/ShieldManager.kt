package com.github.bryanser.artificepro.shield

import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.tools.ParticleEffect
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import java.util.*

object ShieldManager : Listener, Runnable {
    val shieldInfos = mutableMapOf<UUID, MutableList<ShieldInfo>>()

    var time = 0

    init {
        Bukkit.getScheduler().runTaskTimer(Main.Plugin, this, 2, 2)
    }

    override fun run() {
        time++
        time %= 10
        val it = shieldInfos.entries.iterator()
        while (it.hasNext()) {
            val (uid, silist) = it.next()
            val e = Bukkit.getEntity(uid)
            if (e == null || e.isDead) {
                it.remove()
            } else {
                silist.playerEffect(e)
            }
        }

    }

    fun MutableList<ShieldInfo>.playerEffect(e: Entity) {
        if(this.isEmpty()){
            return
        }
        var r = 0
        var g = 0
        var b = 0
        val it = this.iterator()
        while (it.hasNext()) {
            val si = it.next()
            if (System.currentTimeMillis() > si.endTime) {
                it.remove()
            } else {
                r += si.color.red
                g += si.color.green
                b += si.color.blue
            }
        }
        val s = this.size
        if(s == 0){
            return
        }
        r /= s
        g /= s
        b /= s

        var y = 0f
        var i = time
        if (i >= 5) {
            i -= 5
            y = 2f - i * 0.4f
        } else {
            y = i * 0.4f
        }
        val color = ParticleEffect.OrdinaryColor(r, g, b)
        val tloc  = e.location.clone()
        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
            var st = 0.0
            while (st <= Math.PI * 2.0) {
                val loc = tloc.clone()
                loc.setY(loc.getY() + y)
                loc.setX(loc.getX() + Math.cos(st))
                loc.setZ(loc.getZ() + Math.sin(st))
                ParticleEffect.REDSTONE.display(color, loc, 100.0)
                st += Math.PI / 12.0
            }
        }
    }

    fun MutableList<ShieldInfo>.shield(dmg: Double): Double {
        var dmg = dmg
        val it = this.listIterator()
        while (it.hasNext()) {
            if (dmg <= 0) {
                break
            }
            val si = it.next()
            if (System.currentTimeMillis() > si.endTime) {
                it.remove()
            } else {
                if (dmg > si.shield) {
                    dmg -= si.shield
                    it.remove()
                } else {
                    si.shield -= dmg
                    dmg = 0.0
                }
            }

        }
        return dmg
    }

    fun getShieldInfo(p: LivingEntity): MutableList<ShieldInfo> = shieldInfos.getOrPut(p.uniqueId) {
        mutableListOf()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDamage(evt: EntityDamageEvent) {
        val si = getShieldInfo(evt.entity as? LivingEntity ?: return)
        evt.damage = si.shield(evt.damage)
    }
}