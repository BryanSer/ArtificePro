package com.github.bryanser.artificepro.motion.impl

import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.motion.Motion
import com.github.bryanser.artificepro.motion.ProjectileType
import com.github.bryanser.artificepro.motion.motionDamage
import com.github.bryanser.artificepro.particle.Particle
import com.github.bryanser.artificepro.particle.ParticleManager
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.script.FinderManager
import com.github.bryanser.artificepro.script.finder.EntityFinderTemplate
import com.github.bryanser.artificepro.script.finder.Finder
import com.github.bryanser.artificepro.skill.SkillManager
import com.github.bryanser.artificepro.tools.Tools
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

const val METADATA_KEY: String = "artificepro_projectile_damage"
const val METADATA_UUID: String = "artificepro_projectile_uuid"

class Launch : Motion("Launch") {

    lateinit var damage: Expression
    var gravity: Double = 0.0
    var speed: Expression? = null
    var projectileType = ProjectileType.ARROW

    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val a = p.launchProjectile(projectileType.clazz)
        a.setMetadata(METADATA_UUID, FixedMetadataValue(Main.Plugin, ci.castId.toString()))
        a.setMetadata(METADATA_KEY, FixedMetadataValue(Main.Plugin, damage(p).toDouble()))
        val speed = speed?.invoke(p)?.toDouble() ?: -1.0
        object : BukkitRunnable() {
            val initY = a.location.direction.y
            override fun run() {
                if (!a.isValid || a.isDead) {
                    this.cancel()
                    return
                }
                if (gravity == 0.0) {
                    val vec = a.velocity
                    vec.y = initY
                    if (speed > 0)
                        vec.normalize().multiply(speed)
                    a.velocity = vec
                    return
                }
                val vec = a.velocity
                vec.y -= gravity
                if (speed > 0)
                    vec.normalize().multiply(speed)
                a.velocity = vec
            }
        }.runTaskTimer(Main.Plugin, 1, 1)
    }

    override fun loadConfig(config: ConfigurationSection) {
        damage = ExpressionHelper.compileExpression(config.getString("damage"))
        if (config.contains("speed"))
            speed = ExpressionHelper.compileExpression(config.getString("speed"))
        projectileType = ProjectileType.valueOf(config.getString("ProjectileType", "ARROW"))
        gravity = config.getDouble("gravity", 0.0)
    }

}

class LaunchGuided : Motion("LaunchGuided") {

    lateinit var damage: Expression
    lateinit var time: Expression
    lateinit var radius: Expression
    lateinit var speed: Expression
    var projectileType = ProjectileType.ARROW

    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val a = p.launchProjectile(projectileType.clazz)
        a.setMetadata(METADATA_UUID, FixedMetadataValue(Main.Plugin, ci.castId.toString()))
        a.setMetadata(METADATA_KEY, FixedMetadataValue(Main.Plugin, damage(p).toDouble()))
        val maxTime = time(p).toInt() / 2
        val r = radius(p).toDouble()
        val speed = speed(p).toDouble()
        object : BukkitRunnable() {
            var time = 0
            override fun run() {
                if (time++ >= maxTime) {
                    this.cancel()
                    return
                }
                if (!a.isValid || a.isDead) {
                    this.cancel()
                    return
                }
                val loc = a.location
                val dir = loc.direction
                var maxDis = Double.MAX_VALUE
                var target: LivingEntity? = null
                for (e in Tools.sectorSearch(loc, dir, r) { it != p }) {
                    val dis = e.location.distanceSquared(loc)
                    if (dis < maxDis) {
                        target = e
                        maxDis = dis
                    }
                }
                if (target != null) {
                    val vec = target.location.toVector().subtract(loc.toVector()).normalize()
                    vec.multiply(speed)
                    a.velocity = vec
                }
            }
        }.runTaskTimer(Main.Plugin, 2, 2)
    }

    override fun loadConfig(config: ConfigurationSection) {
        damage = ExpressionHelper.compileExpression(config.getString("damage"))
        time = ExpressionHelper.compileExpression(config.getString("time"))
        radius = ExpressionHelper.compileExpression(config.getString("radius"))
        speed = ExpressionHelper.compileExpression(config.getString("speed", "1.0"))
        projectileType = ProjectileType.valueOf(config.getString("ProjectileType", "ARROW"))
    }

}

