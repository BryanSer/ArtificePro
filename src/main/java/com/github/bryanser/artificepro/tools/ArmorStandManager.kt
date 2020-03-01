package com.github.bryanser.artificepro.tools

import com.github.bryanser.brapi.Main
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.ArmorStand
import org.bukkit.metadata.FixedMetadataValue
import java.io.File
import java.util.*

object ArmorStandManager : Runnable {
    const val META_KEY_CREATE_TIME = "artificepro_as_creattime"
    const val MAX_TIME = 60000L

    init {
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), this, 200, 200)
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getPlugin(), {
            saveUUID()
        }, 600, 600)
        checkUUID()
    }

    fun saveUUID() {
        val f = File(Main.PLGUIN.dataFolder, "ArmorStandManager.tmp")
        val config = YamlConfiguration()
        config.set("agentUUID", ArrayList(agentUUID.map{it.toString()}))
        config.save(f)
    }

    fun checkUUID() {
        val f = File(Main.PLGUIN.dataFolder, "ArmorStandManager.tmp")
        if(f.exists()){
            val config = YamlConfiguration()
            val uids = config.getStringList("agentUUID")
            agentUUID.addAll(uids.map { UUID.fromString(it) })
        }
    }

    fun removeAll() {
        val it = agentUUID.iterator()
        while (it.hasNext()) {
            val uid = it.next()
            val a = Bukkit.getEntity(uid)
            a?.remove()
        }
        agentUUID.clear()
        val f = File(Main.PLGUIN.dataFolder, "ArmorStandManager.tmp")
        f.delete()
    }

    val agentUUID = LinkedList<UUID>()

    fun createArmorStand(loc: Location, func: (ArmorStand) -> Unit = {}): ArmorStand {
        val a = loc.world.spawn(loc, ArmorStand::class.java, func)
        a.setMetadata(META_KEY_CREATE_TIME, FixedMetadataValue(Main.getPlugin(), System.currentTimeMillis()))
        agentUUID.add(a.uniqueId)
        return a
    }

    override fun run() {
        val it = agentUUID.iterator()
        while (it.hasNext()) {
            val uid = it.next()
            val a = Bukkit.getEntity(uid)
            if (a == null || a.isDead || !a.isValid) {
                a?.remove()
                it.remove()
            } else {
                if(!a.hasMetadata(META_KEY_CREATE_TIME)){
                    a.remove()
                    it.remove()
                    continue
                }
                val time = a.getMetadata(META_KEY_CREATE_TIME).first().asLong()
                if (System.currentTimeMillis() - time > MAX_TIME) {
                    a.remove()
                    it.remove()
                }
            }
        }
    }
}