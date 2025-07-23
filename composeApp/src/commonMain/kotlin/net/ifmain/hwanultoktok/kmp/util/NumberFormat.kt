package net.ifmain.hwanultoktok.kmp.util

import kotlin.math.pow
import kotlin.math.round

fun Double.format(decimals: Int = 2): String {
    val multiplier = 10.0.pow(decimals)
    val rounded = round(this * multiplier) / multiplier
    return if (decimals == 0) {
        rounded.toInt().toString()
    } else {
        val intPart = rounded.toInt()
        val decimalPart = ((rounded - intPart) * multiplier).toInt()
        val decimalStr = decimalPart.toString().padStart(decimals, '0')
        "$intPart.$decimalStr"
    }
}