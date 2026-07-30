package com.example.game.reward

data class RewardBundle(
    val title: String,
    val source: RewardSource,
    val rewards: List<Reward>,
    val referenceId: String? = null
)
