package net.ifmain.hwanultoktok.kmp.util

/**
 * 통화 관련 유틸리티 함수들
 * 
 * @author gayoung
 * @since 2025. 8. 14.
 */
object CurrencyUtils {

    fun getOfficialCurrencyUnit(currencyCode: String): String {
        return when (val normalizedCode = currencyCode.uppercase()) {
            "JPY", "JPY(100)" -> "JPY(100)"
            "IDR", "IDR(100)" -> "IDR(100)"
            else -> normalizedCode
        }
    }
    
    /**
     * 통화별 이모지 반환
     */
    fun getCurrencyEmoji(currencyCode: String): String {
        return when (currencyCode.uppercase()) {
            "USD" -> "🇺🇸"
            "JPY" -> "🇯🇵"
            "EUR" -> "🇪🇺"
            "CNY" -> "🇨🇳"
            "GBP" -> "🇬🇧"
            "CHF" -> "🇨🇭"
            "CAD" -> "🇨🇦"
            "AUD" -> "🇦🇺"
            "HKD" -> "🇭🇰"
            "SGD" -> "🇸🇬"
            "THB" -> "🇹🇭"
            "MYR" -> "🇲🇾"
            "TWD" -> "🇹🇼"
            "NZD" -> "🇳🇿"
            "SEK" -> "🇸🇪"
            "NOK" -> "🇳🇴"
            "DKK" -> "🇩🇰"
            else -> "💱"
        }
    }
    
    /**
     * 통화별 이름 반환
     */
    fun getCurrencyName(currencyCode: String): String {
        return when (currencyCode.uppercase()) {
            "USD" -> "미국 달러"
            "JPY" -> "일본 엔"
            "EUR" -> "유럽 유로"
            "CNY" -> "중국 위안"
            "GBP" -> "영국 파운드"
            "CHF" -> "스위스 프랑"
            "CAD" -> "캐나다 달러"
            "AUD" -> "호주 달러"
            "HKD" -> "홍콩 달러"
            "SGD" -> "싱가포르 달러"
            "THB" -> "태국 바트"
            "MYR" -> "말레이시아 링깃"
            "TWD" -> "대만 달러"
            "NZD" -> "뉴질랜드 달러"
            "SEK" -> "스웨덴 크로나"
            "NOK" -> "노르웨이 크로네"
            "DKK" -> "덴마크 크로네"
            else -> "외화"
        }
    }
    
    /**
     * 통화별 전체 이름 반환 (이모지 + 코드 + 이름)
     */
    fun getCurrencyFullName(currencyCode: String): String {
        val emoji = getCurrencyEmoji(currencyCode)
        val name = getCurrencyName(currencyCode)
        return "$emoji $currencyCode ($name)"
    }
    
    /**
     * 환율 변동 방향에 따른 이모지 반환
     */
    fun getChangeEmoji(change: String): String {
        return when {
            change.startsWith("▲") || change.startsWith("+") -> "📈"
            change.startsWith("▼") || change.startsWith("-") -> "📉"
            else -> "➖"
        }
    }
    
    /**
     * 환율 포맷팅 (소수점 처리) - 간단한 버전
     */
    fun formatExchangeRate(rate: Double): String {
        return when {
            rate >= 1000 -> "${rate.toInt()}.${((rate % 1) * 100).toInt().toString().padStart(2, '0')}"
            rate >= 100 -> "${rate.toInt()}.${((rate % 1) * 100).toInt().toString().padStart(2, '0')}"
            rate >= 10 -> "${rate.toInt()}.${((rate % 1) * 1000).toInt().toString().padStart(3, '0')}"
            else -> "${rate.toInt()}.${((rate % 1) * 10000).toInt().toString().padStart(4, '0')}"
        }
    }
}
