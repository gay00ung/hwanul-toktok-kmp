package net.ifmain.hwanultoktok.kmp.platform

import platform.Foundation.NSBundle

object ApiKeyProvider {
    fun getApiKey(): String {
        return NSBundle.mainBundle.objectForInfoDictionaryKey("KOREAEXIM_API_KEY") as? String 
            ?: "YOUR_API_KEY_HERE"
    }
    
    fun getHolidayApiKey(): String {
        return NSBundle.mainBundle.objectForInfoDictionaryKey("KOREA_HOLIDAY_API_KEY_ENCODING") as? String
            ?: "KOREA_HOLIDAY_API_KEY_DECODING"
    }
}
