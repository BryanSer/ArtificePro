package com.github.bryanser.artificepro.motion

import org.bukkit.entity.*

enum class ProjectileType(
        val clazz: Class<out Projectile>
) {
    ARROW(Arrow::class.java),
    EGG(Egg::class.java),
    DRAGONFIREBALL(DragonFireball::class.java),
    LARGEFIREBALL(LargeFireball::class.java),
    SHULKERBULLET(ShulkerBullet::class.java),
    SMALLFIREBALL(SmallFireball::class.java),
    SNOWBALL(Snowball::class.java),
    WITHERSKULL(WitherSkull::class.java)
}