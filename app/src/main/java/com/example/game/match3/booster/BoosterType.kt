package com.example.game.match3.booster

enum class BoosterType(
    val id: String,
    val displayName: String,
    val description: String,
    val iconName: String,
    val unlockLevel: Int
) {
    HAMMER("HAMMER", "Hammer", "Destroys one selected tile or damages an obstacle.", "hammer", 1),
    SWAP("SWAP", "Free Swap", "Swaps any two adjacent tiles without creating a match.", "swap", 1),
    SHUFFLE("SHUFFLE", "Shuffle", "Rearranges all playable tiles on the board.", "shuffle", 1),
    EXTRA_MOVES("EXTRA_MOVES", "+5 Moves", "Adds 5 extra moves to your remaining turns.", "extra_moves", 1),
    ROW_CLEAR("ROW_CLEAR", "Row Clear", "Clears an entire selected row on the board.", "row_clear", 1),
    COLOR_REMOVE("COLOR_REMOVE", "Color Clear", "Removes all tiles of a selected color.", "color_remove", 1),
    COLOR_BOMB("COLOR_BOMB", "Color Bomb", "Spawns a Color Bomb on the board at start.", "color_bomb", 1),
    ROCKET_START("ROCKET_START", "Rocket Start", "Spawns a Rocket tile on the board at start.", "rocket_start", 1),
    BOMB_START("BOMB_START", "Bomb Start", "Spawns a Bomb tile on the board at start.", "bomb_start", 1),
    RAINBOW_START("RAINBOW_START", "Rainbow Start", "Spawns a Rainbow Disco ball on the board at start.", "rainbow_start", 1);

    companion object {
        fun fromId(id: String): BoosterType? {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
        }
    }
}
