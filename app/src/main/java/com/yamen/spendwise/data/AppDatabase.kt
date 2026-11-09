package com.yamen.spendwise.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The Room database. It lists the tables (entities) and gives access to the DAO.
 * We keep only ONE instance for the whole app (singleton), because opening
 * a database is expensive.
 */
@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // If it already exists, return it; otherwise create it (only once, thread-safe)
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spendwise_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
