package com.example.game.world.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.example.core.GameConstants
import com.example.core.GameDataProvider
import com.example.game.reward.Reward
import com.example.game.reward.RewardManager
import com.example.game.reward.RewardType
import com.example.game.world.WorldConfig
import com.example.ui.GameButton
import com.example.utils.TextureFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorldCompletePopup(
    private val worldConfig: WorldConfig,
    private val totalWorldStars: Int,
    private val onContinue: () -> Unit
) : Table() {

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        background = TextureFactory.createRoundedPanel(
            width = 420,
            height = 460,
            fillColor = Color(0.1f, 0.22f, 0.35f, 0.98f),
            borderColor = GameConstants.COLOR_GOLD,
            borderThickness = 4
        )
        setSize(420f, 460f)
        setPosition(
            GameConstants.VIRTUAL_WIDTH / 2f - 210f,
            GameConstants.VIRTUAL_HEIGHT / 2f - 230f
        )
        pad(20f)

        val titleFont = BitmapFont()
        titleFont.data.setScale(1.8f)
        val titleStyle = Label.LabelStyle(titleFont, GameConstants.COLOR_GOLD)
        add(Label("WORLD COMPLETE!", titleStyle)).padBottom(6f).row()

        val subFont = BitmapFont()
        subFont.data.setScale(1.3f)
        val subStyle = Label.LabelStyle(subFont, Color.WHITE)
        add(Label(worldConfig.worldName, subStyle)).padBottom(16f).row()

        val fontMedium = BitmapFont()
        fontMedium.data.setScale(1.1f)
        val infoStyle = Label.LabelStyle(fontMedium, Color.WHITE)
        val goldStyle = Label.LabelStyle(fontMedium, GameConstants.COLOR_GOLD)

        add(Label("10 / 10 Levels Completed", infoStyle)).padBottom(6f).row()
        add(Label("★ World Stars: $totalWorldStars / 30", goldStyle)).padBottom(16f).row()

        val rewardTable = Table()
        rewardTable.background = TextureFactory.createRoundedPanel(
            width = 360,
            height = 110,
            fillColor = Color(0.15f, 0.3f, 0.2f, 0.9f),
            borderColor = Color.GREEN,
            borderThickness = 2
        )
        rewardTable.pad(12f)

        rewardTable.add(Label("WORLD REWARDS:", goldStyle)).padBottom(6f).row()
        rewardTable.add(
            Label("+${worldConfig.rewardCoins} Coins   +${worldConfig.rewardXp} XP   +${worldConfig.rewardGems} Gems", infoStyle)
        ).row()

        add(rewardTable).padBottom(24f).row()

        // Claim Reward via WorldRepository
        scope.launch {
            val claimed = GameDataProvider.worldRepository.claimWorldReward(worldConfig.worldId)
            if (claimed) {
                RewardManager.grantRewards(
                    listOf(
                        Reward(RewardType.COINS, quantity = worldConfig.rewardCoins),
                        Reward(RewardType.XP, quantity = worldConfig.rewardXp),
                        Reward(RewardType.GEMS, quantity = worldConfig.rewardGems)
                    )
                )
            }
        }

        val btnFont = BitmapFont()
        btnFont.data.setScale(1.1f)
        val btnStyle = Label.LabelStyle(btnFont, Color.WHITE)

        val continueBtn = GameButton(
            text = "CONTINUE",
            bgColor = Color(0.15f, 0.65f, 0.25f, 1f),
            labelStyle = btnStyle,
            onClick = {
                remove()
                onContinue()
            }
        )
        add(continueBtn).size(200f, 54f)
    }
}
