package com.yamen.spendwise

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.yamen.spendwise.data.AppDatabase
import com.yamen.spendwise.data.Expense
import com.yamen.spendwise.data.ExpenseDao
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * One screen for both ADDING and EDITING an expense.
 * - Opened without an id  -> add a new expense.
 * - Opened with an id     -> load that expense, edit it, or delete it.
 */
class AddEditExpenseActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXPENSE_ID = "expense_id"
        private const val KEY_DATE = "selected_date"
    }

    private lateinit var dao: ExpenseDao

    private lateinit var layoutAmount: TextInputLayout
    private lateinit var layoutTitle: TextInputLayout
    private lateinit var layoutCategory: TextInputLayout
    private lateinit var editAmount: EditText
    private lateinit var editTitle: EditText
    private lateinit var dropdownCategory: MaterialAutoCompleteTextView
    private lateinit var editDate: EditText
    private lateinit var editNote: EditText
    private lateinit var buttonDelete: Button

    private var expenseId = 0                    // 0 = we are adding a new expense
    private var currentExpense: Expense? = null  // the loaded expense when editing
    private var selectedDate = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_expense)

        // Keep content away from the system bars and above the keyboard
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        dao = AppDatabase.getDatabase(this).expenseDao()

        layoutAmount = findViewById(R.id.layoutAmount)
        layoutTitle = findViewById(R.id.layoutTitle)
        layoutCategory = findViewById(R.id.layoutCategory)
        editAmount = findViewById(R.id.editAmount)
        editTitle = findViewById(R.id.editTitle)
        dropdownCategory = findViewById(R.id.dropdownCategory)
        editDate = findViewById(R.id.editDate)
        editNote = findViewById(R.id.editNote)
        buttonDelete = findViewById(R.id.buttonDelete)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        expenseId = intent.getIntExtra(EXTRA_EXPENSE_ID, 0)
        val isEditing = expenseId != 0

        toolbar.setTitle(if (isEditing) R.string.title_edit_expense else R.string.title_add_expense)
        toolbar.setNavigationOnClickListener { finish() }

        // After a screen rotation, keep the date the user picked
        if (savedInstanceState != null) {
            selectedDate = savedInstanceState.getLong(KEY_DATE, selectedDate)
        }
        editDate.setText(formatDate(selectedDate))
        editDate.setOnClickListener { showDatePicker() }

        // Remove a field's error as soon as the user changes that field
        editAmount.doAfterTextChanged { layoutAmount.error = null }
        editTitle.doAfterTextChanged { layoutTitle.error = null }
        dropdownCategory.doAfterTextChanged { layoutCategory.error = null }

        findViewById<Button>(R.id.buttonSave).setOnClickListener { saveExpense() }
        buttonDelete.setOnClickListener { confirmDelete() }

        if (isEditing) {
            // Load the expense in the background, then fill in the form
            lifecycleScope.launch {
                val expense = dao.getById(expenseId) ?: return@launch
                currentExpense = expense
                buttonDelete.visibility = View.VISIBLE
                // Only fill the fields the first time (after rotation they keep what the user typed)
                if (savedInstanceState == null) {
                    fillForm(expense)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_DATE, selectedDate)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // After rotation Android puts the chosen category back into the dropdown, and that
        // filters the list down to just that one item. Setting the items again shows all of them.
        dropdownCategory.setSimpleItems(R.array.categories)
    }

    private fun fillForm(expense: Expense) {
        editAmount.setText(expense.amount.toString())
        editTitle.setText(expense.title)
        dropdownCategory.setText(expense.category, false) // false = don't filter the list
        editNote.setText(expense.note)
        selectedDate = expense.date
        editDate.setText(formatDate(selectedDate))
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = calendar.timeInMillis
                editDate.setText(formatDate(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /** Checks the input, shows an error under each wrong field, and saves only if all is OK. */
    private fun saveExpense() {
        val amountText = editAmount.text.toString().trim()
        val title = editTitle.text.toString().trim()
        val category = dropdownCategory.text.toString()
        val note = editNote.text.toString().trim()
        val amount = amountText.toLongOrNull()

        // Clear old errors first
        layoutAmount.error = null
        layoutTitle.error = null
        layoutCategory.error = null

        var isValid = true
        if (amountText.isEmpty()) {
            layoutAmount.error = getString(R.string.error_amount_empty)
            isValid = false
        } else if (amount == null || amount <= 0) {
            layoutAmount.error = getString(R.string.error_amount_invalid)
            isValid = false
        }
        if (title.isEmpty()) {
            layoutTitle.error = getString(R.string.error_title_empty)
            isValid = false
        }
        if (category.isEmpty()) {
            layoutCategory.error = getString(R.string.error_category_empty)
            isValid = false
        }
        if (!isValid || amount == null) return

        val expense = Expense(
            id = expenseId,
            amount = amount,
            title = title,
            category = category,
            date = selectedDate,
            note = note
        )

        // Database work runs in the background (suspend functions), then we close the screen
        lifecycleScope.launch {
            if (expenseId == 0) {
                dao.insert(expense)
            } else {
                dao.update(expense)
            }
            Toast.makeText(this@AddEditExpenseActivity, R.string.toast_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /** Asks "are you sure?" before deleting, because deleting cannot be undone. */
    private fun confirmDelete() {
        val expense = currentExpense ?: return

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_dialog_title)
            .setMessage(getString(R.string.delete_dialog_message, expense.title))
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.button_delete) { _, _ ->
                lifecycleScope.launch {
                    dao.delete(expense)
                    Toast.makeText(this@AddEditExpenseActivity, R.string.toast_deleted, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .show()
    }
}
