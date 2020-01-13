package com.github.bryanser.artificepro.motion.impl

import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.motion.Motion
import com.github.bryanser.artificepro.motion.trigger.LaunchItemTrigger
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.skill.SkillManager
import com.github.bryanser.brapi.ItemBuilder
import com.github.bryanser.brapi.Utils
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

class LaunchItem : Motion("LaunchItem") {

    lateinit var item: ItemStack
    lateinit var speed: Expression
    lateinit var maxLength: Expression
    lateinit var hitRange: Expression
    lateinit var key: String
    var type: Int = 0
    lateinit var destroyOnHit: Expression

    override fun cast(ci: CastInfo) {
        val vec = ci.caster.location.direction
        val left = Utils.getLeft(vec)
        val offset: (loc: Location, left: Vector, rev: Boolean) -> Location = if (type == 0) type0Offset else type1Offset
        val tar = ci.caster.world.spawn(offset(ci.caster.location, left, false), ArmorStand::class.java) {
            it.isMarker = true
            it.isVisible = false
            it.setGravity(false)
            it.itemInHand = item.clone()
            if (type == 0) {
                it.rightArmPose = EulerAngle(-Math.PI / 6, 0.0, 0.0)
            } else {
                it.rightArmPose = EulerAngle(-Math.PI / 18, 0.0, Math.PI / 2)
            }
        }
        val max = this.maxLength(ci.caster).toDouble()
        val sp = speed(ci.caster).toDouble()
        val hr = hitRange(ci.caster).toDouble()
        val destroy = destroyOnHit(ci.caster).toBoolean()
        object : BukkitRunnable() {
            val from = tar.location

            override fun cancel() {
                super.cancel()
                tar.remove()
            }

            override fun run() {
                val loc = tar.location
                if (loc.distanceSquared2(from) > max) {
                    this.cancel()
                    return
                }
                val t = offset(loc, left, true)
                val cd = SkillManager.castingSkill[ci.castId]
                if (cd != null) {
                    for (e in t.world.getNearbyEntities(t, hr, hr, hr)) {
                        if (e == ci.caster) {
                            continue
                        }
                        if (e is LivingEntity) {
                            for (t in cd.triggers) {
                                if (t is LaunchItemTrigger) {
                                    if (t.key == this@LaunchItem.key) {
                                        t.onTrigger(e, ci.caster, ci.castId)
                                        if (destroy) {
                                            this.cancel()
                                            return
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                tar.teleport(loc.add(vec.multiply(sp)))
            }
        }.runTaskTimer(Main.Plugin, 1, 1)
    }

    companion object {
        val type0Offset: (loc: Location, left: Vector, rev: Boolean) -> Location = { it, left, rev ->
            if (rev) {
                it.clone().add(left.clone().multiply(-0.37)).add(0.0, 1.17, 0.0)
            } else {
                it.clone().add(left.clone().multiply(0.37)).add(0.0, 0.34, 0.0)
            }
        }//-30 0 0  deg
        val type1Offset: (loc: Location, left: Vector, rev: Boolean) -> Location = { it, left, rev ->
            if (rev) {
                it.clone().add(left.clone().multiply(-0.84)).add(0.0, 1.4, 0.0)
            } else {
                it.clone().add(left.clone().multiply(0.84)).add(0.0, 0.1, 0.0)
            }
        }//-10 0 90 deg
    }

    override fun loadConfig(config: ConfigurationSection) {
        val str = config.getString("item").split(":")
        str.run {
            val id = this[0].toInt()
            val dur = this.getOrNull(1)?.toShort() ?: 0
            val lore = this.getOrNull(2)?.let { ChatColor.translateAlternateColorCodes('&', it) }
            item = ItemBuilder.createItem(Material.getMaterial(id), durability = 0) {
                lore?.apply {
                    lore(this)
                }
            }
        }
        speed = ExpressionHelper.compileExpression(config.getString("speed"))
        maxLength = ExpressionHelper.compileExpression(config.getString("maxLength"))
        hitRange = ExpressionHelper.compileExpression(config.getString("hitRange"))
        key = config.getString("key")
        type = config.getInt("type", 0)
        destroyOnHit = ExpressionHelper.compileExpression(config.getString("destroyOnHit", "false"), true)
    }
}