package com.github.bryanser.artificepro.mark

import com.github.bryanser.artificepro.motion.CastInfo
import com.github.bryanser.artificepro.motion.Motion
import com.github.bryanser.artificepro.script.Expression
import com.github.bryanser.artificepro.script.ExpressionHelper
import com.github.bryanser.artificepro.script.FinderManager
import com.github.bryanser.artificepro.script.finder.EntityFinderTemplate
import com.github.bryanser.artificepro.script.finder.Finder
import org.bukkit.Color
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity

class Mark : Motion("Mark") {

    lateinit var time: Expression
    lateinit var color: Color
    lateinit var finder: Finder<LivingEntity>
    lateinit var key: String
    override fun cast(ci: CastInfo) {
        val endTime = System.currentTimeMillis() + (time(ci.caster).toDouble() * 50L).toLong()
        val cdata = MarkManager.getData(ci.caster.uniqueId)
        for (target in finder(ci)) {
            val data = MarkManager.getData(target.uniqueId)
            val it = data.beenMarked.iterator()
            while (it.hasNext()) {
                val info = it.next()
                if (info.key == this.key && info.from == ci.caster.uniqueId) {
                    it.remove()
                    cdata.marks.remove(info)
                }
            }
            val info = MarkInfo(target.uniqueId, ci.caster.uniqueId, endTime, color, key)
            data.beenMarked += info
            cdata.marks += info
        }
    }

    override fun loadConfig(config: ConfigurationSection) {
        val (f, t) = FinderManager.readFinder(config.getString("Finder"))
        if (t !is EntityFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Entity")
        }
        finder = f as Finder<LivingEntity>
        time = ExpressionHelper.compileExpression(config.getString("time"))
        key = config.getString("Key")
        if (config.contains("color")) {
            val c = config.getString("color").split(",")
            color = Color.fromRGB(c[0].toInt(), c[1].toInt(), c[2].toInt())
        } else {
            color = Color.BLUE
        }
    }
}