package net.ifmain.hwanultoktok.kmp.util

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate

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
    val searchDate = getDataBaseDate(koreaTime)

    return searchDate.toString().replace("-", "")
}

fun formatDateTime(dateTime: LocalDateTime): String {
    val dataDate = getDataBaseDate(dateTime)
    return "${dataDate.year}년 ${dataDate.month.number}월 ${dataDate.day}일 고시환율"
}

fun getDataBaseDate(updateTime: LocalDateTime): LocalDate {
    // 업데이트 시간을 기준으로 실제 환율 데이터의 기준일 계산
    val koreaTime = updateTime

    // 11시 이전이면 전날 데이터
    var dataDate = koreaTime.date
    dataDate = if (koreaTime.hour < 11) {
        when (koreaTime.dayOfWeek) {
            DayOfWeek.MONDAY -> dataDate.minus(3, DateTimeUnit.DAY) // 월요일 11시 이전은 금요일
            DayOfWeek.SATURDAY -> dataDate.minus(1, DateTimeUnit.DAY) // 토요일은 금요일
            DayOfWeek.SUNDAY -> dataDate.minus(2, DateTimeUnit.DAY) // 일요일은 금요일
            else -> dataDate.minus(1, DateTimeUnit.DAY) // 다른 평일은 전날
        }
    } else {
        // 11시 이후라도 주말이면 금요일 데이터
        when (koreaTime.dayOfWeek) {
            DayOfWeek.SATURDAY -> dataDate.minus(1, DateTimeUnit.DAY)
            DayOfWeek.SUNDAY -> dataDate.minus(2, DateTimeUnit.DAY)
            else -> dataDate
        }
    }

    return dataDate
}