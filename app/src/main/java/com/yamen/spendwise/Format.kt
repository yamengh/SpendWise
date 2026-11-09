package com.yamen.spendwise

import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Date
import java.util.Locale

/**
 * Formats a Forint amount with a space between thousands: 4500 -> "4 500".
 * The " Ft" part comes from strings.xml (R.string.amount_ft).
 */
fun formatAmount(amount: Long): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    symbols.groupingSeparator = ' '
    return DecimalFormat("#,##0", symbols).format(amount)
}

/** Formats a date (milliseconds) in the phone's language, e.g. "Nov 9, 2026". */
fun formatDate(millis: Long): String {
    return DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(millis))
}
