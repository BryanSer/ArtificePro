package com.github.bryanser.artificepro.motion.impl

import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.motion.Motion
import com.github.bryanser.artificepro.motion.trigger.LaunchItemTrigger
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.skill.SkillManager
import com.github.bryanser.brapi.Main
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SurroundItem : Motion("SurroundItem") {

    lateinit var item: ItemStack
    lateinit var speed: Expression
    lateinit var hitRange: Expression
    lateinit var key: String
    lateinit var destroyOnHit: Expression
    lateinit var radius: Expression
    lateinit var time: Expression
    lateinit var amount: Expression

    override fun cast(ci: CastInfo) {
        val speed = speed(ci.caster).toDouble() / 20
        val hitRange = hitRange(ci.caster).toDouble()
        val destroyOnHit = destroyOnHit(ci.caster).toBoolean()
        val radius = radius(ci.caster).toDouble()
        val time = time(ci.caster).toInt()
        val amount = amount(ci.caster).toInt()
        val diff = PI * 2 / amount
        object : BukkitRunnable() {
            var rtime = time
            var angle = 0.0

            var count = 0

            fun proj(ang: Double): Location {
                val center = ci.caster.location
                val loc = center.clone().add(cos(ang) * radius, 0.0, sin(ang) * radius)
                val vec = loc.toVector().subtract(center.toVector()).normalize()
                loc.direction = vec
                return loc
            }

            val armorStand: Array<Pair<ArmorStand, MutableSet<Int>>?> = Array(amount) {
                val ang = it * diff
                val i = ci.caster.world.spawn(proj(ang), ArmorStand::class.java) {
                    it.isMarker = true
                    it.isVisible = false
                    it.setGravity(false)
                    it.itemInHand = item.clone()
                }
                i to hashSetOf<Int>()
            }

            fun clear() {
                for (v in armorStand) {
                    v?.second?.clear()
                }
            }

            override fun cancel() {
                super.cancel()
                for(p in armorStand){
                    p?.first?.remove()
                }
            }

            override fun run() {
                angle += speed
                if (angle > PI * 2) {
                    this.rtime--
                    if (this.rtime <= 0) {
                        this.cancel()
                        return
                    }
                    angle = 0.0
                    clear()
                }
                val cd = SkillManager.castingSkill[ci.castId]
                T@ for ((it, p) in armorStand.withIndex()) {
                    val (a, s) = p ?: continue
                    val ang = it * diff + angle
                    a.teleport(proj(ang))
                    if (cd != null && count++ % 3 == 0) {
                        for (e in a.getNearbyEntities(hitRange, hitRange, hitRange)) {
                            if (e !is LivingEntity || e == ci.caster) {
                                continue
                            }
                            if (!s.add(e.entityId)) {
                                continue
                            }
                            for (t in cd.triggers) {
                                if (t is LaunchItemTrigger && t.key == key) {
                                    t.onTrigger(e, ci.caster, ci.castId)
                                    if (destroyOnHit) {
                                        a.remove()
                                        armorStand[it] = null
                                        continue@T
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(), 1, 1)

    }


    override fun loadConfig(config: ConfigurationSection) {
        val str = config.getString("item").split(":")
        item = LaunchItem.loadItem(str)
        speed = ExpressionHelper.compileExpression(config.getString("speed"))
        hitRange = ExpressionHelper.compileExpression(config.getString("hitRange"))
        key = config.getString("key")
        destroyOnHit = ExpressionHelper.compileExpression(config.getString("destroyOnHit", "false"), true)
        radius = ExpressionHelper.compileExpression(config.getString("radius"))
        time = ExpressionHelper.compileExpression(config.getString("time"))
        amount = ExpressionHelper.compileExpression(config.getString("amount"))
    }
}