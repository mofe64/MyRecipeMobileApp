package com.nubari.favdish.viewmodel

import androidx.lifecycle.*
import com.nubari.favdish.model.database.FavDishRepository
import com.nubari.favdish.model.entities.FavDIsh
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

/**
 * The ViewModel's role is to provide data to the UI and survive configuration changes.
 * A ViewModel acts as a communication center between the Repository and the UI.
 * You can also use a ViewModel to share data between fragments.
 * The ViewModel is part of the lifecycle library.
 *
 * @param repository - The repository class is
 */
class FavDishViewModel(private val repository: FavDishRepository) : ViewModel() {
    /**
     * Launching a new coroutine to insert the data in a non-blocking way.
     */
    fun insert(dish: FavDIsh) = viewModelScope.launch {
        repository.insertFavDishData(dish)
    }

    /** Using LiveData and caching what allDishes returns has several benefits:
     * We can put an observer on the data (instead of polling for changes) and only
     * update the UI when the data actually changes.
     * Repository is completely separated from the UI through the ViewModel.
     */
    val allDishesList: LiveData<List<FavDIsh>> = repository.allDishesList.asLiveData()

    val favDishesList: LiveData<List<FavDIsh>> = repository.favoriteDishes.asLiveData()

    fun update(dish: FavDIsh) = viewModelScope.launch {
        repository.updateDish(dish)
    }

    fun delete(dish: FavDIsh) {
        viewModelScope.launch {
            repository.delete(dish)
        }
    }
}

/**
 * To create the ViewModel we implement a ViewModelProvider.Factory that gets as a parameter the dependencies
 * needed to create FavDishViewModel: the FavDishRepository.
 * By using viewModels and ViewModelProvider.Factory then the framework will take care of the lifecycle of the ViewModel.
 * It will survive configuration changes and even if the Activity is recreated,
 * you'll always get the right instance of the FavDishViewModel class.
 */
class FavDishViewModelFactory(private val repository: FavDishRepository) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavDishViewModel::class.java)) {
            return FavDishViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}