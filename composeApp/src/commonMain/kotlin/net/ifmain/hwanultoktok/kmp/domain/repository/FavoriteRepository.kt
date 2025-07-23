package net.ifmain.hwanultoktok.kmp.domain.repository

import kotlinx.coroutines.flow.Flow
import net.ifmain.hwanultoktok.kmp.domain.model.FavoriteCurrencyPair

interface FavoriteRepository {
    fun getAllFavorites(): Flow<List<FavoriteCurrencyPair>>
    suspend fun addFavorite(fromCurrencyCode: String, toCurrencyCode: String): Result<Unit>
    suspend fun removeFavorite(fromCurrencyCode: String, toCurrencyCode: String): Result<Unit>
    suspend fun isFavorite(fromCurrencyCode: String, toCurrencyCode: String): Boolean
    suspend fun updateDisplayOrder(favoriteId: Long, newOrder: Int): Result<Unit>
}