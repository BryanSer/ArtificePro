package com.github.bryanser.artificepro.script

import com.github.bryanser.artificepro.Tools
import com.github.bryanser.artificepro.script.finder.EntityFinderTemplate
import com.github.bryanser.artificepro.script.finder.Finder
import com.github.bryanser.artificepro.script.finder.FinderTemplate
import com.github.bryanser.artificepro.script.finder.PlayerFinderTemplate
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.logging.Level
import java.util.logging.Logger

object FinderManager {
    val finderTemplates = mutableMapOf<String, FinderTemplate<*>>()

    fun readFinder(func: String): FinderInfo {
        val sp = func.split("[()]".toRegex(), 2)
        val t = finderTemplates[sp[0]] ?: throw IllegalArgumentException("找不到名为${sp[0]}的Finder")
        val arg = sp[1].replace(" ", "").split(",")
        try {
            return FinderInfo(t.read(arg.toTypedArray()), t)
        } catch (e: Exception) {
            throw IllegalArgumentException("Finder编写错误: $func", e)
        }
    }

    fun init() {
        finderTemplates[SightEntityFinderTemplate.name] = SightEntityFinderTemplate
        finderTemplates[SightPlayerFinderTemplate.name] = SightPlayerFinderTemplate
        finderTemplates[RangeEntityFinderTemplate.name] = RangeEntityFinderTemplate
        finderTemplates[RangePlayerFinderTemplate.name] = RangePlayerFinderTemplate
    }

    /*
     * 参数: 最远搜索长度, 是否选择玩家
    */
    object SightEntityFinderTemplate : EntityFinderTemplate<LivingEntity>("SightEntity") {
        override fun read(args: Array<String>): Finder<LivingEntity> {
            val range = args[0].toDouble()
            val player = args[1].toBoolean()
            return if (player) {
                {
                    val t = Tools.getLookAtEntity(it, range, range.toInt() * 2)
                    if (t != null) {
                        listOf(t)
                    } else {
                        listOf()
                    }
                }
            } else {
                {
                    val t = Tools.getLookAtEntity(it, range, range.toInt() * 2) {
                        it is LivingEntity && it !is Player
                    }
                    if (t != null) {
                        listOf(t)
                    } else {
                        listOf()
                    }
                }
            }
        }
    }

    /*
     * 参数: 最远搜索长度
    */
    object SightPlayerFinderTemplate : PlayerFinderTemplate("SightPlayer") {
        override fun read(args: Array<String>): Finder<Player> {
            val range = args[0].toDouble()
            return {
                val t = Tools.getLookAtEntity(it, range, range.toInt() * 2) {
                    it is Player
                } as? Player
                if (t != null) {
                    listOf(t)
                } else {
                    listOf()
                }
            }
        }
    }

    /*
     * 参数: 搜寻半径, 搜寻数量(0表示无限), 是否搜索玩家, 是否包括自身
     */
    object RangeEntityFinderTemplate : EntityFinderTemplate<LivingEntity>("RangeEntity") {
        val filters = arrayOf<(LivingEntity, Player) -> Boolean>(
                { e, c ->
                    e !is Player && e != c
                },
                { e, c ->
                    e is Player && e != c
                },
                { e, c ->
                    e !is Player || e == c
                },
                { e, c ->
                    e is Player || e != c
                }
        )

        override fun read(args: Array<String>): Finder<LivingEntity> {
            val r = args[0].toDouble()
            val max = args[1].toInt()
            val player = if (args[2].toBoolean()) 1 else 0
            val self = if (args[3].toBoolean()) 2 else 0
            val filter = filters[player + self]
            return { p ->
                val list = mutableListOf<LivingEntity>()
                var count = 0
                for (e in p.getNearbyEntities(r, r, r)) {
                    if (max > 0 && count >= max) {
                        break
                    }
                    if (e !is LivingEntity) {
                        continue
                    }
                    if (filter(e, p)) {
                        list += e
                        count++
                    }
                }
                list
            }
        }

    }

    /*
     * 参数: 搜寻半径, 搜寻数量(0表示无限), 是否包括自身
     */
    object RangePlayerFinderTemplate : PlayerFinderTemplate("RangePlayer") {
        override fun read(args: Array<String>): Finder<Player> {
            val r = args[0].toDouble()
            val max = args[1].toInt()
            val self = args[2].toBoolean()
            return {
                val list = mutableListOf<Player>()
                var count = 0
                for (e in it.getNearbyEntities(r, r, r)) {
                    if (max > 0 && count >= max) {
                        break
                    }
                    if (e !is Player) {
                        continue
                    }
                    if (!self) {
                        if (e === it) {
                            continue
                        }
                    }
                    list += e
                    count++
                }
                list
            }

        }

    }
}

data class FinderInfo(
        val finder: Finder<*>,
        val template: FinderTemplate<*>
)