package com.yamen.spendwise.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One expense = one row in the "expenses" table.
 * Room creates the table from this class (each property becomes a column).
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,          // 0 = new expense, Room gives it a real id when inserted
    val amount: Long,         // in Forint, whole numbers only (e.g. 4500)
    val title: String,
    val category: String,
    val date: Long,           // date as milliseconds since 1970 (easy to store and sort)
    val note: String = ""
)
