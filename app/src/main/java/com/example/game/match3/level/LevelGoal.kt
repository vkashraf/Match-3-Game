package com.example.game.match3.level

data class LevelGoal(
    val goalType: String,
    val targetAmount: Int,
    var currentAmount: Int = 0
) {
    val isCompleted: Boolean
        get() = currentAmount >= targetAmount
}
