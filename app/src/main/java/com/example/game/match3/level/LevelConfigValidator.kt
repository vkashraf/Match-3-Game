package com.example.game.match3.level

object LevelConfigValidator {
    fun validate(config: LevelConfig): Boolean {
        if (config.levelId < 1 || config.levelId > 200) return false
        if (config.moves <= 0) return false
        if (config.goals.isEmpty()) return false
        for (g in config.goals) {
            if (g.targetAmount <= 0) return false
        }
        val set = mutableSetOf<Pair<Int, Int>>()
        for (obs in config.obstacleLayouts) {
            if (obs.row !in 0..7 || obs.col !in 0..7) return false
            val pair = Pair(obs.row, obs.col)
            if (set.contains(pair)) return false
            set.add(pair)
        }
        if (config.coinReward < 0 || config.xpReward < 0) return false
        return true
    }
}
