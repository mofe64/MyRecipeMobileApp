package com.nubari.favdish.model.database

import androidx.annotation.WorkerThread
import com.nubari.favdish.model.entities.FavDIsh
import kotlinx.coroutines.flow.Flow

/**
 * A Repository manages queries and allows you to use multiple backend.
 *
 * The DAO is passed into the repository constructor as opposed to the whole database.
 * This is because it only needs access to the DAO, since the DAO contains all the read/write methods for the database.
 * There's no need to expose the entire database to the repository.
 *
 * @param favDishDao - Pass the FavDishDao as the parameter.
 */
class FavDishRepository(private val favDishDao: FavDishDao) {

    // enforces that method should be only be called
    // on a worker thread
    // if class is annotated with this, then all methods in class
    // should only be called on a worker thread
    /**
     * By default Room runs suspend queries off the main thread, therefore, we don't need to
     * implement anything else to ensure we're not doing long running database work
     * off the main thread.
     */
    @WorkerThread
    suspend fun insertFavDishData(favDish: FavDIsh) {
        favDishDao.insertFavDishDetails(favDish)
    }

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allDishesList: Flow<List<FavDIsh>> = favDishDao.getAllDishesList()

    @WorkerThread
    suspend fun updateDish(updatedDetails: FavDIsh) {
        favDishDao.updateFavDishDetails(updatedDetails)
    }

    suspend fun delete(favDish: FavDIsh) {
        favDishDao.delete(favDish)
    }

    val favoriteDishes: Flow<List<FavDIsh>> = favDishDao.getFavs()

    fun filter(value: String): Flow<List<FavDIsh>> = favDishDao.filter(value)
}