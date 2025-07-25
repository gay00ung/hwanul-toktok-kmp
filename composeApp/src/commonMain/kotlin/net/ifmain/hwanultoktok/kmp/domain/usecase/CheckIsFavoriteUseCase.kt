package net.ifmain.hwanultoktok.kmp.domain.usecase

import net.ifmain.hwanultoktok.kmp.domain.repository.FavoriteRepository

/**
 * Use case for checking if a currency pair is marked as favorite.
 *
 * @param fromCurrencyCode The source currency code (e.g., "USD")
 * @param toCurrencyCode The target currency code (e.g., "KRW")
 * @return Boolean indicating whether the currency pair is in favorites
 */
class CheckIsFavoriteUseCase(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(
        fromCurrencyCode: String,
        toCurrencyCode: String
    ): Boolean {
        return favoriteRepository.isFavorite(fromCurrencyCode, toCurrencyCode)
    }
}