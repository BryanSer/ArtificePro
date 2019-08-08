package com.github.bryanser.artificepro.script

class ExpressionResult(
        val value: Double
) : Number() {

    constructor(b: Boolean) : this(if (b) 1.0 else -1.0)

    override fun toChar(): Char = value.toChar()
    fun toBoolean(): Boolean = value > 0

    override fun toByte(): Byte = value.toByte()
    override fun toDouble(): Double = value
    override fun toFloat(): Float = value.toFloat()
    override fun toInt(): Int = value.toInt()
    override fun toLong(): Long = value.toLong()
    override fun toShort(): Short = value.toShort()
}