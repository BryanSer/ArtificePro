package com.github.bryanser.artificepro.motion

import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.script.*
import com.github.bryanser.artificepro.script.finder.EntityFinderTemplate
import com.github.bryanser.artificepro.script.finder.Finder
import com.github.bryanser.artificepro.script.finder.PlayerFinderTemplate
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.*
import Br.API.ParticleEffect.ParticleEffect
import org.bukkit.Sound


class Scattering : Motion(
        "Scattering"
) {
    lateinit var damage: Expression
    lateinit var amount: Expression
    override fun cast(p: Player) {
        val v = p.location.direction
        val amount = amount(p).toInt()
        var i = 0
        while (i < amount) {
            val a = p.launchProjectile(Arrow::class.java, randomVector(v))
            a.setMetadata(METADATA_KEY, FixedMetadataValue(Main.Plugin, damage(p).toDouble()))
            i++
        }
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
        const val METADATA_KEY: String = "artificepro_arrow_damage"

        init {
            Bukkit.getPluginManager().registerEvents(this, Main.Plugin)
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        fun onHit(evt: EntityDamageByEntityEvent) {
            val a = evt.damager as? Arrow ?: return
            if (a.hasMetadata(METADATA_KEY)) {
                val dmg = a.getMetadata(METADATA_KEY).first().asDouble()
                evt.damage = dmg
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
        fun onPick(evt: PlayerPickupArrowEvent) {
            if (evt.arrow.hasMetadata(METADATA_KEY)) {
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

class Command : Motion("Command") {
    //target,from
    val commands: MutableList<(Player, Player) -> Unit> = mutableListOf()
    lateinit var finder: Finder<Player>
    override fun loadConfig(config: ConfigurationSection?) {
        if (config == null) throw IllegalArgumentException("配置编写错误 缺少配置数据")
        //commands = config.getStringList("Commands")
        for (cmd in config.getStringList("Commands")) {
            val arg = cmd.split(":".toRegex(), 2)
            if (arg.size < 2) {
                throw IllegalArgumentException("动作Command编写错误 缺少p,target,op,targetop或c")
            }
            val cmd = arg[1]
            when (arg[0].toLowerCase()) {
                "p" -> {
                    commands += { target, from ->
                        try {
                            Bukkit.dispatchCommand(from,
                                    cmd.replace("%target%", target.name).replace("%from%", from.name)
                            )
                        } catch (e: Exception) {
                        }
                    }
                }
                "target" -> {
                    commands += { target, from ->
                        try {
                            Bukkit.dispatchCommand(target,
                                    cmd.replace("%target%", target.name).replace("%from%", from.name)
                            )
                        } catch (e: Exception) {
                        }
                    }
                }
                "op" -> {
                    commands += { target, from ->
                        val op = from.isOp
                        try {
                            from.isOp = true
                            Bukkit.dispatchCommand(from,
                                    cmd.replace("%target%", target.name).replace("%from%", from.name)
                            )
                        } finally {
                            from.isOp = op
                        }
                    }
                }
                "targetop" -> {
                    commands += { target, from ->
                        val op = target.isOp
                        try {
                            target.isOp = true
                            Bukkit.dispatchCommand(target,
                                    cmd.replace("%target%", target.name).replace("%from%", from.name)
                            )
                        } finally {
                            target.isOp = op
                        }
                    }
                }
                "c" -> {
                    commands += { target, from ->
                        try {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                    cmd.replace("%target%", target.name).replace("%from%", from.name)
                            )
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        }
        val (f, t) = FinderManager.readFinder(config.getString("Finder", "Self()"))
        if (t !is PlayerFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Player")
        }
        finder = f as Finder<Player>
    }

    override fun cast(p: Player) {
        for (target in finder(p)) {
            for (cmd in commands) {
                cmd(target, p)
            }
        }
    }


}

class GuidedArrow : Motion("GuidedArrow") {
    lateinit var damage: Expression
    lateinit var time: Expression
    lateinit var finder: Finder<LivingEntity>
    override fun cast(p: Player) {
        val maxTime = time(p).toLong()
        val dmg = damage(p).toDouble()
        for (target in finder(p)) {
            val arrow: Arrow = p.launchProjectile(
                    Arrow::class.java,
                    target.eyeLocation.subtract(p.location).toVector().normalize()
            )
            arrow.setMetadata(Scattering.METADATA_KEY, FixedMetadataValue(Main.Plugin, dmg))
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


    override fun loadConfig(config: ConfigurationSection?) {
        if (config != null) {
            damage = ExpressionHelper.compileExpression(config.getString("damage"))
            time = ExpressionHelper.compileExpression(config.getString("time"))
            val (f, t) = FinderManager.readFinder(config.getString("Finder"))
            if (t !is EntityFinderTemplate) {
                throw IllegalArgumentException("配置编写错误 Finder类型不是Entity")
            }
            finder = f as Finder<LivingEntity>
        } else {
            throw IllegalArgumentException("配置编写错误 缺少配置数据")
        }
    }

}

class Effect : Motion("Effect") {
    var id: Int = 0
    lateinit var time: Expression
    lateinit var level: Expression
    lateinit var finder: Finder<LivingEntity>

    override fun cast(p: Player) {
        val tick = time(p).toInt()
        val lv = level(p).toInt() - 1
        for (target in finder(p)) {
            target.addPotionEffect(PotionEffect(PotionEffectType.getById(id), tick, lv))
        }
    }

    override fun loadConfig(config: ConfigurationSection?) {
        if (config != null) {
            id = config.getInt("id")
            time = ExpressionHelper.compileExpression(config.getString("time"))
            level = ExpressionHelper.compileExpression(config.getString("level"))
            val (f, t) = FinderManager.readFinder(config.getString("Finder"))
            if (t !is EntityFinderTemplate) {
                throw IllegalArgumentException("配置编写错误 Finder类型不是Entity")
            }
            finder = f as Finder<LivingEntity>
        } else {
            throw IllegalArgumentException("配置编写错误 缺少配置数据")
        }
    }

}

class Charge : Motion("Charge") {
    lateinit var damage: Expression
    lateinit var length: Expression
    lateinit var knock: Expression
    lateinit var stop: Expression
    override fun cast(p: Player) {
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
                    if (e is LivingEntity && e !== p && !damaged.contains(e.entityId)) {
                        e.damage(dmg)
                        val tvec = e.location.subtract(p.location).toVector()
                        tvec.y = 1.0
                        tvec.normalize().multiply(knock)
                        e.velocity = tvec
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

    override fun loadConfig(config: ConfigurationSection?) {
        if (config != null) {
            damage = ExpressionHelper.compileExpression(config.getString("damage"))
            length = ExpressionHelper.compileExpression(config.getString("length"))
            knock = ExpressionHelper.compileExpression(config.getString("knock"))
            stop = ExpressionHelper.compileExpression(config.getString("stop"), true)
        } else {
            throw IllegalArgumentException("配置编写错误 缺少配置数据")
        }
    }

}

class Jump : Motion("Jump") {
    lateinit var power: Expression
    lateinit var back: Expression
    var high: Expression? = null
    override fun cast(p: Player) {
        val vec = p.location.direction
        if (back(p).toBoolean()) {
            vec.x *= -1
            vec.z *= -1
        }
        if (high != null) {
            vec.y = high!!(p).toDouble()
        }
        vec.normalize().multiply(power(p).toDouble())
        p.velocity = vec
    }

    override fun loadConfig(config: ConfigurationSection?) {
        if (config != null) {
            power = ExpressionHelper.compileExpression(config.getString("power"))
            back = ExpressionHelper.compileExpression(config.getString("back"), true)
            if (config.contains("high"))
                high = ExpressionHelper.compileExpression(config.getString("high"))
        } else {
            throw IllegalArgumentException("配置编写错误 缺少配置数据")
        }
    }

}

class Flash : Motion("Flash") {
    lateinit var length: Expression
    lateinit var air: Expression
    override fun cast(p: Player) {
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

    override fun loadConfig(config: ConfigurationSection?) {
        if (config != null) {
            length = ExpressionHelper.compileExpression(config.getString("length"))
            air = ExpressionHelper.compileExpression(config.getString("air"), true)
        } else {
            throw IllegalArgumentException("配置编写错误 缺少配置数据")
        }
    }

}

class Heal : Motion("Heal") {
    lateinit var amount: Expression
    lateinit var percentage: Expression
    lateinit var finder: Finder<LivingEntity>

    override fun cast(p: Player) {
        val per = percentage(p).toBoolean()
        val v = amount(p).toDouble()
        for (target in finder(p)) {
            val max = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).value
            val h = if (per) {
                max * v
            } else {
                v
            }
            var health = p.health + h
            if (health > max) {
                health = max
            } else if (health < 0) {
                health = 0.0
            }
            p.health = health
        }
    }

    override fun loadConfig(config: ConfigurationSection?) {
        if (config != null) {
            amount = ExpressionHelper.compileExpression(config.getString("amount"))
            percentage = ExpressionHelper.compileExpression(config.getString("percentage"), true)
            val (f, t) = FinderManager.readFinder(config.getString("Finder"))
            if (t !is EntityFinderTemplate) {
                throw IllegalArgumentException("配置编写错误 Finder类型不是Entity")
            }
            finder = f as Finder<LivingEntity>
        } else {
            throw IllegalArgumentException("配置编写错误 缺少配置数据")
        }
    }
}

class FlamesColumn : Motion("FlamesColumn") {
    lateinit var damage: Expression
    lateinit var radius: Expression
    lateinit var range: Expression
    lateinit var delay: Expression
    lateinit var fireTick: Expression
    override fun cast(p: Player) {
        var b = p.getTargetBlock(mutableSetOf<Material>(Material.AIR), range(p).toInt()).location
        while (b.block.type == Material.AIR && b.y > 0) {
            b.add(0.0, -1.0, 0.0)
        }
        val dmg = damage(p).toDouble()
        val center = b.block.location.add(0.5, 1.0, 0.5)
        val delay = delay(p).toInt()
        val r = radius(p).toDouble()
        val fire = fireTick(p).toInt()
        object : BukkitRunnable() {
            val damaged = mutableSetOf<Int>(p.entityId)
            var time = 0
            override fun run() {
                if (time++ >= delay) {
                    if (time % 2 == 0)
                        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
                            drawColumn(center.clone(), r)
                        }
                    for (target in getInColumn(center, r)) {
                        if (damaged.contains(target.entityId)) continue
                        damaged += target.entityId
                        target.damage(dmg, p)
                        target.fireTicks = fire
                    }
                    if (time >= delay + 10) {
                        this.cancel()
                    }
                    return
                }
                Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
                    drawCircle(center, r)
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
                val loc = e.location
                loc.y = 0.0
                if (cloc.distanceSquared(loc) <= r * r) {
                    list += e
                }
            }
            return list
        }

        private fun drawCircle(center: Location, r: Double, fill: Boolean = false) {
            var st = 0.0
            while (st < Math.PI * 2) {
                st += Math.PI / if (fill) 6 else 12
                val x = cos(st)
                val z = sin(st)
                val loc = center.clone().add(x * r, 0.0, z * r)
                ParticleEffect.FLAME.display(0.0f, 0f, 0f, 0F, 2, loc, 50.0)
                //fire.playParticle(loc, 50.0)
                if (fill) {
                    val del = r / 3
                    var r = r - del
                    while (r > 0) {
                        val vec = Vector.getRandom().multiply(0.1)
                        val loc = center.clone().add(x * r, 0.0, z * r)
                        ParticleEffect.FLAME.display(vec.x.toFloat(), vec.y.toFloat() * 2, vec.z.toFloat(), 0.03F, 7, loc, 50.0)
                        r -= del
                    }
                }
            }
        }

        private fun drawColumn(center: Location, r: Double) {
            for (i in 1..5) {
                center.add(0.0, 0.6, 0.0)
                drawCircle(center, r, true)
            }
        }

    }

    override fun loadConfig(config: ConfigurationSection?) {
        if (config != null) {
            damage = ExpressionHelper.compileExpression(config.getString("damage"))
            radius = ExpressionHelper.compileExpression(config.getString("radius"))
            range = ExpressionHelper.compileExpression(config.getString("range"))
            delay = ExpressionHelper.compileExpression(config.getString("delay"))
            fireTick = ExpressionHelper.compileExpression(config.getString("fireTick"))
        } else {
            throw IllegalArgumentException("配置编写错误 缺少配置数据")
        }

    }

}

class Damage : Motion("Damage") {
    lateinit var damage: Expression
    lateinit var finder: Finder<LivingEntity>
    override fun cast(p: Player) {
        val dmg = damage(p).toDouble()
        for (target in finder(p)) {
            target.damage(dmg, p)
        }
    }

    override fun loadConfig(config: ConfigurationSection?) {
        if (config != null) {
            damage = ExpressionHelper.compileExpression(config.getString("damage"))
            val (f, t) = FinderManager.readFinder(config.getString("Finder"))
            if (t !is EntityFinderTemplate) {
                throw IllegalArgumentException("配置编写错误 Finder类型不是Entity")
            }
            finder = f as Finder<LivingEntity>
        } else {
            throw IllegalArgumentException("配置编写错误 缺少配置数据")
        }
    }
}

class Knock : Motion("Knock") {
    lateinit var knock: Expression
    lateinit var finder: Finder<LivingEntity>

    override fun cast(p: Player) {
        val k = knock(p).toDouble()
        for (target in finder(p)) {
            val vec = target.location.toVector().subtract(p.location.toVector())
            vec.y = 1.0
            vec.normalize().multiply(k)
            target.velocity = vec
        }
    }


    override fun loadConfig(config: ConfigurationSection?) {
        if (config != null) {
            knock = ExpressionHelper.compileExpression(config.getString("knock"))
            val (f, t) = FinderManager.readFinder(config.getString("Finder"))
            if (t !is EntityFinderTemplate) {
                throw IllegalArgumentException("配置编写错误 Finder类型不是Entity")
            }
            finder = f as Finder<LivingEntity>
        } else {
            throw IllegalArgumentException("配置编写错误 缺少配置数据")
        }
    }

}

class ShockWave : Motion("ShockWave") {
    lateinit var damage: Expression
    lateinit var length: Expression
    lateinit var width: Expression
    lateinit var knock: Expression
    lateinit var speed: Expression //每tick传播的距离

    override fun cast(p: Player) {
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
                        damaged += e.entityId
                        e.damage(damage, p)
                        e.velocity = effectVec.clone().multiply(knock)
                    }
                    playEffect(loc, effectVec)
                    loc = center.clone().add(a_x.clone().multiply(-w))
                    for (e in loc.world.getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                        if (e !is LivingEntity || damaged.contains(e.entityId)) continue
                        damaged += e.entityId
                        e.damage(damage, p)
                        e.velocity = effectVec.clone().multiply(knock)
                    }
                    playEffect(loc, effectVec)
                    w += 0.5
                }
                len += speed
            }

        }.runTaskTimer(Main.Plugin, 1, 1)
    }

    override fun loadConfig(config: ConfigurationSection?) {
        if (config != null) {
            damage = ExpressionHelper.compileExpression(config.getString("damage"))
            length = ExpressionHelper.compileExpression(config.getString("length"))
            width = ExpressionHelper.compileExpression(config.getString("width"))
            knock = ExpressionHelper.compileExpression(config.getString("knock"))
            speed = ExpressionHelper.compileExpression(config.getString("speed"))
        } else {
            throw IllegalArgumentException("配置编写错误 缺少配置数据")
        }
    }

    companion object {

        private fun playEffect(loc: Location, vec: Vector) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
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
            }
        }
    }

}