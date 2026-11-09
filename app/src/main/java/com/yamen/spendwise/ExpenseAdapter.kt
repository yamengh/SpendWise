package com.yamen.spendwise

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yamen.spendwise.data.Expense

/**
 * Connects the list of expenses to the RecyclerView.
 * The RecyclerView only creates enough rows to fill the screen and REUSES them while
 * scrolling; onBindViewHolder puts the data of one expense into a reused row.
 *
 * @param onClick what to do when a row is tapped (MainActivity opens the edit screen)
 */
class ExpenseAdapter(
    private val onClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private var expenses: List<Expense> = emptyList()

    /** Holds the views of one row, so we don't call findViewById again and again. */
    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle: TextView = view.findViewById(R.id.textTitle)
        val textSubtitle: TextView = view.findViewById(R.id.textSubtitle)
        val textAmount: TextView = view.findViewById(R.id.textAmount)
    }

    /** Creates a new row from item_expense.xml (called only a few times). */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    /** Fills a row with the data of the expense at [position]. */
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        val context = holder.itemView.context

        holder.textTitle.text = expense.title
        holder.textSubtitle.text = context.getString(
            R.string.expense_subtitle, expense.category, formatDate(expense.date)
        )
        holder.textAmount.text = context.getString(R.string.amount_ft, formatAmount(expense.amount))
        holder.itemView.setOnClickListener { onClick(expense) }
    }

    override fun getItemCount(): Int = expenses.size

    /** Replaces the whole list and redraws it (the list is small, so this is fine). */
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}
