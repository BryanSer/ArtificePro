package com.github.bryanser.artificepro.motion

import org.bukkit.configuration.ConfigurationSection
import java.lang.IllegalArgumentException

object MotionManager {
    private val motions = mutableMapOf<String, Class<out Motion>>()
    fun init() {
        registerMotion("Scattering", Scattering::class.java)
        registerMotion("Command", Command::class.java)
        registerMotion("GuidedArrow", GuidedArrow::class.java)
        registerMotion("Charge", Charge::class.java)
        registerMotion("Heal", Heal::class.java)
        registerMotion("Flash", Flash::class.java)
        registerMotion("Jump", Jump::class.java)
        registerMotion("Effect", Effect::class.java)
        registerMotion("FlamesColumn", FlamesColumn::class.java)
        registerMotion("Damage", Damage::class.java)
        registerMotion("Knock", Knock::class.java)
    }

    fun registerMotion(name: String, cls: Class<out Motion>) {
        motions[name] = cls
    }

    fun loadMotion(config: ConfigurationSection): Motion {
        val name = config.getString("Name")
        val m = motions[name] ?: throw IllegalArgumentException("找不到动作: $name")
        val t: Motion?
        try {
            t = m.newInstance()
            t.loadConfig(config.getConfigurationSection("Config"))
        } catch (e: Exception) {
            throw IllegalArgumentException("读取动作失败@ ${config}", e)
        }
        return t
    }
}