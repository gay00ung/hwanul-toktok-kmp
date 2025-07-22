package net.ifmain.hwanultoktok.kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform