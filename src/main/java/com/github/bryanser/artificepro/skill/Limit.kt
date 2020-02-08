package com.github.bryanser.artificepro.skill

import com.bekvon.bukkit.residence.api.ResidenceApi
import com.github.bryanser.artificepro.Main
import com.github.bryanser.brapi.Utils
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

object Limit {
    var residenceSelf = true
    var residenceOther = false
    lateinit var residenceList:List<String>
    var residenceAllow = false

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
            val allow = if(isOwner){
                 residenceSelf
            }else{
                residenceOther
            }
            if(!allow){
                return false
            }
            val rc = residenceList.contains(res.name)
            if(rc xor residenceAllow){
                return false
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
        residenceSelf = config.getBoolean("Residence.self")
        residenceOther = config.getBoolean("Residence.other")
        residenceList = config.getStringList("Residence.list")
        residenceAllow = config.getBoolean("Residence.allow")
        worldList = config.getStringList("World.list")
        worldAllow = config.getBoolean("World.allow")
    }
}