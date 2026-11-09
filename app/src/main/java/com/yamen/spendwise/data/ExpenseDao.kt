package com.yamen.spendwise.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * DAO = Data Access Object: all the database operations we need.
 * Room writes the actual code for these functions at compile time.
 */
@Dao
interface ExpenseDao {

    // LiveData: the screen is notified automatically every time the table changes
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAll(): LiveData<List<Expense>>

    // "suspend" = runs in the background, so the UI never freezes
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Int): Expense?

    @Insert
    suspend fun insert(expense: Expense)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}
