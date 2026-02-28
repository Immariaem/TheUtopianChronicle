package study.coco.project_code

// handles using inventory items and interacting with objects in the world
class UseHandler (state: GameState, world: World): BaseHandler(state,world) {

    fun use(target: String): String {
        if (target.isBlank()) return "Use what? Try: use <name>"

        val inventoryItem = state.playerInventory.find { it.itemName.lowercase() == target.lowercase() }
            ?: state.playerInventory.find { it.itemName.lowercase().contains(target.lowercase()) }
        val correctOrder = listOf("blue_crystal_key", "red_crystal_key", "green_crystal_key")

        // item found in inventory
        if (inventoryItem != null) {

            // crystal key puzzle in the singing chamber
            if (state.currentQuadrant.quadrantId == "D5" && inventoryItem.itemName.lowercase().contains("crystal key")) {
                state.placedKeys.add(inventoryItem)
                state.playerInventory.remove(inventoryItem)

                if (state.placedKeys.size == 3) {

                    // crystal quest
                    if (state.placedKeys.map { it.itemId } == correctOrder) {
                        state.gameFlags.add("crystal_keys_complete")
                        return  "The crystals begin to sing... \n All three keys glow in unison. A light and comfortable ringing echos trough the caves. Kira will be more than happy to help any seeker find their way to the clouds now."
                    } else {
                        state.placedKeys.forEach { key ->
                            state.currentQuadrant.visibleObjects.items.add(key)
                        }
                        state.placedKeys.clear()
                        return "The console rejects the sequence. The keys fall to the ground."
                    }
                }
                return "You place the ${inventoryItem.itemName} on the console."
            }

            // desert compass quest
            if (state.currentQuadrant.quadrantId == "F2" && inventoryItem.itemId == "fathers_compass") {
                state.gameFlags.add("found_star_point")
                return "EPHEMERAL:The compass needle spins wildly, then locks in place. A vision burns into your mind: \n\n\"You see three stars lighting up in the night sky. The first sinks below the horizon. The second and third rise side by side where the sun rises each day follow them twice. The cove awaits the seeker worthy of understanding.\"\n\nThe compass goes still."
            }

            if (inventoryItem.itemType == "consumable") {
                val itemId = inventoryItem.itemId.lowercase()

                if (itemId.contains("water") || itemId == "coconut" || itemId ==
                    "cactus_fruit") {
                    state.hydration = minOf(state.hydration + 5,20)
                }

                if (!itemId.contains("water") || itemId == "coconut" || itemId ==
                    "cactus_fruit") {
                    state.saturation = minOf(state.saturation + 5, 20)
                }

                state.playerInventory.remove(inventoryItem)
            }
            return inventoryItem.itemMessage ?: "Nothing happens."
        }

        // target not in inventory, check interactables
        val interactable = state.currentQuadrant.visibleObjects.interactables
            .find { it.name.lowercase() == target.lowercase() }
            ?: return "You don't see '$target' here."

        // zara's ship, requires cargo delivery and kira's crystal
        if (interactable.id == "storm_chaser") {
            if ("cargo_delivered" !in state.gameFlags) {
                return "Zara is not letting anyone near her ship right now."
            }
            val crystal = state.playerInventory.find { it.itemId == "kiras_crystal" }
            if (crystal == null) {
                return "Zara looks you over and frowns. You are not ready yet. That crystal keeper in the caves left something for you, did she not? Go back and get it before we fly."
            }
            val e8 = world.quadrants.find { it.quadrantId == "E8" }
            if (e8 != null) {
                state.currentQuadrant = e8
                state.visitedQuadrants.add(e8.quadrantId)
                state.checkpointQuadrantId = "E8"
            }
            return interactable.interactionText ?: "Zara's ship carries you south over the ocean. Time to dive."
        }

        if (interactable.id == "sailing_boat") {
            state.gameFlags.add("island_sighted")
            state.gameFlags.add("reached_island")

            val i3 = world.quadrants.find { it.quadrantId == "I3" }
            if (i3 != null) {
                state.currentQuadrant = i3
                state.visitedQuadrants.add(i3.quadrantId)
                state.checkpointQuadrantId = "I4"
            }
            return "You untie the knot keeping the boat in place and raise the sail. The boat glides out of the cove as if it has been waiting for this moment. The desert disappears behind you. For three days you sail south, guided by your father's compass. The ocean is vast and indifferent. Then, on the morning of the fourth day, the horizon turns gold.\n\nThe Island of Bliss."
        }

        if (interactable.id == "labyrinth_entrance") {
            state.labyrinthActive = true
            state.labyrinthQuestion = 0
            state.labyrinthAwaitingMove = false
            return "You and your father step through the arch together. The world outside vanishes. \n\nEvery surface is mirrors. Infinite versions of you and your father stretch in every direction. The reflections are perfectly still, until one steps forward. \n\n\"Why do people seek utopia?\"\n\nA) Because they are running from something, not towards something. \nB) Because they believe something better is possible. \nC) Because the world as it is has truly failed them."
        }

        if (interactable.id == "weathered_boat") {
            if ("mirror_labyrinth_complete" !in state.gameFlags) {
                return "The ocean stretches endlessly before you. You don't feel ready to leave yet. There is still something unfinished on this island."
            }
            return "WIN:You untie the weathered boat and push off from shore together. The island recedes behind you.\n\nHalfway across the cove, you reach into your pack and pull out The Utopian Chronicle. Your handwriting filling the pages he started. The story of a journey that began with his obsession and ended with yours.\n\nYour father sees it and goes still.\n\nThe Smiling Ones call from the shore: \"Stay. Stay. Be happy forever.\"\n\nNeither of you turns around.\n\n\"We should let it go,\" you say.\n\nHe looks at the book for a long moment. At his handwriting on the first pages. At yours on the rest.\n\n\"Yes,\" he says quietly. \"We should.\"\n\nYou hold it out over the water together. The book falls, hits the surface, floats for a moment, pages spreading like wings, then sinks. Down into the blue.\n\n\"We don't need a map to paradise anymore,\" your father says. \"Because we're not going there. We're going home.\"\n\nYou sail toward the sunrise.\n\nWhat remains: two people, imperfect, real. Sailing toward an imperfect, beautiful world.\n\nThat's enough. That's everything."
        }


        if (!interactable.canInteract) return "You can't use that."

        // crystal spring restores hydration
        if (interactable.id == "clear_spring") {
            state.hydration = minOf(state.hydration + 5, 20)
        }

        interactable.unlockFlag?.let { flag ->
            state.gameFlags.add(flag)
        }

        return interactable.interactionText ?: "Nothing happens."
    }
}