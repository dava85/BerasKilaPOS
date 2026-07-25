package com.example.neotokopos85.utils

import java.text.NumberFormat
import java.util.*

fun rupiah(number: Number): String {
    val format = NumberFormat.getNumberInstance(Locale("in", "ID"))
    return "Rp ${format.format(number)}"
}