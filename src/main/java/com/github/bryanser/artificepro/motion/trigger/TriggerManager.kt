package com.github.bryanser.artificepro.motion.trigger

import org.bukkit.configuration.ConfigurationSection

object TriggerManager {
    val registerTriggers = mutableMapOf<String, Class<out Trigger>>()

    fun init() {
        registerTriggers["DamageTrigger"] = DamageTrigger::class.java
        registerTriggers["KnockTrigger"] = KnockTrigger::class.java
        registerTriggers["EffectTrigger"] = EffectTrigger::class.java
        registerTriggers["LaunchItemTrigger"] = LaunchItemTrigger::class.java
        registerTriggers["ShapeTrigger"] = ShapeTrigger::class.java
    }

    fun loadTrigger(config: ConfigurationSection): Trigger {
        val type = config.getString("Type")
        val tc = registerTriggers[type] ?: throw IllegalArgumentException("找不到名为${type}的触发器")
        val c = tc.newInstance()
        c.load(config)
        return c
    }
}