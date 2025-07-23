package net.ifmain.hwanultoktok.kmp.domain.usecase

import net.ifmain.hwanultoktok.kmp.domain.repository.FavoriteRepository

/**
 *
 * @author gayoung.
 * @since 2025. 7. 23.
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