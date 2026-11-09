package com.yamen.spendwise

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yamen.spendwise.data.AppDatabase

/** Main screen: the total at the top, the list of expenses, and a + button. */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Show the splash screen (logo on green). Must be called BEFORE super.onCreate().
        // When the activity is ready, it switches to the normal app theme by itself.
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Keep content away from the status bar and navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val textTotal = findViewById<TextView>(R.id.textTotal)
        val layoutEmpty = findViewById<View>(R.id.layoutEmpty)
        val recyclerExpenses = findViewById<RecyclerView>(R.id.recyclerExpenses)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        // Tapping a row opens the edit screen for that expense
        val adapter = ExpenseAdapter { expense ->
            val intent = Intent(this, AddEditExpenseActivity::class.java)
            intent.putExtra(AddEditExpenseActivity.EXTRA_EXPENSE_ID, expense.id)
            startActivity(intent)
        }
        recyclerExpenses.layoutManager = LinearLayoutManager(this)
        recyclerExpenses.adapter = adapter

        // + button opens the same screen, but without an id = "add new"
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditExpenseActivity::class.java))
        }

        // Observe the database: this block runs now AND every time an expense is
        // added, edited or deleted, so the screen is always up to date.
        val dao = AppDatabase.getDatabase(this).expenseDao()
        dao.getAll().observe(this) { expenses ->
            adapter.submitList(expenses)

            val total = expenses.sumOf { it.amount }
            textTotal.text = getString(R.string.amount_ft, formatAmount(total))

            layoutEmpty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
