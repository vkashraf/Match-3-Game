package com.example.game.economy

import java.util.Locale

object NumberFormatter {
    fun formatCompact(number: Long): String {
        return when {
            number < 1_000 -> number.toString()
            number < 1_000_000 -> {
                val valK = number / 1000.0
                if (number % 1000 == 0L) String.format(Locale.US, "%.0fK", valK)
                else String.format(Locale.US, "%.1fK", valK)
            }
            else -> {
                val valM = number / 1_000_000.0
                if (number % 1_000_000 == 0L) String.format(Locale.US, "%.0fM", valM)
                else String.format(Locale.US, "%.1fM", valM)
            }
        }
    }

    fun formatResourceWithCapacity(current: Int, maxCapacity: Int): String {
        return "${formatCompact(current.toLong())} / ${formatCompact(maxCapacity.toLong())}"
    }
}
