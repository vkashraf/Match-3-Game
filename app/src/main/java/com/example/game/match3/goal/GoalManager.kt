package com.example.game.match3.goal

import com.example.game.match3.level.LevelGoal
import com.example.game.match3.obstacle.ObstacleType
import com.example.game.match3.tile.TileType

class GoalManager(val goals: List<LevelGoal>) {

    fun onTilesCleared(clearedTiles: List<TileType>) {
        val counts = clearedTiles.groupingBy { it.name }.eachCount()

        for (goal in goals) {
            val wasCompleted = goal.isCompleted
            val amount = counts[goal.goalType] ?: 0
            if (amount > 0) {
                goal.currentAmount = Math.min(goal.targetAmount, goal.currentAmount + amount)
                if (!wasCompleted && goal.isCompleted) {
                    com.example.core.event.GameEventBus.postEvent(
                        com.example.core.event.GameEvent(
                            type = com.example.core.event.GameEventType.GOAL_COMPLETED,
                            itemId = goal.goalType
                        )
                    )
                }
            }
        }
    }

    fun onObstaclesDestroyed(destroyedObstacles: List<ObstacleType>) {
        val counts = destroyedObstacles.groupingBy { it.name }.eachCount()

        for (goal in goals) {
            val wasCompleted = goal.isCompleted
            val amount = counts[goal.goalType] ?: 0
            if (amount > 0) {
                goal.currentAmount = Math.min(goal.targetAmount, goal.currentAmount + amount)
                if (!wasCompleted && goal.isCompleted) {
                    com.example.core.event.GameEventBus.postEvent(
                        com.example.core.event.GameEvent(
                            type = com.example.core.event.GameEventType.GOAL_COMPLETED,
                            itemId = goal.goalType
                        )
                    )
                }
            }
        }
    }

    fun isAllGoalsCompleted(): Boolean {
        return goals.all { it.isCompleted }
    }
}
