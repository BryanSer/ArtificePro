package com.github.bryanser.artificepro.motion.impl

import Br.API.ParticleEffect.ParticleEffect
import Br.API.Utils
import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.motion.*
import com.github.bryanser.artificepro.motion.trigger.Trigger
import com.github.bryanser.artificepro.motion.trigger.TriggerManager
import com.github.bryanser.artificepro.particle.ColorDust
import com.github.bryanser.artificepro.particle.Particle
import com.github.bryanser.artificepro.particle.ParticleManager
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.script.finder.isCitizens
import com.github.bryanser.artificepro.skill.SkillManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class Charge : Motion("Charge") {
    lateinit var damage: Expression
    lateinit var length: Expression
    lateinit var knock: Expression
    lateinit var stop: Expression
    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val dmg = damage(p).toDouble()
        var lengthSq = length(p).toDouble()
        lengthSq *= lengthSq
        val knock = knock(p).toDouble()
        val stop = stop(p).toBoolean()
        val start = p.location.clone()
        val vec = p.location.direction.clone()
        vec.setY(0)
        vec.normalize()
        object : BukkitRunnable() {
            var time = 0
            val damaged = mutableSetOf<Int>()
            override fun run() {
                if (p.world != start.world) {
                    this.cancel()
                    p.velocity = Vector()
                    return
                }
                if (time++ >= 100) {
                    this.cancel()
                    p.velocity = Vector()
                    return
                }
                if (p.location.add(vec).block.type != Material.AIR) {
                    p.velocity = Vector()
                    this.cancel()
                    return
                }
                if (start.distanceSquared(p.location) >= lengthSq) {
                    p.velocity = Vector()
                    this.cancel()
                    return
                }
                p.velocity = vec
                for (e in p.getNearbyEntities(0.25, 0.25, 0.25)) {
                    if (isCitizens(e)) {
                        continue
                    }
                    if (e is LivingEntity && e !== p && !damaged.contains(e.entityId)) {
                        e.motionDamage(dmg, p, ci.castId)
                        val tvec = e.location.subtract(p.location).toVector()
                        tvec.y = 1.0
                        tvec.normalize().multiply(knock)
                        //e.velocity = tvec
                        e.knock(tvec, ci.castId)
                        damaged += e.entityId
                        if (stop) {
                            p.velocity = Vector()
                            this.cancel()
                            return
                        }
                    }
                }
            }
        }.runTaskTimer(Main.Plugin, 1, 1)
    }

    override fun loadConfig(config: ConfigurationSection) {
        damage = ExpressionHelper.compileExpression(config.getString("damage"))
        length = ExpressionHelper.compileExpression(config.getString("length"))
        knock = ExpressionHelper.compileExpression(config.getString("knock"))
        stop = ExpressionHelper.compileExpression(config.getString("stop"), true)

    }

}

class Jump : Motion("Jump") {
    lateinit var power: Expression
    lateinit var back: Expression
    var high: Expression? = null
    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val vec = p.location.direction
        if (back(p).toBoolean()) {
            vec.x *= -1
            vec.z *= -1
        }
        if (high != null) {
            vec.y = high!!(p).toDouble()
        }
        vec.normalize().multiply(power(p).toDouble())
        p.knock(vec, ci.castId)
    }

    override fun loadConfig(config: ConfigurationSection) {
        power = ExpressionHelper.compileExpression(config.getString("power"))
        back = ExpressionHelper.compileExpression(config.getString("back"), true)
        if (config.contains("high"))
            high = ExpressionHelper.compileExpression(config.getString("high"))

    }

}

class Flash : Motion("Flash") {
    lateinit var length: Expression
    lateinit var air: Expression
    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val length = length(p).toDouble()
        val vec = p.location.direction.normalize()
        if (!air(p).toBoolean()) {
            vec.y = 0.0
            vec.normalize()
        }
        var l = 0.0
        val move = length / (length + 1)
        var last = p.location
        while (l <= length) {
            l += move
            val t = vec.clone().multiply(l)
            val loc = p.location.add(t)
            if (loc.block.type != Material.AIR) {
                break
            } else {
                last = loc
            }
        }
        p.teleport(last)
    }

    override fun loadConfig(config: ConfigurationSection) {
        length = ExpressionHelper.compileExpression(config.getString("length"))
        air = ExpressionHelper.compileExpression(config.getString("air"), true)


    }

}