class Scattering : Motion(
        "Scattering"
) {

    lateinit var damage: Expression
    lateinit var amount: Expression
    var projectileType = ProjectileType.ARROW
    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val v = p.location.direction
        val amount = amount(p).toInt()
        var i = 0
        while (i < amount) {
            val a = p.launchProjectile(projectileType.clazz, randomVector(v))
            a.setMetadata(METADATA_UUID, FixedMetadataValue(Main.Plugin, ci.castId.toString()))
            a.setMetadata(METADATA_KEY, FixedMetadataValue(Main.Plugin, damage(p).toDouble()))
            i++
        }
    }

    override fun loadConfig(config: ConfigurationSection) {
        damage = ExpressionHelper.compileExpression(config.getString("damage"))
        amount = ExpressionHelper.compileExpression(config.getString("amount"))
        projectileType = ProjectileType.valueOf(config.getString("ProjectileType", "ARROW"))
    }

    companion object : Listener {

        init {
            Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun onHit(evt: EntityDamageByEntityEvent) {
            val a = evt.damager as? Projectile ?: return
            if (a.hasMetadata(METADATA_KEY)) {
                val dmg = a.getMetadata(METADATA_KEY).first().asDouble()
                val uuid = UUID.fromString(a.getMetadata(METADATA_UUID).first().asString())
                evt.damage = dmg
                val e = evt.entity as? LivingEntity ?: return
                val p = SkillManager.castingSkill[uuid]?.player ?: return
                evt.damage = 0.0
                e.motionDamage(dmg, p, uuid)
            }
        }

        @EventHandler
        fun onFire(evt: BlockIgniteEvent) {
            if (evt.ignitingEntity != null){
                if(evt.ignitingEntity.hasMetadata(METADATA_KEY)){
                    evt.isCancelled = true
                }
            }

        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
        fun onPick(evt: PlayerPickupArrowEvent) {
            if (evt.arrow.hasMetadata(METADATA_KEY)) {
                evt.isCancelled = true
                evt.arrow.remove()
            }
        }

        @EventHandler
        fun onHit(evt: ProjectileHitEvent) {
            if (evt.entity.hasMetadata(METADATA_KEY)) {
                Bukkit.getScheduler().runTaskLater(Main.Plugin, {
                    evt.entity.remove()
                }, 1)
            }
        }

        private fun randomVector(v: Vector): Vector {
            val v = v.clone()
            val r = Vector(v.getX() + random(v.getX()), v.getY() + random(v.getY()), v.getZ() + random(v.getZ()))
            r.multiply(1.0 / r.length())
            r.multiply(v.length())
            return r
        }

        val ran = Random()

        private fun random(d: Double): Double {
            return (if (ran.nextBoolean()) 1 else -1) * ran.nextDouble() / 2.0
        }
    }

}

class GuidedArrow : Motion("GuidedArrow") {
    lateinit var damage: Expression
    lateinit var time: Expression
    lateinit var finder: Finder<LivingEntity>
    var projectileType = ProjectileType.ARROW
    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val maxTime = time(p).toLong()
        val dmg = damage(p).toDouble()
        for (target in finder(ci)) {
            val arrow = p.launchProjectile(
                    projectileType.clazz,
                    target.eyeLocation.subtract(p.location).toVector().normalize()
            )
            arrow.setMetadata(METADATA_UUID, FixedMetadataValue(Main.Plugin, ci.castId.toString()))
            arrow.setMetadata(METADATA_KEY, FixedMetadataValue(Main.Plugin, dmg))
            object : BukkitRunnable() {
                var time = 0L
                override fun run() {
                    if (time++ > maxTime || arrow.isValid || arrow.isDead) {
                        this.cancel()
                        arrow.remove()
                        return
                    }
                    arrow.velocity = target.eyeLocation.toVector().subtract(arrow.location.toVector()).normalize()
                }
            }
        }
    }


    override fun loadConfig(config: ConfigurationSection) {
        damage = ExpressionHelper.compileExpression(config.getString("damage"))
        time = ExpressionHelper.compileExpression(config.getString("time"))
        projectileType = ProjectileType.valueOf(config.getString("ProjectileType", "ARROW"))
        val (f, t) = FinderManager.readFinder(config.getString("Finder"))
        if (t !is EntityFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Entity")
        }
        finder = f as Finder<LivingEntity>

    }

}

class LaunchRain : Motion("LaunchRain") {
    lateinit var damage: Expression
    lateinit var amount: Expression
    lateinit var totalAmount: Expression
    lateinit var radius: Expression
    lateinit var range: Expression
    var projectileType = ProjectileType.ARROW
    lateinit var particle: Particle

    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val dmg = damage(p).toDouble()
        val amt = amount(p).toInt()
        val tamt = totalAmount(p).toInt()
        val r = radius(p).toDouble()
        val rng = range(p).toInt()
        val target = p.getTargetBlock(setOf(Material.AIR), rng)?.location ?: return
        object : BukkitRunnable() {
            var launched = 0
            override fun run() {
                if (launched >= tamt) {
                    this.cancel()
                    return
                }
                repeat(amt) {
                    val loc = randomLocation(target, r)
                    loc.world.spawn(loc, projectileType.clazz) {
                        it.setMetadata(METADATA_UUID, FixedMetadataValue(Main.Plugin, ci.castId.toString()))
                        it.setMetadata(METADATA_KEY, FixedMetadataValue(Main.Plugin, dmg))
                        it.velocity = Vector(0.0, -1.2, 0.0)
                    }
                }
                val pp = 12
                val loc = target
                Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
                    var st = 0.0
                    val add = Math.PI / pp
                    while (st <= Math.PI * 2) {
                        val x = cos(st) * r
                        val z = sin(st) * r
                        val loc = loc.clone().add(x, 0.0, z)
                        particle.play(loc)
                        st += add
                    }
                }
                launched += amt
            }
        }.runTaskTimer(Main.Plugin, 10, 10)
    }

    override fun loadConfig(config: ConfigurationSection) {
        damage = ExpressionHelper.compileExpression(config.getString("damage"))
        amount = ExpressionHelper.compileExpression(config.getString("amount"))
        totalAmount = ExpressionHelper.compileExpression(config.getString("totalAmount"))
        radius = ExpressionHelper.compileExpression(config.getString("radius"))
        range = ExpressionHelper.compileExpression(config.getString("range"))
        projectileType = ProjectileType.valueOf(config.getString("ProjectileType", "ARROW"))
        particle = ParticleManager.readParticle(config.getString("Particle","ColorDust(255,0,0)"))
    }

    companion object {
        fun randomLocation(center: Location, r: Double): Location {
            val st = Math.random() * Math.PI * 2
            val p = Math.random() * r
            val ox = cos(st) * p
            val oz = sin(st) * p
            return center.clone().add(ox, 10.0, oz)
        }
    }

}