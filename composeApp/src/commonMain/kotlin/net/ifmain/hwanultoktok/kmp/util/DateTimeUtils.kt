package net.ifmain.hwanultoktok.kmp.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import net.ifmain.hwanultoktok.kmp.domain.usecase.GetHolidaysUseCase
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun getCurrentDateTime(): LocalDateTime {
    val now = Clock.System.now()
    val timeZone = TimeZone.currentSystemDefault()
    return now.toLocalDateTime(timeZone)
}

@OptIn(ExperimentalTime::class)
suspend fun getExchangeRateSearchDate(getHolidaysUseCase: GetHolidaysUseCase): String {
    val now = Clock.System.now()
    val koreaTimeZone = TimeZone.of("Asia/Seoul")
    val koreaTime = now.toLocalDateTime(koreaTimeZone)
    val searchDate = getDataBaseDate(koreaTime, getHolidaysUseCase)

    return searchDate.toString().replace("-", "")
}

// 공휴일 체크 포함 버전
suspend fun formatDateTime(
    dateTime: LocalDateTime,
    getHolidaysUseCase: GetHolidaysUseCase
): String {
    val dataDate = getDataBaseDate(dateTime, getHolidaysUseCase)
    return "${dataDate.year}년 ${dataDate.month.number}월 ${dataDate.day}일 고시환율"
}

// 공휴일 체크 없는 기존 로직
fun getDataBaseDateWithoutHoliday(updateTime: LocalDateTime): LocalDate {
    val koreaTime = updateTime
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
            DayOfWeek.SATURDAY -> dataDate.minus(1, DateTimeUnit.DAY) // 토요일은 금요일
            DayOfWeek.SUNDAY -> dataDate.minus(2, DateTimeUnit.DAY) // 일요일은 금요일
            else -> dataDate // 평일은 당일
        }
    }

    return dataDate
}

suspend fun isHoliday(date: LocalDate, getHolidaysUseCase: GetHolidaysUseCase): Boolean {
    val holidays = getHolidaysUseCase(date.year, date.month.number).getOrNull() ?: return false
    val dateLong = "${date.year}${date.month.number.toString().padStart(2, '0')}${
        date.day.toString().padStart(2, '0')
    }".toLong()
    return holidays.any { it.locdate == dateLong && it.isHoliday == "Y" }
}

fun isWeekend(data: LocalDate): Boolean {
    return data.dayOfWeek == DayOfWeek.SATURDAY || data.dayOfWeek == DayOfWeek.SUNDAY
}

suspend fun isBusinessDay(date: LocalDate, getHolidaysUseCase: GetHolidaysUseCase): Boolean {
    return !isWeekend(date) && !isHoliday(date, getHolidaysUseCase)
}

suspend fun findPreviousBusinessDay(
    startDate: LocalDate,
    getHolidaysUseCase: GetHolidaysUseCase
): LocalDate {
    var date = startDate
    while (!isBusinessDay(date, getHolidaysUseCase)) {
        date = date.minus(1, DateTimeUnit.DAY)
    }
    return date
}

suspend fun getDataBaseDate(
    updateTime: LocalDateTime,
    getHolidaysUseCase: GetHolidaysUseCase
): LocalDate {
    // 업데이트 시간을 기준으로 실제 환율 데이터의 기준일 계산
    val koreaTime = updateTime

    // 11시 이전이면 전날 데이터
    var dataDate = koreaTime.date

    dataDate = if (koreaTime.hour < 11) {
        findPreviousBusinessDay(dataDate.minus(1, DateTimeUnit.DAY), getHolidaysUseCase)
    } else {
        // 11시 이후라도 오늘이 영업일이 아니면 이전 영업일
        if (isBusinessDay(dataDate, getHolidaysUseCase)) {
            dataDate
        } else {
            findPreviousBusinessDay(dataDate, getHolidaysUseCase)
        }
    }

    return dataDate
}