class FlamesColumn : Motion("FlamesColumn") {
    lateinit var damage: Expression
    lateinit var radius: Expression
    lateinit var range: Expression
    lateinit var delay: Expression
    lateinit var fireTick: Expression
    var particle: Particle? = null
    override fun cast(ci: CastInfo) {
        val p = ci.caster
        var b = p.getTargetBlock(mutableSetOf(Material.AIR), range(p).toInt()).location
        while (b.block.type == Material.AIR && b.y > 0) {
            b.add(0.0, -1.0, 0.0)
        }
        val dmg = damage(p).toDouble()
        val center = b.block.location.add(0.5, 1.0, 0.5)
        val delay = delay(p).toInt()
        val r = radius(p).toDouble()
        val fire = fireTick(p).toInt()
        object : BukkitRunnable() {
            val damaged = mutableSetOf(p.entityId)
            var time = 0
            override fun run() {
                if (time++ >= delay) {
                    if (time % 2 == 0)
                        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
                            drawColumn(center.clone(), r, particle)
                        }
                    for (target in getInColumn(center, r)) {
                        if (damaged.contains(target.entityId)) continue
                        damaged += target.entityId
                        target.motionDamage(dmg, p, ci.castId)
                        target.fireTicks = fire
                    }
                    if (time >= delay + 10) {
                        this.cancel()
                    }
                    return
                }
                Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
                    drawCircle(center, r, par = particle)
                }
            }
        }.runTaskTimer(Main.Plugin, 1, 1)
    }

    private companion object {

        private fun getInColumn(center: Location, r: Double, h: Double = 3.0): Collection<LivingEntity> {
            val center = center.clone()
            val list = mutableSetOf<LivingEntity>()
            val search = sqrt(r * r + h * h)
            val cloc = center.clone()
            cloc.y = 0.0
            for (e in center.world.getNearbyEntities(center, search, search, search)) {
                if (e !is LivingEntity) {
                    continue
                }
                if (isCitizens(e)) {
                    continue
                }
                val loc = e.location
                loc.y = 0.0
                if (cloc.distanceSquared(loc) <= r * r) {
                    list += e
                }
            }
            return list
        }

        private fun drawCircle(center: Location, r: Double, fill: Boolean = false, par: Particle?) {
            var st = 0.0
            while (st < Math.PI * 2) {
                st += Math.PI / if (fill) 6 else 9
                val x = cos(st)
                val z = sin(st)
                val loc = center.clone().add(x * r, 0.0, z * r)
                if (par == null)
                    ParticleEffect.FLAME.display(0.0f, 0f, 0f, 0F, 2, loc, 50.0)
                else par.play(loc)
                //fire.playParticle(loc, 50.0)
                if (fill) {
                    val del = r / 2
                    var r = r - del
                    while (r > 0) {
                        val vec = Vector.getRandom().multiply(0.1)
                        val loc = center.clone().add(x * r, 0.0, z * r)

                        if (par == null)
                            ParticleEffect.FLAME.display(vec.x.toFloat(), vec.y.toFloat() * 2, vec.z.toFloat(), 0.03F, 7, loc, 50.0)
                        else par.play(loc)
                        r -= del
                    }
                }
            }
        }

        private fun drawColumn(center: Location, r: Double, par: Particle?) {
            for (i in 1..3) {
                center.add(0.0, 1.0, 0.0)
                drawCircle(center, r, true, par)
            }
        }

    }

    override fun loadConfig(config: ConfigurationSection) {
        damage = ExpressionHelper.compileExpression(config.getString("damage"))
        radius = ExpressionHelper.compileExpression(config.getString("radius"))
        range = ExpressionHelper.compileExpression(config.getString("range"))
        delay = ExpressionHelper.compileExpression(config.getString("delay"))
        fireTick = ExpressionHelper.compileExpression(config.getString("fireTick"))
        if (config.contains("particle")) {
            particle = ParticleManager.readParticle(config.getString("particle"))
        }


    }

}

