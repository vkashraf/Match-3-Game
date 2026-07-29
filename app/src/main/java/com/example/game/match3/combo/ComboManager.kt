package com.example.game.match3.combo

class ComboManager {

    var currentCombo: Int = 0
        private set

    fun reset() {
        currentCombo = 0
    }

    fun increment(): Int {
        currentCombo++
        return currentCombo
    }

    fun getMultiplier(): Float {
        return when (currentCombo) {
            0, 1 -> 1.0f
            2 -> 1.25f
            3 -> 1.5f
            4 -> 2.0f
            else -> 2.5f
        }
    }
}
