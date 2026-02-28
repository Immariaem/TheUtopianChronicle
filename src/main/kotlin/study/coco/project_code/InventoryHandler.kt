package study.coco.project_code

// handles picking up, dropping and listing items
class InventoryHandler(state: GameState, world: World) : BaseHandler(state, world) {

    // picks up an item from the current quadrant
    fun take(target: String): String {
        if (target.isBlank()) return "Take what? Try: take <name>"

        val item = state.currentQuadrant.visibleObjects.items
            .find { it.itemName.lowercase() == target.lowercase() }
            ?: state.currentQuadrant.visibleObjects.items
            .find { it.itemName.lowercase().contains(target.lowercase()) }
            ?: return "There's no '$target' here to take."

        if (!item.isCollectable) return "You can't take that."

        if (state.playerInventory.size >= 10) return "Your pack is full. You can only carry so much on this journey. Drop something first."

        state.itemSources[item] = state.currentQuadrant.quadrantId
        state.playerInventory.add(item)
        state.currentQuadrant.visibleObjects.items.remove(item)
        return "You pick up ${item.itemName}."
    }

    // drops an item, with special handling for quest items
    fun drop(target: String): String {
        if (target.isBlank()) return "Drop what? Try: drop <name>"

        val item = state.playerInventory.find { it.itemName.lowercase() == target.lowercase() }
            ?: state.playerInventory.find { it.itemName.lowercase().contains(target.lowercase()) }
            ?: return "You don't have '$target' in your inventory."

        // cargo delivery to zara's dock
        if (item.itemId == "zaras_cargo") {
            if (state.currentQuadrant.quadrantId == "B7") {
                state.playerInventory.remove(item)
                state.gameFlags.add("cargo_delivered")
                return "You set down the heavy cargo on Zara's dock. She walks over and inspects it with a nod of approval."
            } else {
                return "You should bring this back to Zara's dock."
            }
        }

        // crystal key placement at the singing chamber console
        if (state.currentQuadrant.quadrantId == "D5" && item.itemName.lowercase().contains("crystal key")) {
            val correctOrder = listOf("blue_crystal_key", "red_crystal_key", "green_crystal_key")
            state.placedKeys.add(item)
            state.playerInventory.remove(item)

            if (state.placedKeys.size == 3) {
                if (state.placedKeys.map { it.itemId } == correctOrder) {
                    state.gameFlags.add("crystal_keys_complete")
                    return "The crystals begin to sing... \n All three keys glow in unison. A light and comfortable ringing echos trough the caves. Kira will be more than happy to help any seeker find their way to the clouds now."
                } else {
                    state.placedKeys.forEach { key -> state.currentQuadrant.visibleObjects.items.add(key) }
                    state.placedKeys.clear()
                    return "The console rejects the sequence. The keys fall to the ground."
                }
            }
            return "You place the ${item.itemName} on the console."
        }

        // relic offering at the pearl guardian altar
        if (item.itemId in listOf("coral_relic", "pearl_relic", "stone_relic")) {
            if (state.currentQuadrant.quadrantId == "H8") {
                state.playerInventory.remove(item)
                state.gameFlags.add("dropped_${item.itemId}")

                if ("dropped_coral_relic" in state.gameFlags && "dropped_pearl_relic" in state.gameFlags && "dropped_stone_relic" in state.gameFlags) {
                    state.gameFlags.add("relics_offered")
                    return "You place ${item.itemName} on the altar. The Pearl Guardian nods. All three relics have been offered. Speak to the Guardian when you are ready."
                }
                return "You place the ${item.itemName} on the altar. The Guardian watches in silence."
            } else {
                return "This does not belong here."
            }
        }

        // generic drop
        state.currentQuadrant.visibleObjects.items.add(item)
        state.itemSources.remove(item)
        state.playerInventory.remove(item)
        return "You drop ${item.itemName}."
    }

    // lists all carried items
    fun inventory(): String {
        if (state.playerInventory.isEmpty()) return "Your inventory is empty."

        val result = StringBuilder("You are carrying:")
        state.playerInventory.forEach { result.appendLine("\n- ${it.itemName}") }
        return result.toString()
    }
}
