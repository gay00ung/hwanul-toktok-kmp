package net.ifmain.hwanultoktok.kmp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.ifmain.hwanultoktok.kmp.database.HwanulDatabase
import net.ifmain.hwanultoktok.kmp.domain.model.FavoriteCurrencyPair
import net.ifmain.hwanultoktok.kmp.domain.repository.FavoriteRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FavoriteRepositoryImpl(
    private val database: HwanulDatabase
): FavoriteRepository {
    override fun getAllFavorites(): Flow<List<FavoriteCurrencyPair>> {
        return database.exchangeRateAlertQueries.getAllFavorites()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { favorites ->
                favorites.map { favorite ->
                    FavoriteCurrencyPair(
                        id = favorite.id,
                        fromCurrencyCode = favorite.fromCurrencyCode,
                        toCurrencyCode = favorite.toCurrencyCode,
                        displayOrder = favorite.displayOrder.toInt(),
                        createdAt = LocalDateTime.parse(favorite.createdAt)
                    )
                }
            }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun addFavorite(
        fromCurrencyCode: String,
        toCurrencyCode: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val newOrder = database.exchangeRateAlertQueries.getAllFavorites().executeAsList().size

                database.exchangeRateAlertQueries.insertFavorite(
                    fromCurrencyCode = fromCurrencyCode,
                    toCurrencyCode = toCurrencyCode,
                    displayOrder = newOrder.toLong(),
                    createdAt = now.toString()
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun removeFavorite(
        fromCurrencyCode: String,
        toCurrencyCode: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                database.exchangeRateAlertQueries.deleteFavorite(fromCurrencyCode, toCurrencyCode)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun isFavorite(
        fromCurrencyCode: String,
        toCurrencyCode: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            database.exchangeRateAlertQueries.checkIsFavorite(fromCurrencyCode, toCurrencyCode)
                .executeAsOne()
        }
    }

    override suspend fun updateDisplayOrder(
        favoriteId: Long,
        newOrder: Int
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                database.exchangeRateAlertQueries.updateDisplayOrder(newOrder.toLong(), favoriteId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}