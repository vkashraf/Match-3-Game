package com.example.data.repository

object PlayerLevelConfig {

    /**
     * Required cumulative XP to reach a given player level.
     */
    fun getRequiredXpForLevel(level: Int): Long {
        if (level <= 1) return 0L
        val lvl = level - 1
        return (lvl * 100L) + (lvl * (lvl - 1) * 50L)
    }

    /**
     * Calculates the level based on total accumulated XP.
     */
    fun getLevelForXp(totalXp: Long): Int {
        var level = 1
        while (totalXp >= getRequiredXpForLevel(level + 1)) {
            level++
        }
        return level
    }
}
