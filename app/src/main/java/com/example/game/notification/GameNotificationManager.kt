package com.example.game.notification

import com.badlogic.gdx.scenes.scene2d.Stage
import com.example.game.reward.Reward
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class NotificationType {
    LEVEL_UP,
    ACHIEVEMENT_UNLOCKED,
    MISSION_COMPLETED,
    DAILY_REWARD_READY,
    BUILDING_COMPLETED,
    CONTENT_UNLOCKED
}

data class NotificationItem(
    val type: NotificationType,
    val title: String,
    val message: String,
    val rewards: List<Reward> = emptyList(),
    val iconName: String = "star"
)

object GameNotificationManager {

    private val queue = mutableListOf<NotificationItem>()
    private var isDisplaying = false
    private var isGameplayActive = false

    private val _notificationFlow = MutableSharedFlow<NotificationItem>(extraBufferCapacity = 32)
    val notificationFlow: SharedFlow<NotificationItem> = _notificationFlow.asSharedFlow()

    fun setGameplayActive(active: Boolean) {
        isGameplayActive = active
        if (!active) {
            processNextNotification()
        }
    }

    fun enqueue(item: NotificationItem) {
        queue.add(item)
        if (!isGameplayActive && !isDisplaying) {
            processNextNotification()
        }
    }

    fun notifyLevelUp(oldLevel: Int, newLevel: Int, rewards: List<Reward>) {
        enqueue(
            NotificationItem(
                type = NotificationType.LEVEL_UP,
                title = "LEVEL UP!",
                message = "You reached Level $newLevel!",
                rewards = rewards,
                iconName = "star"
            )
        )
    }

    fun notifyAchievement(title: String, rewards: List<Reward>) {
        enqueue(
            NotificationItem(
                type = NotificationType.ACHIEVEMENT_UNLOCKED,
                title = "ACHIEVEMENT UNLOCKED!",
                message = title,
                rewards = rewards,
                iconName = "gem"
            )
        )
    }

    fun notifyUnlock(contentTitle: String) {
        enqueue(
            NotificationItem(
                type = NotificationType.CONTENT_UNLOCKED,
                title = "NEW CONTENT UNLOCKED!",
                message = contentTitle,
                iconName = "key"
            )
        )
    }

    private fun processNextNotification() {
        if (queue.isEmpty() || isGameplayActive) {
            isDisplaying = false
            return
        }
        isDisplaying = true
        val item = queue.removeAt(0)
        _notificationFlow.tryEmit(item)
    }

    fun onNotificationDismissed() {
        isDisplaying = false
        processNextNotification()
    }
}
