package com.github.bryanser.artificepro.mark

import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.tools.ParticleEffect
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import java.util.*

object MarkManager : Runnable {

    var tick: Long = 0

    val markDatas = mutableMapOf<UUID, MarkData>()

    fun getData(uuid:UUID):MarkData = markDatas.getOrPut(uuid){
        MarkData(uuid)
    }

    fun init() {
        Bukkit.getScheduler().runTaskTimer(Main.Plugin, this, 3, 3)
    }

    fun display(p: Location, color: Color) {
        var st = 0.0
        val color = ParticleEffect.OrdinaryColor(color.red, color.green, color.blue)
        while (st <= Math.PI * 2) {
            var al = 0.0
            while (al <= Math.PI) {
                val offx = Math.sin(st) * 0.5
                val offz = Math.cos(st) * 0.5
                val offy = Math.cos(al) * 0.5
                val loc = p.clone().add(offx, offy, offz)
                ParticleEffect.REDSTONE.display(color, loc, 60.0)
                al += Math.PI / 6
            }
            st += Math.PI / 8.0
        }
    }

    override fun run() {
        val mit = markDatas.entries.iterator()
        while(mit.hasNext()) {
            val (name,data) = mit.next()
            Bukkit.getEntity(name)?.also { p ->
                val iter = data.beenMarked.listIterator()
                var index = 0
                val size = data.beenMarked.size
                val tar = (tick % size).toInt()
                while (iter.hasNext()) {
                    val info = iter.next()
                    if (System.currentTimeMillis() > info.endTime) {
                        iter.remove()
                        continue
                    }
                    if (tar == index) {
                        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
                            display(p.location.add(0.0, 2.7, 0.0), info.color)
                        }
                    }
                    index++
                }
            } ?: mit.remove()
        }
        tick++
    }
}