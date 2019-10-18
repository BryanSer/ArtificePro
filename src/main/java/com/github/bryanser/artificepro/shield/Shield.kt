package com.github.bryanser.artificepro.shield

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
import kotlin.math.roundToLong

class Shield : Motion("Shield") {
    lateinit var finder: Finder<LivingEntity>
    lateinit var amount: Expression

    lateinit var time: Expression
    lateinit var color: Color
    lateinit var rate: Expression
    override fun cast(ci: CastInfo) {
        val p = finder(ci)
        val am = amount(ci.caster).toDouble()
        val t = time(ci.caster).toDouble()
        for (e in p) {
            val si = ShieldManager.getShieldInfo(e)
            if (rate(ci.caster).toBoolean()) {
                si += ShieldInfo(am * e.maxHealth, (System.currentTimeMillis() + t * 1000L).roundToLong(), color)
            } else {
                si += ShieldInfo(am, (System.currentTimeMillis() + t * 1000L).roundToLong(), color)
            }
        }
    }

    override fun loadConfig(config: ConfigurationSection) {
        val (f, t) = FinderManager.readFinder(config.getString("Finder"))
        if (t !is EntityFinderTemplate) {
            throw IllegalArgumentException("配置编写错误 Finder类型不是Entity")
        }
        finder = f as Finder<LivingEntity>
        amount = ExpressionHelper.compileExpression(config.getString("amount"))
        time = ExpressionHelper.compileExpression(config.getString("time"))
        rate = ExpressionHelper.compileExpression(config.getString("rate", "false"), true)
        if (config.contains("color")) {
            val c = config.getString("color").split(",")
            color = Color.fromRGB(c[0].toInt(), c[1].toInt(), c[2].toInt())
        } else {
            color = Color.BLUE
        }
    }
}