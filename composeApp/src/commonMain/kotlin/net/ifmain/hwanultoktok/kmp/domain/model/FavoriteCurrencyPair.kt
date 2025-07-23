package net.ifmain.hwanultoktok.kmp.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteCurrencyPair(
    val id: Long = 0L,
    val fromCurrencyCode: String,
    val toCurrencyCode: String,
    val displayOrder: Int,
    val createdAt: LocalDateTime,
)
 