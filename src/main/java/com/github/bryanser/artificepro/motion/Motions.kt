package com.github.bryanser.artificepro.motion

import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.script.*
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.Vector
import java.util.*

class Scattering : Motion(
        "Scattering"
) {
    lateinit var damage: Expression
    lateinit var amount: Expression
    override fun cast(p: Player): Boolean {
        val v = p.location.direction
        val amount = amount(p).toInt()
        var i = 0
        while (i++ < amount) {
            val a = p.launchProjectile(Arrow::class.java, randomVector(v))
            a.setMetadata(METADATA_KEY,FixedMetadataValue(Main.Plugin,damage(p).toDouble()))
            i++
        }
        return true
    }

    override fun loadConfig(config: ConfigurationSection?) {
        if (config != null) {
            damage = ExpressionHelper.compileExpression(config.getString("damage"))
            amount = ExpressionHelper.compileExpression(config.getString("amount"))
        } else {
            throw IllegalArgumentException("配置编写错误 缺少配置数据")
        }
    }

    companion object : Listener {
        const val METADATA_KEY: String = "artificepro_motion_scattering"

        init {
            Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun onHit(evt:EntityDamageByEntityEvent){
            val a= evt.damager as? Arrow ?: return
            if(a.hasMetadata(METADATA_KEY)){
                val dmg = a.getMetadata(METADATA_KEY).first().asDouble()
                evt.damage = dmg
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
        fun onPick(evt:PlayerPickupArrowEvent){
            if(evt.arrow.hasMetadata(METADATA_KEY)){
                evt.isCancelled = true
                evt.arrow.remove()
            }
        }

        private fun randomVector(v: Vector): Vector {
            val v = v.clone()
            val r = Vector(v.getX() + this.random(v.getX()), v.getY() + this.random(v.getY()), v.getZ() + this.random(v.getZ()))
            r.multiply(1.0 / r.length())
            r.multiply(v.length())
            return r
        }

        val ran = Random()

        private fun random(d: Double): Double {
            return (if (this.ran.nextBoolean()) 1 else -1) * this.ran.nextDouble() / 2.0
        }
    }

}