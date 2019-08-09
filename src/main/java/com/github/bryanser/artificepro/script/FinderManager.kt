package com.github.bryanser.artificepro.script

import com.github.bryanser.artificepro.script.finder.*

object FinderManager {
    val finderTemplates = mutableMapOf<String, FinderTemplate<*>>()

    fun readFinder(func: String): FinderInfo {
        val sp = func.split("[()]".toRegex(), 3)
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
        finderTemplates[SelfFinder.name] = SelfFinder
    }

}

data class FinderInfo(
        val finder: Finder<*>,
        val template: FinderTemplate<*>
)