class ShockWave : Motion("ShockWave") {
    lateinit var damage: Expression
    lateinit var length: Expression
    lateinit var width: Expression
    lateinit var knock: Expression
    lateinit var speed: Expression //每tick传播的距离
    var particle: Particle? = null

    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val from = p.location
        val effectVec = from.direction.clone()
        effectVec.y = 0.6
        effectVec.normalize()
        val a_z = Vector(
                -sin(Math.toRadians(from.yaw.toDouble())),
                0.0,
                cos(Math.toRadians(from.yaw.toDouble()))
        ).normalize()
        val a_y = Vector(0f, 1f, 0f)
        val a_x = a_z.getCrossProduct(a_y).normalize()
        var length = length(p).toDouble()
        val width = width(p).toDouble() / 2
        val damage = damage(p).toDouble()
        val knock = knock(p).toDouble()
        val speed = speed(p).toDouble()
        object : BukkitRunnable() {
            val damaged = mutableSetOf(p.entityId)
            var len = speed
            override fun run() {
                if (len >= length) {
                    this.cancel()
                }
                val center = from.clone().add(a_z.clone().multiply(len))
                center.world.playSound(center, Sound.ENTITY_WITHER_BREAK_BLOCK, 0.1F, 0F)
                var w = 0.0
                while (w <= width) {
                    var loc = center.clone().add(a_x.clone().multiply(w))
                    for (e in loc.world.getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                        if (e !is LivingEntity || damaged.contains(e.entityId)) continue
                        if (isCitizens(e)) continue
                        damaged += e.entityId
                        e.motionDamage(damage, p, ci.castId)
                        e.knock(effectVec.clone().multiply(knock), ci.castId)
                    }
                    playEffect(loc, effectVec, particle)
                    loc = center.clone().add(a_x.clone().multiply(-w))
                    for (e in loc.world.getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                        if (e !is LivingEntity || damaged.contains(e.entityId)) continue
                        if (isCitizens(e)) continue
                        damaged += e.entityId
                        e.motionDamage(damage, p, ci.castId)
                        e.knock(effectVec.clone().multiply(knock), ci.castId)
                    }
                    playEffect(loc, effectVec, particle)
                    w += 0.5
                }
                len += speed
            }

        }.runTaskTimer(Main.Plugin, 1, 1)
    }

    override fun loadConfig(config: ConfigurationSection) {
        damage = ExpressionHelper.compileExpression(config.getString("damage"))
        length = ExpressionHelper.compileExpression(config.getString("length"))
        width = ExpressionHelper.compileExpression(config.getString("width"))
        knock = ExpressionHelper.compileExpression(config.getString("knock"))
        speed = ExpressionHelper.compileExpression(config.getString("speed"))
        if (config.contains("particle")) {
            particle = ParticleManager.readParticle(config.getString("particle"))
        }
    }

    companion object {

        private fun playEffect(loc: Location, vec: Vector, particle: Particle?) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
                if (particle == null) {
                    var t = loc.block.type
                    if (t == Material.AIR) {
                        t = loc.clone().add(0.0, -1.0, 0.0).block.type
                    }
                    if (t == Material.AIR) {
                        t = Material.STONE
                    }
                    ParticleEffect.BLOCK_DUST.display(
                            ParticleEffect.BlockData(t, 0),
                            vec.x.toFloat(),
                            vec.y.toFloat(),
                            vec.z.toFloat(),
                            0.1f,
                            8,
                            loc,
                            50.0
                    )
                } else {
                    particle.play(loc)
                }
            }
        }
    }

}

class ShockWavePull : Motion("ShockWavePull") {
    lateinit var damage: Expression
    lateinit var length: Expression
    lateinit var width: Expression
    lateinit var knock: Expression
    lateinit var speed: Expression //每tick传播的距离
    var particle: Particle? = null

    override fun cast(ci: CastInfo) {
        val p = ci.caster
        val from = p.location
        val effectVec = from.direction.clone()
        effectVec.y = 0.6
        effectVec.x *= -1
        effectVec.z *= -1
        effectVec.normalize()
        val a_z = Vector(
                -sin(Math.toRadians(from.yaw.toDouble())),
                0.0,
                cos(Math.toRadians(from.yaw.toDouble()))
        ).normalize()
        val a_y = Vector(0f, 1f, 0f)
        val a_x = a_z.getCrossProduct(a_y).normalize()
        var length = length(p).toDouble()
        val width = width(p).toDouble() / 2
        val damage = damage(p).toDouble()
        val knock = knock(p).toDouble()
        val speed = speed(p).toDouble()
        object : BukkitRunnable() {
            val damaged = mutableSetOf(p.entityId)
            var len = length
            override fun run() {
                if (len <= 0) {
                    this.cancel()
                }
                val center = from.clone().add(a_z.clone().multiply(len))
                center.world.playSound(center, Sound.ENTITY_WITHER_BREAK_BLOCK, 0.1F, 0F)
                var w = 0.0
                while (w <= width) {
                    var loc = center.clone().add(a_x.clone().multiply(w))
                    for (e in loc.world.getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                        if (e !is LivingEntity || damaged.contains(e.entityId)) continue
                        if (isCitizens(e)) continue
                        damaged += e.entityId
                        e.motionDamage(damage, p, ci.castId)
                        e.knock(effectVec.clone().multiply(knock), ci.castId)
                    }
                    playEffect(loc, effectVec, particle)
                    loc = center.clone().add(a_x.clone().multiply(-w))
                    for (e in loc.world.getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                        if (e !is LivingEntity || damaged.contains(e.entityId)) continue
                        if (isCitizens(e)) continue
                        damaged += e.entityId
                        e.motionDamage(damage, p, ci.castId)
                        e.knock(effectVec.clone().multiply(knock), ci.castId)
                    }
                    playEffect(loc, effectVec, particle)
                    w += 0.5
                }
                len -= speed
            }

        }.runTaskTimer(Main.Plugin, 1, 1)
    }

