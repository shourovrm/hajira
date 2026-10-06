package com.rms.hazira.domain

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// Fixed to English symbols: the interface is in English, and a phone set to Bangla would
// otherwise mix Bangla digits into English text and into the shared statement.
private val englishSymbols = DecimalFormatSymbols(Locale.ENGLISH)
private val takaFormat = DecimalFormat("#,##0", englishSymbols)
private val quantityFormat = DecimalFormat("0.##", englishSymbols)

/** 20380 becomes "৳20,380". A negative amount keeps its minus sign in front of the symbol. */
fun formatTaka(amountTaka: Int): String {
    if (amountTaka < 0) {
        return "−৳" + takaFormat.format(-amountTaka.toLong())
    }
    return "৳" + takaFormat.format(amountTaka.toLong())
}

/** 1.0 becomes "1" and 0.5 becomes "0.5": no trailing zeros. */
fun formatQuantity(quantity: Double): String {
    return quantityFormat.format(quantity)
}

/**
 * 2.0 and "litre" become "2 litre". The unit is typed by the user and may be in any language,
 * so it is shown exactly as typed and never pluralised.
 */
fun formatQuantityWithUnit(quantity: Double, unitName: String): String {
    if (unitName.isBlank()) {
        return formatQuantity(quantity)
    }
    return formatQuantity(quantity) + " " + unitName
}
