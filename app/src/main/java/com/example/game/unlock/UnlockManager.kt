package com.example.game.unlock

import com.example.core.event.GameEvent
import com.example.core.event.GameEventBus
import com.example.core.event.GameEventType

enum class RequirementType {
    PLAYER_LEVEL,
    WORLD_COMPLETED,
    STARS,
    BUILDING_LEVEL,
    ACHIEVEMENT,
    MISSION
}

data class UnlockRequirement(
    val requirementType: RequirementType,
    val value: Int,
    val description: String
)

data class UnlockItem(
    val contentType: String, // "BUILDING", "ZONE", "WORLD", "DECORATION", "BOOSTER"
    val contentId: String,
    val title: String,
    val requirement: UnlockRequirement
)

object UnlockManager {

    private val unlockRegistry = mutableListOf(
        UnlockItem("BUILDING", "FARM", "Farm Area", UnlockRequirement(RequirementType.PLAYER_LEVEL, 1, "Reach Player Level 1")),
        UnlockItem("BUILDING", "MINE", "Mining Quarry", UnlockRequirement(RequirementType.PLAYER_LEVEL, 3, "Reach Player Level 3")),
        UnlockItem("BUILDING", "WORKSHOP", "Crafting Workshop", UnlockRequirement(RequirementType.PLAYER_LEVEL, 5, "Reach Player Level 5")),
        UnlockItem("BUILDING", "BAKERY", "Island Bakery", UnlockRequirement(RequirementType.PLAYER_LEVEL, 6, "Reach Player Level 6")),
        UnlockItem("BUILDING", "HARBOR", "Trading Harbor", UnlockRequirement(RequirementType.PLAYER_LEVEL, 8, "Reach Player Level 8")),
        UnlockItem("BUILDING", "ACADEMY", "Magic Academy", UnlockRequirement(RequirementType.PLAYER_LEVEL, 12, "Reach Player Level 12")),

        UnlockItem("ZONE", "3", "Mining Zone", UnlockRequirement(RequirementType.PLAYER_LEVEL, 3, "Reach Player Level 3")),
        UnlockItem("ZONE", "4", "Workshop Zone", UnlockRequirement(RequirementType.PLAYER_LEVEL, 5, "Reach Player Level 5")),
        UnlockItem("ZONE", "5", "Harbor Zone", UnlockRequirement(RequirementType.PLAYER_LEVEL, 8, "Reach Player Level 8")),
        UnlockItem("ZONE", "6", "Magic Zone", UnlockRequirement(RequirementType.PLAYER_LEVEL, 12, "Reach Player Level 12")),

        UnlockItem("WORLD", "2", "Lush Forest World", UnlockRequirement(RequirementType.STARS, 15, "Earn 15 Stars")),
        UnlockItem("WORLD", "3", "Volcano Peak World", UnlockRequirement(RequirementType.STARS, 40, "Earn 40 Stars"))
    )

    fun getUnlockRequirement(contentType: String, contentId: String): String {
        val item = unlockRegistry.firstOrNull { it.contentType == contentType && it.contentId == contentId }
        return item?.requirement?.description ?: "Locked"
    }

    fun isUnlocked(
        contentType: String,
        contentId: String,
        playerLevel: Int,
        totalStars: Int,
        completedWorldId: Int = 1
    ): Boolean {
        val item = unlockRegistry.firstOrNull { it.contentType == contentType && it.contentId == contentId } ?: return true
        val req = item.requirement
        return when (req.requirementType) {
            RequirementType.PLAYER_LEVEL -> playerLevel >= req.value
            RequirementType.STARS -> totalStars >= req.value
            RequirementType.WORLD_COMPLETED -> completedWorldId >= req.value
            else -> true
        }
    }

    suspend fun checkAndNotifyUnlocks(
        playerLevel: Int,
        totalStars: Int,
        completedWorldId: Int = 1
    ) {
        for (item in unlockRegistry) {
            if (isUnlocked(item.contentType, item.contentId, playerLevel, totalStars, completedWorldId)) {
                GameEventBus.emit(
                    GameEvent(
                        type = GameEventType.CONTENT_UNLOCKED,
                        itemId = item.contentId,
                        buildingId = item.contentType
                    )
                )
            }
        }
    }
}
