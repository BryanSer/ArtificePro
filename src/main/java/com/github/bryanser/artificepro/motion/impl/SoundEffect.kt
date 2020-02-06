package com.github.bryanser.artificepro.motion.impl

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.github.bryanser.artificepro.Main
import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.motion.Motion
import com.github.bryanser.artificepro.script.FinderManager
import com.github.bryanser.artificepro.script.finder.Finder
import com.github.bryanser.artificepro.script.finder.PlayerFinderTemplate
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

class SoundEffect : Motion("SoundEffect") {
    lateinit var finder: Finder<Player>

    lateinit var packet: () -> PacketContainer
    override fun cast(ci: CastInfo) {
        val os = finder(ci)
        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
            for (p in os) {
                val loc = p.location
                val pc = packet()
                pc.integers.write(0, loc.blockX * 8)
                pc.integers.write(1, loc.blockY * 8)
                pc.integers.write(2, loc.blockZ * 8)
                ProtocolLibrary.getProtocolManager().sendServerPacket(p, pc, false)
            }
        }
    }

    override fun loadConfig(config: ConfigurationSection) {
        val sound = Sound.valueOf(config.getString("sound").toUpperCase())

        val pm = ProtocolLibrary.getProtocolManager()
        val pc = pm.createPacket(PacketType.Play.Server.NAMED_SOUND_EFFECT)
        pc.soundEffects.write(0, sound)
        pc.soundCategories.write(0, EnumWrappers.SoundCategory.VOICE)
        pc.float.write(0, config.getDouble("volume").toFloat())
        pc.float.write(1, config.getDouble("pitch").toFloat())
        packet = {
            pc.deepClone()
        }

        val (f, t) = FinderManager.readFinder(config.getString("Finder", "Self()"))
        if (t !is PlayerFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Player")
        }
        finder = f as Finder<Player>
    }
}

class CustomSoundEffect : Motion("CustomSoundEffect") {
    lateinit var finder: Finder<Player>

    lateinit var packet: () -> PacketContainer
    override fun cast(ci: CastInfo) {
        val os = finder(ci)
        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
            for (p in os) {
                val loc = p.location
                val pc = packet()
                pc.integers.write(0, loc.blockX * 8)
                pc.integers.write(1, loc.blockY * 8)
                pc.integers.write(2, loc.blockZ * 8)
                ProtocolLibrary.getProtocolManager().sendServerPacket(p, pc, false)
            }
        }
    }

    override fun loadConfig(config: ConfigurationSection) {
        val sound = config.getString("sound")

        val pm = ProtocolLibrary.getProtocolManager()
        val pc = pm.createPacket(PacketType.Play.Server.CUSTOM_SOUND_EFFECT)
        pc.strings.write(0, sound)
        pc.soundCategories.write(0, EnumWrappers.SoundCategory.VOICE)
        pc.float.write(0, config.getDouble("volume").toFloat())
        pc.float.write(1, config.getDouble("pitch").toFloat())
        packet = {
            pc.deepClone()
        }

        val (f, t) = FinderManager.readFinder(config.getString("Finder", "Self()"))
        if (t !is PlayerFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Player")
        }
        finder = f as Finder<Player>
    }
}