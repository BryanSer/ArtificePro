package com.github.bryanser.artificepro.skill

import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.motion.MotionManager
import com.github.bryanser.artificepro.script.ExpressionHelper
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*

@FunctionalInterface
interface IStep {
    fun cast(p: Player, lv: Int, castId: UUID)
}

class Step(
        config: ConfigurationSection
) : IStep {

    var next: IStep? = null
    val level: Int = config.getInt("Level")
    val run: (Player, Int, UUID) -> Unit

    init {

        if (config.getString("Name") == "Delay") {
            val time = ExpressionHelper.compileExpression(config.getString("Config.Time"))
            run = { it, lv, castID ->
                Bukkit.getScheduler().runTaskLater(Main.Plugin, {
                    if (next != null) {
                        ExpressionHelper.levelHolder[it.entityId] = lv
                        next!!.cast(it, lv, castID)
                    }
                }, time(it).toLong())
            }
        }else if(config.getString("Name") == "Chance"){
            val chance = ExpressionHelper.compileExpression(config.getString("Config.Chance"))
            run = { it, lv, castID ->
                val c = chance(it).toDouble()
                if(Math.random() <= c){
                    if (next != null) {
                        next!!.cast(it, lv, castID)
                    }
                }else{
                    Bukkit.getScheduler().runTaskLater(Main.Plugin, {
                        SkillManager.castingSkill.remove(castID)
                    }, 600)
                }
            }
        } else {
            val motion = MotionManager.loadMotion(config)
            run = { it, lv, castID ->
                motion.cast(CastInfo(it, it, castID))
                if (next != null) {
                    next!!.cast(it, lv, castID)
                }
            }
        }
    }

    override fun cast(p: Player, lv: Int, castId: UUID) {
        if (lv < level) {
            if (next != null) {
                next!!.cast(p, lv, castId)
                return
            }
        }
        this.run(p, lv, castId)
    }

}