package com.example.game.match3.score

class ScoreManager(var currentScore: Int = 0) {
    fun addScore(points: Int) {
        if (points > 0) {
            currentScore += points
        }
    }
}
