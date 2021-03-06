package com.knz21.android.recipeviewer.presentation

import com.knz21.android.recipeviewer.R
import com.knz21.android.recipeviewer.domain.struct.FavoriteTime
import com.knz21.android.recipeviewer.domain.struct.RecipesStruct
import com.knz21.android.recipeviewer.extension.addTo
import com.knz21.android.recipeviewer.extension.applySchedulers
import com.knz21.android.recipeviewer.infra.repository.RecipeRepository
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class RecipePresenter @Inject constructor(private val contract: Contract, private val repository: RecipeRepository) {
    private val disposables = CompositeDisposable()
    private var isAll = true

    fun getRecipes() {
        repository.getRecipes()
                .applySchedulers()
                .subscribe(
                        { contract.showRecipes(it) },
                        { Timber.e(it, "#getRecipes") }
                )
                .addTo(disposables)
    }

    fun dispose() {
        disposables.dispose()
    }

    fun toggleFavorite(id: String, isFavorite: Boolean) {
        if (isAll) repository.toggleFavorite(id, isFavorite)
                .applySchedulers()
                .subscribe(
                        { contract.toggleFavorite(id, it) },
                        { Timber.e(it, "#toggleFavorite") }
                )
                .addTo(disposables)
    }

    fun toggleRecipeList(recipes: RecipesStruct, tabId: Int) {
        when (tabId) {
            R.id.navigation_all -> contract.updateList(recipes).apply { isAll = true }
            R.id.navigation_favorites ->
                contract.updateList(RecipesStruct(recipes.data.filter { it.favoriteTime != null }
                        .sortedBy { it.favoriteTime?.time?.times(-1) }.toMutableList()))
                        .apply { isAll = false }
        }
    }

    interface Contract {
        fun showRecipes(recipes: RecipesStruct)

        fun toggleFavorite(id: String, time: FavoriteTime)

        fun updateList(recipes: RecipesStruct)
    }
}
