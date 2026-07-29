package com.example.game.match3.level

object StarCalculator {
    /**
     * Calculates 0, 1, 2, or 3 stars based on final score and goals completion.
     */
    fun calculateStars(
        allGoalsCompleted: Boolean,
        finalScore: Int,
        baseTarget: Int
    ): Int {
        if (!allGoalsCompleted) return 0
        if (finalScore >= (baseTarget * 1.5f).toInt()) return 3
        if (finalScore >= (baseTarget * 1.2f).toInt()) return 2
        return 1
    }
}
