package com.example.game.match3.goal

import com.example.game.match3.level.LevelGoal
import com.example.game.match3.obstacle.ObstacleType
import com.example.game.match3.tile.TileType

class GoalManager(val goals: List<LevelGoal>) {

    fun onTilesCleared(clearedTiles: List<TileType>) {
        val counts = clearedTiles.groupingBy { it.name }.eachCount()

        for (goal in goals) {
            val amount = counts[goal.goalType] ?: 0
            if (amount > 0) {
                goal.currentAmount = Math.min(goal.targetAmount, goal.currentAmount + amount)
            }
        }
    }

    fun onObstaclesDestroyed(destroyedObstacles: List<ObstacleType>) {
        val counts = destroyedObstacles.groupingBy { it.name }.eachCount()

        for (goal in goals) {
            val amount = counts[goal.goalType] ?: 0
            if (amount > 0) {
                goal.currentAmount = Math.min(goal.targetAmount, goal.currentAmount + amount)
            }
        }
    }

    fun isAllGoalsCompleted(): Boolean {
        return goals.all { it.isCompleted }
    }
}
