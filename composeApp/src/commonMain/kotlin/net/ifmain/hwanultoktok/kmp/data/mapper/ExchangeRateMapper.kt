package net.ifmain.hwanultoktok.kmp.data.mapper

import net.ifmain.hwanultoktok.kmp.data.remote.dto.ExchangeRateDto
import net.ifmain.hwanultoktok.kmp.domain.model.ExchangeRate
import net.ifmain.hwanultoktok.kmp.util.getCurrentDateTime

fun ExchangeRateDto.toDomain(): ExchangeRate {
    val currencyCode = currencyUnit.replace(Regex("\\(\\d+\\)"), "").trim()
    
    return ExchangeRate(
        currencyCode = currencyCode,
        currencyName = currencyName,
        currencyUnit = currencyUnit,
        buyingRate = buyingRate?.replace(",", "")?.toDoubleOrNull() ?: 0.0,
        sellingRate = sellingRate?.replace(",", "")?.toDoubleOrNull() ?: 0.0,
        baseRate = baseRate?.replace(",", "")?.toDoubleOrNull() ?: 0.0,
        bookPrice = bookPrice?.replace(",", "")?.toDoubleOrNull() ?: 0.0,
        timestamp = getCurrentDateTime()
    )
}