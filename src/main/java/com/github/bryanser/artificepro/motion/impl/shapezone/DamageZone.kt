package com.github.bryanser.artificepro.motion.impl.shapezone

import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.motion.Motion
import com.github.bryanser.artificepro.motion.trigger.ShapeTrigger
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.skill.SkillManager
import com.github.bryanser.brapi.Main
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.scheduler.BukkitRunnable

class DamageZone : Motion("DamageZone") {
    lateinit var shape: Shape
    lateinit var key: String
    lateinit var delay: Expression
    lateinit var self: Expression
    lateinit var follow: Expression

    override fun cast(ci: CastInfo) {
        val delay = this.delay(ci.caster).toInt()
        val self = this.self(ci.caster).toBoolean()
        val follow = this.follow(ci.caster).toBoolean()
        object : BukkitRunnable() {
            var tick = 0
            var loc = ci.caster.location
            init{
                shape.playEffect(loc)
            }
            override fun run() {
                if (tick++ == delay) {
                    this.cancel()
                    val cd = SkillManager.castingSkill[ci.castId]
                    if(cd != null) {
                        if (follow) {
                            loc = ci.caster.location
                        }
                        for(e in shape.getDamageZoneEntities(loc)){
                            if(!self && e == ci.caster){
                                continue
                            }
                            if(e !is LivingEntity){
                                continue
                            }
                            for (t in cd.triggers) {
                                if (t is ShapeTrigger && t.key == key) {
                                    t.onTrigger(e, ci.caster, ci.castId)
                                }
                            }
                        }
                    }
                    return
                }

                if (follow) {
                    loc = ci.caster.location
                }
                if (tick % 3 == 0) {
                    shape.playEffect(loc)
                }
            }
        }.runTaskTimer(Main.getPlugin(), 1, 1)
    }

    override fun loadConfig(config: ConfigurationSection) {
        shape = Shape(config.getConfigurationSection("Shape"))
        key = config.getString("key")
        delay = ExpressionHelper.compileExpression(config.getString("delay"))
        self = ExpressionHelper.compileExpression(config.getString("self"), true)
        follow = ExpressionHelper.compileExpression(config.getString("follow"), true)
    }
}