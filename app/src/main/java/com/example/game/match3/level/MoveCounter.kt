package com.example.game.match3.level

class MoveCounter(var movesRemaining: Int = 28) {
    fun consumeMove(): Boolean {
        if (movesRemaining > 0) {
            movesRemaining--
            return true
        }
        return false
    }

    fun addMoves(amount: Int) {
        if (amount > 0) {
            movesRemaining += amount
        }
    }
}