    override fun loadConfig(config: ConfigurationSection) {
        damage = ExpressionHelper.compileExpression(config.getString("damage"))
        length = ExpressionHelper.compileExpression(config.getString("length"))
        width = ExpressionHelper.compileExpression(config.getString("width"))
        knock = ExpressionHelper.compileExpression(config.getString("knock"))
        speed = ExpressionHelper.compileExpression(config.getString("speed"))
        if (config.contains("particle")) {
            particle = ParticleManager.readParticle(config.getString("particle"))
        }

    }

    companion object {

        private fun playEffect(loc: Location, vec: Vector, particle: Particle?) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
                if (particle == null) {
                    var t = loc.block.type
                    if (t == Material.AIR) {
                        t = loc.clone().add(0.0, -1.0, 0.0).block.type
                    }
                    if (t == Material.AIR) {
                        t = Material.STONE
                    }
                    ParticleEffect.BLOCK_DUST.display(
                            ParticleEffect.BlockData(t, 0),
                            vec.x.toFloat(),
                            vec.y.toFloat(),
                            vec.z.toFloat(),
                            0.1f,
                            8,
                            loc,
                            50.0
                    )
                } else {
                    particle?.play(loc)
                }
            }
        }
    }

}

class TriggerMotion : Motion("Trigger") {
    lateinit var trigger: Trigger

    override fun loadConfig(config: ConfigurationSection) {
        trigger = TriggerManager.loadTrigger(config)
    }

    override fun cast(p: CastInfo) {
        val cd = SkillManager.castingSkill[p.castId]
        cd?.triggers?.add(this.trigger)
    }
}

class GreatLight : Motion("GreatLight") {
    lateinit var damage: Expression
    lateinit var length: Expression
    lateinit var radius: Expression
    lateinit var delay: Expression
    override fun cast(ci: CastInfo) {
        if (casting.contains(ci.caster.entityId)) {
            return
        }
        casting += ci.caster.entityId
        val dmg = damage(ci.caster).toDouble()
        val length = length(ci.caster).toDouble()
        val radius = radius(ci.caster).toDouble()
        val delay = delay(ci.caster).toInt()
        object : BukkitRunnable() {
            var time = 0
            val from = ci.caster.eyeLocation.add(0.0, -0.25, 0.0)
            val vec = from.direction.normalize()
            override fun run() {
                if (time < delay) {
                    Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
                        var l = 0.0
                        while (l < length) {
                            l += 0.2
                            val t = vec.clone().multiply(l)
                            val target = from.clone().add(t)
                            centerParticle.play(target)
                        }
                    }
                } else {
                    var l = 0.0
                    val damage = mutableSetOf<LivingEntity>()
                    while (l < length) {
                        l += 0.25
                        val t = vec.clone().multiply(l)
                        val target = from.clone().add(t)
                        for (e in target.world.getNearbyEntities(target, radius * 1.44, radius * 1.44, radius * 1.44)) {
                            if (e !is LivingEntity || e == ci.caster) {
                                continue
                            }
                            val eloc = e.eyeLocation
                            val c = from.toVector().subtract(eloc.toVector()).getCrossProduct(vec)
                            val r = c.length()
                            if (r < radius) {
                                damage += e
                            }
                        }
                        val tl = l
                        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
                            val proj = Utils.Coordinate.create2DProjector(target, vec)
                            var st = 0.0
                            while (st <= Math.PI * 2) {
                                val x = cos(st)
                                val z = sin(st)
                                var r = 0.0
                                while (r <= radius) {
                                    val x = x * r
                                    val z = z * r
                                    val eff = proj.apply(x, z)
                                    eff.add(vec.clone().multiply(tl))
                                    damageParticle.play(eff)
                                    r += radius / 6.0
                                }
                                st += Math.PI / 6
                            }
                        }
                        for (e in damage) {
                            e.motionDamage(dmg, ci.caster, ci.castId)
                        }

                    }

                    damage.clear()
                    this.cancel()
                    casting -= ci.caster.entityId
                    return
                }
                time++
            }

        }.runTaskTimer(Main.Plugin, 1, 1)
    }

    override fun loadConfig(config: ConfigurationSection) {
        damage = ExpressionHelper.compileExpression(config.getString("damage"))
        length = ExpressionHelper.compileExpression(config.getString("length"))
        radius = ExpressionHelper.compileExpression(config.getString("radius"))
        delay = ExpressionHelper.compileExpression(config.getString("delay"))
    }

    companion object : Listener {
        val centerParticle = ColorDust()
        val damageParticle = ColorDust()

        val casting = mutableSetOf<Int>()

        init {
            Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
            centerParticle.init(arrayOf("255", "0", "0"))
            damageParticle.init(arrayOf("255", "255", "0"))
        }

        @EventHandler
        fun onMove(evt: PlayerMoveEvent) {
            if (casting.contains(evt.player.entityId)) {
                evt.isCancelled = true
            }
        }
    }
}