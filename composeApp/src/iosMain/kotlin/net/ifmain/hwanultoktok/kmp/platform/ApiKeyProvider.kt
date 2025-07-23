package net.ifmain.hwanultoktok.kmp.platform

import platform.Foundation.NSBundle

object ApiKeyProvider {
    fun getApiKey(): String {
        return NSBundle.mainBundle.objectForInfoDictionaryKey("KOREAEXIM_API_KEY") as? String 
            ?: "YOUR_API_KEY_HERE"
    }
}