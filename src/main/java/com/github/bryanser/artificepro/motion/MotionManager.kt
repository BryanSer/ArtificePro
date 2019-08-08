package com.github.bryanser.artificepro.motion

import org.bukkit.configuration.ConfigurationSection
import java.lang.IllegalArgumentException

object MotionManager {
    private val motions = mutableMapOf<String, Class<out Motion>>()
    fun init() {
        registerMotion("Scattering", Scattering::class.java)
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
            throw IllegalArgumentException("读取动作失败@ $config", e)
        }
        return t
    }
}