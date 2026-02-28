package study.coco.project_code

// handles player movement, diving and death respawn
class MovementHandler(state: GameState,
                      world: World,
                      private val dialogue: DialogueHandler) : BaseHandler(state, world) {

    fun move(direction: String): String {

        // check survival before moving
        if (state.saturation <= 0) {
            return respawn("You died of starvation! You have respawned at your last checkpoint.")
        }

        if (state.hydration <= 0) {
            return respawn("You died of dehydration! You have respawned at your last checkpoint.")
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

        // surface lock: must dive at E8 before moving anywhere
        if (state.currentQuadrant.quadrantId == "E8" && "entered_underwater" !in state.gameFlags) {
            return "You are floating on the surface of the open ocean. Water stretches in every direction with nothing but sky above. The only way forward is down. Type 'dive' to descend into the underwater realm."
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

    // flags earned within each checkpoint section / all removed on death in that section
    private val sectionFlags = mapOf(
        "B2" to setOf(
            "spirit_rescued"
        ),
        "C4" to setOf(
            "viewed_blue_tablet", "viewed_red_tablet", "viewed_green_tablet",
            "crystal_keys_complete"
        ),
        "A7" to setOf(
            "cargo_delivered"
        ),
        "F5" to setOf(
            "found_star_point",
            "met_hermit", "received_compass",
            "star_navigation_complete"
        ),
        "E8" to setOf(
            "dropped_coral_relic", "dropped_pearl_relic", "dropped_stone_relic", "relics_offered",
            "guardian_trial_complete"
        ),
        "I4" to setOf(
            "met_smiling_ones", "found_father", "mirror_labyrinth_complete"
        )
    )

    // restores items to their source quadrants and sends player to last checkpoint
    private fun respawn(cause: String): String {
        // restore all taken items (inventory, placed keys, consumed) back to their source quadrants
        state.itemSources.forEach { (item, sourceId) ->
            val sourceQuadrant = world.quadrants.find { it.quadrantId == sourceId }
            if (sourceQuadrant != null && item !in sourceQuadrant.visibleObjects.items) {
                sourceQuadrant.visibleObjects.items.add(item)
            }
        }
        state.itemSources.clear()
        state.playerInventory.clear()
        state.placedKeys.clear()

        // reset puzzle state variables
        state.labyrinthActive = false
        state.labyrinthQuestion = 0
        state.labyrinthAwaitingMove = false
        state.currentGuardianQuestion = 0
        state.guardianAnswersCorrect = true

        // reset all flags earned in the current section so the player must replay it
        val clearedFlags = sectionFlags[state.checkpointQuadrantId] ?: emptySet()
        clearedFlags.forEach { state.gameFlags.remove(it) }

        // un-visit any quadrant whose unlockFlags overlap with the cleared flags
        // so they can re-trigger on the next visit (e.g. met_hermit from E2, star_navigation_complete from G4)
        world.quadrants.forEach { q ->
            if (q.unlockFlags.any { it in clearedFlags }) {
                state.visitedQuadrants.remove(q.quadrantId)
            }
        }

        val checkpoint = world.quadrants.firstOrNull { it.quadrantId == state.checkpointQuadrantId }
            ?: world.quadrants.first { it.quadrantId == "B2" }
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