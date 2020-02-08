package com.github.bryanser.artificepro.skill

import com.bekvon.bukkit.residence.Residence
import com.bekvon.bukkit.residence.api.ResidenceApi
import com.github.bryanser.artificepro.Main
import com.github.bryanser.brapi.Utils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

object Limit {
    var residence_self = true
    var residence_other = false

    lateinit var worldList: List<String>
    var worldAllow = false
    val hasResidence:Boolean by lazy{
        Bukkit.getPluginManager().getPlugin("Residence") != null
    }

    fun checkCastable(p: Player):Boolean{
        val loc = p.location
        var cont = worldList.contains(loc.world.name)
        if(cont xor worldAllow){
            return false
        }
        if(hasResidence){
            val res = ResidenceApi.getResidenceManager().getByLoc(loc) ?: return true
            val isOwner = res.isOwner(p)
            if(isOwner){
                return residence_self
            }else{
                return residence_other
            }
        }
        return true
    }

    fun load() {
        val f = File(Main.Plugin.dataFolder, "castLimit.yml")
        if (!f.exists()) {
            Utils.saveResource(Main.Plugin, "castLimit.yml")
        }
        val config = YamlConfiguration.loadConfiguration(f)
        residence_self = config.getBoolean("Residence.self")
        residence_other = config.getBoolean("Residence.other")
        worldList = config.getStringList("World.list")
        worldAllow = config.getBoolean("World.allow")
    }
}