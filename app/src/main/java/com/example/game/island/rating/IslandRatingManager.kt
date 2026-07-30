package com.example.game.island.rating

import com.example.data.local.entity.BuildingEntity
import com.example.data.local.entity.DecorationEntity
import com.example.data.local.entity.IslandZoneEntity

object IslandRatingManager {

    fun calculateBeautyScore(
        buildings: List<BuildingEntity>,
        decorations: List<DecorationEntity>,
        zones: List<IslandZoneEntity>
    ): Int {
        var score = 0

        // Building points based on level
        for (b in buildings) {
            if (b.isBuilt) {
                score += b.level * 50
            }
        }

        // Decoration points
        score += decorations.size * 30

        // Unlocked Land Zone points
        for (z in zones) {
            if (z.isUnlocked) {
                score += 100
            }
        }

        return score
    }
}
