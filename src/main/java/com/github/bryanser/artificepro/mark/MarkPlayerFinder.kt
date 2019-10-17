package com.github.bryanser.artificepro.mark

import com.github.bryanser.artificepro.script.finder.EntityFinderTemplate
import com.github.bryanser.artificepro.script.finder.Finder
import com.github.bryanser.artificepro.script.finder.PlayerFinderTemplate
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

object MarkPlayerFinder: PlayerFinderTemplate("MarkPlayer") {
    override fun read(args: Array<String>): Finder<Player> {
        val key = args[0]
        return Finder{
            val data = MarkManager.getData(it.uniqueId)
            val list = mutableListOf<Player>()
            for(info in data.marks){
                if(info.key == key){
                    list += Bukkit.getEntity(info.mark) as? Player ?: continue
                }
            }
            list
        }
    }
}
object MarkEntityFinder: EntityFinderTemplate<LivingEntity>("MarkPlayer") {
    override fun read(args: Array<String>): Finder<LivingEntity> {
        val key = args[0]
        val player = args.getOrNull(1)?.toBoolean() ?: false
        return Finder{
            val data = MarkManager.getData(it.uniqueId)
            val list = mutableListOf<LivingEntity>()
            for(info in data.marks){
                if(info.key == key){
                   val ent =  Bukkit.getEntity(info.mark) as? LivingEntity ?: continue
                    if(ent is Player && !player){
                        continue
                    }
                    list += ent
                }
            }
            list
        }
    }
}