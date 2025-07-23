package net.ifmain.hwanultoktok.kmp.domain.usecase

import kotlinx.coroutines.flow.Flow
import net.ifmain.hwanultoktok.kmp.domain.model.FavoriteCurrencyPair
import net.ifmain.hwanultoktok.kmp.domain.repository.FavoriteRepository

class GetFavoritesUseCase(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(): Flow<List<FavoriteCurrencyPair>> {
        return favoriteRepository.getAllFavorites()
    }
}