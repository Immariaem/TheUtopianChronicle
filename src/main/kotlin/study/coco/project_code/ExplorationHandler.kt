package study.coco.project_code

// handles looking around and examining objects
class ExplorationHandler(state: GameState, world: World) : BaseHandler(state, world) {

    // describes the current quadrant including npcs, items and interactables
    fun look(): String {
        val result = StringBuilder()
        result.appendLine(state.currentQuadrant.description.atmospheric)

        val npcs = state.currentQuadrant.visibleObjects.npcs.filter { it.isActive }
        if (npcs.isNotEmpty()) {
            result.appendLine("\nYou see:")
            npcs.forEach { result.appendLine("- ${it.npcName}") }
        }

        val items = state.currentQuadrant.visibleObjects.items
        if (items.isNotEmpty()) {
            result.appendLine("\nItems:")
            items.forEach { result.appendLine("- ${it.itemName}") }
        }

        val interactables = state.currentQuadrant.visibleObjects.interactables
        if (interactables.isNotEmpty()) {
            result.appendLine("\nYou notice:")
            interactables.forEach { result.appendLine("- ${it.name}") }
        }

        return result.toString()
    }

    // shows detailed text for an interactable or item
    fun examine(target: String): String {
        if (target.isBlank()) return "Examine what? Try: examine <name>"

        val interactable = state.currentQuadrant.visibleObjects.interactables
            .find { it.name.lowercase() == target.lowercase() }

        if (interactable != null) return interactable.examineText

        val item = state.currentQuadrant.visibleObjects.items
            .find { it.itemName.lowercase() == target.lowercase() }

        if (item != null) return item.itemMessage ?: item.itemName

        return "You don't see '$target' here."
    }

}
