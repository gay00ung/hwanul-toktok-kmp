package net.ifmain.hwanultoktok.kmp.platform

import platform.Foundation.NSBundle

object ApiKeyProvider {
    fun getApiKey(): String {
        return NSBundle.mainBundle.objectForInfoDictionaryKey("KOREAEXIM_API_KEY") as? String 
            ?: "YOUR_API_KEY_HERE"
    }
    
    fun getAdMobBannerId(): String {
        return NSBundle.mainBundle.objectForInfoDictionaryKey("ADMOB_BANNER_ID") as? String
            ?: "ca-app-pub-3940256099942544/2435281174" // iOS test banner ID as fallback
    }
}