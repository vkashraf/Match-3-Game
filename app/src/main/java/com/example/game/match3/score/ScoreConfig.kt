package com.example.game.match3.score

data class FloatingText(
    val text: String,
    var x: Float,
    var y: Float,
    var alpha: Float = 1f,
    var elapsed: Float = 0f,
    val duration: Float = 0.8f
) {
    val isDead: Boolean get() = elapsed >= duration

    fun update(delta: Float) {
        elapsed += delta
        y += delta * 40f // Float upward
        alpha = (1f - elapsed / duration).coerceIn(0f, 1f)
    }
}

object ScoreConfig {
    fun calculateScore(matchedTileCount: Int, comboMultiplier: Float): Int {
        val baseScore = when (matchedTileCount) {
            3 -> 30
            4 -> 60
            5 -> 100
            else -> 150 + (matchedTileCount - 6) * 30
        }
        return (baseScore * comboMultiplier).toInt()
    }
}
