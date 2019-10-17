package com.github.bryanser.artificepro.script

import com.github.bryanser.artificepro.mark.MarkEntityFinder
import com.github.bryanser.artificepro.mark.MarkPlayerFinder
import com.github.bryanser.artificepro.script.finder.*
import java.lang.StringBuilder

object FinderManager {
    val finderTemplates = mutableMapOf<String, FinderTemplate<*>>()

    fun readFinder(func: String): FinderInfo {
        val sp = func.split("\\(".toRegex(), 2)
        val args = mutableListOf<String>()
        val t = finderTemplates[sp[0]] ?: throw IllegalArgumentException("找不到名为${sp[0]}的Finder")
        var deep = 1
        var str = StringBuilder()
        for ((i, s) in sp[1].withIndex()) {
            if (s == '(') {
                deep++
            }
            if (s == ')') {
                deep--
            }
            if (s == ',' && deep == 1) {
                args += str.toString()
                str = StringBuilder()
                continue
            }
            if (deep == 0) {
                args += str.toString()
                break
            }
            str.append(s)
        }
        try {
            return FinderInfo(t.read(args.toTypedArray()), t)
        } catch (e: Exception) {
            throw IllegalArgumentException("Finder编写错误: $func", e)
        }
    }

    fun init() {
        finderTemplates[SightEntityFinderTemplate.name] = SightEntityFinderTemplate
        finderTemplates[SightPlayerFinderTemplate.name] = SightPlayerFinderTemplate
        finderTemplates[RangeEntityFinderTemplate.name] = RangeEntityFinderTemplate
        finderTemplates[RangePlayerFinderTemplate.name] = RangePlayerFinderTemplate
        finderTemplates[SelfFinder.name] = SelfFinder
        finderTemplates[SectorEntityFinderTemplate.name] = SectorEntityFinderTemplate
        finderTemplates[SectorPlayerFinderTemplate.name] = SectorPlayerFinderTemplate
        finderTemplates[EntityLocationFinderTemplate.name] = EntityLocationFinderTemplate
        finderTemplates[SightLocationFinderTemplate.name] = SightLocationFinderTemplate
        finderTemplates[SelfEntityFinder.name] = SelfEntityFinder
        finderTemplates[AttackTargetEntityTemplate.name] = AttackTargetEntityTemplate
        finderTemplates[DefenceFromEntityTemplate.name] = DefenceFromEntityTemplate
        finderTemplates[AttackTargetPlayerTemplate.name] = AttackTargetPlayerTemplate
        finderTemplates[DefenceFromPlayerTemplate.name] = DefenceFromPlayerTemplate
        finderTemplates[MarkPlayerFinder.name] = MarkPlayerFinder
        finderTemplates[MarkEntityFinder.name] = MarkEntityFinder
    }

}

data class FinderInfo(
        val finder: Finder<*>,
        val template: FinderTemplate<*>
)