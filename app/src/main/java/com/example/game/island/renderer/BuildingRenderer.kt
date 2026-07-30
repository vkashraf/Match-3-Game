package com.example.game.island.renderer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.example.data.local.entity.BuildingEntity
import com.example.data.local.entity.BuildingPlotEntity
import com.example.data.local.entity.DecorationEntity
import com.example.game.island.building.ConstructionManager
import com.example.game.island.building.ProductionManager
import com.example.utils.TextureFactory

class BuildingRenderer(
    private val batch: SpriteBatch,
    private val shapeRenderer: ShapeRenderer,
    private val font: BitmapFont
) {

    private val plotBoundsMap = mutableMapOf<String, Rectangle>()

    fun renderPlotsAndBuildings(
        plots: List<BuildingPlotEntity>,
        buildingsMap: Map<String, BuildingEntity>,
        decorations: List<DecorationEntity> = emptyList(),
        currentTime: Long = System.currentTimeMillis()
    ) {
        plotBoundsMap.clear()

        // 1. Draw Buildings & Plots with SpriteBatch
        batch.begin()
        for (plot in plots) {
            val bounds = Rectangle(plot.x - 55f, plot.y - 55f, 110f, 110f)
            plotBoundsMap[plot.plotId] = bounds

            val building = plot.buildingId?.let { buildingsMap[it] }

            if (building != null) {
                if (building.isConstructing) {
                    // Draw Scaffolding
                    val scaffoldTex = TextureFactory.createBuildingTexture("scaffolding", 110, 110)
                    scaffoldTex.draw(batch, bounds.x, bounds.y, bounds.width, bounds.height)

                    // Draw Timer text
                    val remainingSecs = ConstructionManager.getRemainingSeconds(building, currentTime)
                    val timeStr = ConstructionManager.formatRemainingTime(remainingSecs)
                    font.data.setScale(0.9f)
                    font.color = Color.YELLOW
                    font.draw(batch, timeStr, bounds.x + 15f, bounds.y + bounds.height + 20f)
                } else if (building.isBuilt) {
                    // Draw Building
                    val buildingTex = TextureFactory.createBuildingTexture(building.buildingType, 110, 110)
                    buildingTex.draw(batch, bounds.x, bounds.y, bounds.width, bounds.height)

                    // Draw Level badge
                    val badgeTex = TextureFactory.createCircleTexture(32, Color(0.12f, 0.16f, 0.24f, 0.9f), Color(1.0f, 0.8f, 0.2f, 1f))
                    badgeTex.draw(batch, bounds.x - 5f, bounds.y + bounds.height - 25f, 32f, 32f)

                    font.data.setScale(0.85f)
                    font.color = Color.WHITE
                    font.draw(batch, "${building.level}", bounds.x + 4f, bounds.y + bounds.height - 3f)

                    // Draw Collect Icon if resource accumulated
                    val pendingAmount = ProductionManager.calculatePendingAmount(building, currentTime)
                    if (pendingAmount > 0) {
                        val resType = ProductionManager.getProductionType(building)
                        val resIcon = TextureFactory.createIcon(resType.iconName, 38)
                        resIcon.draw(batch, bounds.x + bounds.width / 2f - 19f, bounds.y + bounds.height + 5f, 38f, 38f)
                    }
                }
            } else {
                if (plot.isUnlocked) {
                    // Draw Empty Plot sign
                    val emptyTex = TextureFactory.createBuildingTexture("plot_empty", 110, 110)
                    emptyTex.draw(batch, bounds.x, bounds.y, bounds.width, bounds.height)
                } else {
                    // Draw Locked Plot
                    val lockedTex = TextureFactory.createBuildingTexture("plot_locked", 110, 110)
                    lockedTex.draw(batch, bounds.x, bounds.y, bounds.width, bounds.height)
                }
            }
        }

        // 2. Draw Decorations
        for (dec in decorations) {
            val decX = dec.gridX * 60f + 100f
            val decY = dec.gridY * 60f + 200f
            val decTex = TextureFactory.createBuildingTexture(dec.decorationId.lowercase(), 60, 60)
            decTex.draw(batch, decX, decY, 60f, 60f)
        }

        batch.end()

        // 3. Draw Construction Progress Bars with ShapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (plot in plots) {
            val building = plot.buildingId?.let { buildingsMap[it] }
            if (building != null && building.isConstructing) {
                val bounds = Rectangle(plot.x - 55f, plot.y - 55f, 110f, 110f)
                val progress = ConstructionManager.getProgressFraction(building, currentTime)

                // Background bar
                shapeRenderer.color = Color(0.15f, 0.15f, 0.2f, 0.85f)
                shapeRenderer.rect(bounds.x, bounds.y - 12f, bounds.width, 10f)

                // Progress fill
                shapeRenderer.color = Color(0.2f, 0.82f, 0.35f, 1f)
                shapeRenderer.rect(bounds.x, bounds.y - 12f, bounds.width * progress, 10f)
            }
        }
        shapeRenderer.end()
    }

    /**
     * Hit tests screen touch coordinate against plots to find clicked plot.
     */
    fun getPlotAt(worldX: Float, worldY: Float, plots: List<BuildingPlotEntity>): BuildingPlotEntity? {
        for (plot in plots) {
            val bounds = plotBoundsMap[plot.plotId] ?: Rectangle(plot.x - 55f, plot.y - 55f, 110f, 110f)
            if (bounds.contains(worldX, worldY)) {
                return plot
            }
        }
        return null
    }
}
