package com.github.bryanser.artificepro.mark

import org.bukkit.Color
import java.util.*

data class MarkInfo(
        val mark: UUID,
        val from: UUID,
        val endTime: Long,
        val color: Color,
        val key: String
){
    fun remove(){
        MarkManager.markDatas[mark]?.beenMarked?.remove(this)
        MarkManager.markDatas[from]?.marks?.remove(this)
    }
}

data class MarkData(
        val player: UUID
) {

    val marks = mutableListOf<MarkInfo>()
    val beenMarked = mutableListOf<MarkInfo>()
}