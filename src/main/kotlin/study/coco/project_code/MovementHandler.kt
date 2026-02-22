package study.coco.project_code

// handles player movement, diving and death respawn
class MovementHandler(state: GameState,
                      world: World,
                      private val dialogue: DialogueHandler) : BaseHandler(state, world) {

    fun move(direction: String): String {

        // check survival before moving
        if (state.hydration <= 0) {
            return respawn("You died of dehydration! You have respawned at your last checkpoint.")
        }

        if (state.saturation <= 0) {
            return respawn("You died of starvation! You have respawned at your last checkpoint.")
        }

        // labyrinth movement gate
        if (state.labyrinthActive && state.currentQuadrant.quadrantId == "J4") {
            if (!state.labyrinthAwaitingMove) {
                return "Your reflection stands before you, waiting. Answer before you move on."
            }
            state.labyrinthAwaitingMove = false
            return dialogue.nextLabyrinthQuestion()
        }

        // labyrinth exit lock
        if (state.currentQuadrant.quadrantId == "J4" && direction == "west" && "mirror_labyrinth_complete" !in state.gameFlags) {
            return "The western wall is solid mirrors. There is no way through. Not yet."
        }

        // resolve direction to connection
        val connection = when (direction.lowercase()) {
            "north" -> state.currentQuadrant.connections.north
            "south" -> state.currentQuadrant.connections.south
            "east" -> state.currentQuadrant.connections.east
            "west" -> state.currentQuadrant.connections.west
            else -> return "Unknown direction: $direction"
        }

        // check if path is blocked
        if (connection.blocked) {
            return connection.blockedMessage ?: "You cannot go that way."
        }

        val targetId = connection.quadrantId ?: return "There's nothing in that direction."
        val target = world.quadrants.find { it.quadrantId == targetId }
            ?: return "Error: quadrant $targetId not found."

        // check required story flags
        if (target.requiredFlags.isNotEmpty()) {
            val missingFlags = target.requiredFlags.filter { it !in state.gameFlags }
            if (missingFlags.isNotEmpty()) {
                return "You can't go there yet. Something is blocking your path."
            }
        }

        // move player and update stats
        state.hydration -= 2
        state.saturation -= 1
        state.currentQuadrant = target

        // first visit unlocks flags and sets description
        var message = if (state.currentQuadrant.quadrantId in state.visitedQuadrants) {
            state.currentQuadrant.description.returnText
        } else {
            if (state.currentQuadrant.unlockFlags.isNotEmpty()) {
                state.currentQuadrant.unlockFlags.forEach { flag ->
                    state.gameFlags.add(flag)
                }
            }
            state.visitedQuadrants.add(state.currentQuadrant.quadrantId)

            // update checkpoint if this is a checkpoint quadrant
            if (state.currentQuadrant.quadrantId in state.checkpointQuadrants) {
                state.checkpointQuadrantId = state.currentQuadrant.quadrantId
            }
            state.currentQuadrant.description.initial
        }

        // low stat warnings
        if (state.hydration <= 8) {
            message += "\n\nWarning: You're getting thirsty! Find water soon."
        }

        if (state.saturation <= 5) {
            message += "\n\nWarning: You're getting hungry! Find food soon."
        }

        return message
    }

    // restores items to their source quadrants and sends player to last checkpoint
    private fun respawn(cause: String): String {
        state.playerInventory.forEach { item ->
            val sourceId = state.itemSources[item]
            val sourceQuadrant = world.quadrants.find { it.quadrantId == sourceId }
            sourceQuadrant?.visibleObjects?.items?.add(item)
        }
        state.playerInventory.clear()

        state.placedKeys.forEach { key ->
            val sourceId = state.itemSources[key]
            val sourceQuadrant = world.quadrants.find { it.quadrantId == sourceId }
            sourceQuadrant?.visibleObjects?.items?.add(key)
        }
        state.placedKeys.clear()
        state.itemSources.clear()

        val checkpoint = world.quadrants.first { it.quadrantId == state.checkpointQuadrantId }
        state.currentQuadrant = checkpoint
        state.hydration = 20
        state.saturation = 20
        return "CHECKPOINT:$cause"
    }

    // transitions player into the underwater realm
    fun dive(): String {
        if (state.currentQuadrant.quadrantId != "E8") return "There is nowhere to dive here."
        if ("entered_underwater" in state.gameFlags) return "You have already dived. The underwater realm is to the east."

        state.gameFlags.add("entered_underwater")
        return "You clutch Kira's crystal and dive beneath the waves. As the water closes over you, the crystal pulses with warmth and light. You open your mouth, expecting to choke, but instead you breathe. The crystal hums softly as the underwater realm opens up around you. Go east to explore."
    }

}