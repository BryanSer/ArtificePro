package com.github.bryanser.artificepro.script

import com.github.bryanser.artificepro.script.finder.EntityFinder
import com.github.bryanser.artificepro.script.finder.Finder
import com.github.bryanser.artificepro.script.finder.FinderTemplate
import org.bukkit.entity.LivingEntity

object FinderManager {
    /*
     * 参数: 最远搜索长度, 是否选择玩家
    */
    object SightEntityFinder:EntityFinder<LivingEntity>("SightEntity"){

        override fun read(args: Array<String>): Finder<LivingEntity> {
            val range = args[0].toDouble()
            val player = args[1].toBoolean()
            return if(player){
                {
                    TODO()
                }
            }else{
                {
                    TODO()

                }
            }
        }

    }
}