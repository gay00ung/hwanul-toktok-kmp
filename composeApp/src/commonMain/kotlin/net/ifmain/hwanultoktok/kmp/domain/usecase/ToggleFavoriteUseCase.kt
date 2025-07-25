package net.ifmain.hwanultoktok.kmp.domain.usecase

import net.ifmain.hwanultoktok.kmp.domain.repository.FavoriteRepository

class ToggleFavoriteUseCase(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(
        fromCurrencyCode: String,
        toCurrencyCode: String
    ): Result<Unit> {
        return if (favoriteRepository.isFavorite(fromCurrencyCode, toCurrencyCode)) {
            favoriteRepository.removeFavorite(fromCurrencyCode, toCurrencyCode)
        } else {
            favoriteRepository.addFavorite(fromCurrencyCode, toCurrencyCode)
        }
    }
}