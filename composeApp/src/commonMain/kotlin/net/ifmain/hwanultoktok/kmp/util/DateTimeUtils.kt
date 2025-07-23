package net.ifmain.hwanultoktok.kmp.util

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.*

@OptIn(ExperimentalTime::class)
fun getCurrentDateTime(): LocalDateTime {
    val now = Clock.System.now()
    val timeZone = TimeZone.currentSystemDefault()
    return now.toLocalDateTime(timeZone)
}

@OptIn(ExperimentalTime::class)
fun getExchangeRateSearchDate(): String {
    val now = Clock.System.now()
    val koreaTimeZone = TimeZone.of("Asia/Seoul")
    val koreaTime = now.toLocalDateTime(koreaTimeZone)
    
    // 주말 체크 (토요일: 6, 일요일: 7)
    var searchDate = koreaTime.date
    if (koreaTime.dayOfWeek == DayOfWeek.SATURDAY) {
        searchDate = searchDate.minus(1, DateTimeUnit.DAY)
    } else if (koreaTime.dayOfWeek == DayOfWeek.SUNDAY) {
        searchDate = searchDate.minus(2, DateTimeUnit.DAY)
    }
    
    // 평일이라도 오전 11시 이전이면 전날 데이터 조회
    if (koreaTime.hour < 11 && koreaTime.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
        searchDate = when (koreaTime.dayOfWeek) {
            DayOfWeek.MONDAY -> searchDate.minus(3, DateTimeUnit.DAY) // 월요일 11시 이전은 금요일 데이터
            else -> searchDate.minus(1, DateTimeUnit.DAY) // 다른 평일은 전날 데이터
        }
    }
    
    // YYYYMMDD 형식으로 변환
    return searchDate.toString().replace("-", "")
}

fun formatDateTime(dateTime: LocalDateTime): String {
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.year}년 ${dateTime.monthNumber}월 ${dateTime.dayOfMonth}일 $hour:$minute 기준"
}