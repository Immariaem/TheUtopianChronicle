package study.coco.project_code

open class GameEngine(private val world: World) {
    var currentQuadrant: Quadrant
        private set
    private val visitedQuadrants = mutableSetOf<String>()
    private val playerInventory = mutableListOf<Item>()

    private var hydration = 20
    private var saturation = 20
    private val gameFlags = mutableSetOf<String>()
    private val placedKeys = mutableListOf<Item>()
    private var currentGuardianQuestion = 0
    private var guardianAnswersCorrect = true
    private var labyrinthActive = false
    private var labyrinthQuestion = 0
    private var labyrinthAwaitingMove = false
    private val checkpointQuadrants = setOf("B2", "C4", "A7", "E8", "F5","I4")
    private var checkpointQuadrantId = "B2"
    private val itemSources = mutableMapOf<Item, String>()

    init {
        currentQuadrant = world.quadrants.first { it.quadrantId == "B2" }
        visitedQuadrants.add("B2")
        println("Game started in: ${currentQuadrant.name}")
    }

    fun processCommand(input: String): String {
        val parts = input
            .trim()
            .replace(Regex("\\s+"), " ")
            .lowercase()
            .split(" ", limit = 2)
        val command = parts[0]
        val argument = if (parts.size > 1) parts[1] else ""

        return when (command) {
            "n","north" -> move("north")
            "s","south" -> move("south")
            "e","east" -> move("east")
            "w","west" -> move("west")
            "go" -> move(argument)
            "t","take" -> take(argument)
            "d","drop" -> drop(argument)
            "i","inventory" -> inventory()
            "sts", "stats" -> stats()
            "l","look" -> look()
            "x","examine" -> examine(argument)
            "u","use", "eat", "drink" -> use(argument)
            "talk" -> talk(argument)
            "dive" -> dive()
            "a" -> if(labyrinthActive) answerLabyrinth(command) else answerGuardian(command)
            "b" -> if(labyrinthActive) answerLabyrinth(command) else answerGuardian(command)
            "c" -> if(labyrinthActive) answerLabyrinth(command) else answerGuardian(command)
            "h","help" -> help()
            "q","quit" -> quit()

            "warp" -> warp(argument)
            "flags" -> setFlags(argument)

            else -> "Unknown command: $command. Type 'help' for a list of commands."
        }
    }

    fun warp(target: String): String {
        if (target.isBlank()) return "Warp where? Try: warp B5"
        val quadrant = world.quadrants.find { it.quadrantId == target.uppercase()
        }
            ?: return "Quadrant '$target' not found."
        currentQuadrant = quadrant
        visitedQuadrants.add(quadrant.quadrantId)
        return "Warped to ${quadrant.name}(${quadrant.quadrantId})\n\n${quadrant.description.initial}"
    }

    fun setFlags(flagList: String): String {
            if (flagList.isBlank()) return "Current flags: $gameFlags"
            flagList.split(",").forEach { gameFlags.add(it.trim()) }
            return "Flags set: $gameFlags"
        }

    fun move(direction: String): String {

        if (hydration <= 0) {
            return respawn("You died of dehydration! You have respawned at your last checkpoint.")
        }

        if (saturation <= 0) {
            return respawn("You died of starvation! You have respawned at your last checkpoint.")
        }

        if (labyrinthActive && currentQuadrant.quadrantId == "J4") {
            if (!labyrinthAwaitingMove) {
                return "Your reflection stands before you, waiting. Answer before you move on."
            }
            labyrinthAwaitingMove = false
            return nextLabyrinthQuestion()
        }

        if (currentQuadrant.quadrantId == "J4" && direction == "west" && "mirror_labyrinth_complete" !in gameFlags) {
            return "The western wall is solid mirrors. There is no way through. Not yet."
        }

        val connection = when (direction.lowercase()) {
            "north" -> currentQuadrant.connections.north
            "south" -> currentQuadrant.connections.south
            "east" -> currentQuadrant.connections.east
            "west" -> currentQuadrant.connections.west
            else -> return "Unknown direction: $direction"
        }

        if (connection.blocked) {
            return connection.blockedMessage ?: "You cannot go that way."
        }

        val targetId = connection.quadrantId ?: return "There's nothing in that direction."
        val target = world.quadrants.find { it.quadrantId == targetId }
            ?: return "Error: quadrant $targetId not found."

        if (target.requiredFlags.isNotEmpty()) {
            val missingFlags = target.requiredFlags.filter { it !in gameFlags }
            if (missingFlags.isNotEmpty()) {
                return "You can't go there yet. Something is blocking your path."
            }
        }

        hydration -= 2
        saturation -= 1
        currentQuadrant = target

        var message = if (currentQuadrant.quadrantId in visitedQuadrants) {
            currentQuadrant.description.returnText
        } else {

            if (currentQuadrant.unlockFlags.isNotEmpty()) {
                currentQuadrant.unlockFlags.forEach { flag ->
                    gameFlags.add(flag)
                }
            }
            visitedQuadrants.add(currentQuadrant.quadrantId)

            if (currentQuadrant.quadrantId in checkpointQuadrants) {
                checkpointQuadrantId = currentQuadrant.quadrantId
            }

            currentQuadrant.description.initial
        }

        if (hydration <= 8) {
            message += "\n\nWarning: You're getting thirsty! Find water soon."

        }

        if (saturation <= 5) {
            message += "\n\nWarning: You're getting hungry! Find food soon."

        }

        return message
    }

    fun take(target: String): String {
        if (target.isBlank()) return "Take what? Try: t <name>"

        val item = currentQuadrant.visibleObjects.items
            .find { it.itemName.lowercase() == target.lowercase() }
            ?: return "There's no '$target' here to take."

        if (!item.isCollectable) return "You can't take that."

        itemSources[item] = currentQuadrant.quadrantId
        playerInventory.add(item)
        currentQuadrant.visibleObjects.items.remove(item)
        return "You pick up ${item.itemName}."
    }

    fun drop(target: String): String {
        if (target.isBlank()) return "Drop what? Try: d <name>"

        val item = playerInventory.find { it.itemName.lowercase() == target.lowercase() }
            ?: return "You don't have '$target' in your inventory."

        if (item.itemId == "zaras_cargo") {
            if (currentQuadrant.quadrantId == "B7") {
                playerInventory.remove(item)
                gameFlags.add("cargo_delivered")
                return "You set down the heavy cargo on Zara's dock. She walks over and inspects it with a nod of approval."
            } else {
                return "You should bring this back to Zara's dock."
            }
        }

        if (item.itemId in listOf("coral_relic", "pearl_relic", "stone_relic")) {
            if (currentQuadrant.quadrantId == "H8") {
                playerInventory.remove(item)
                gameFlags.add("dropped_${item.itemId}")

                if ("dropped_coral_relic" in gameFlags && "dropped_pearl_relic" in gameFlags && "dropped_stone_relic" in gameFlags) {
                    gameFlags.add("relics_offered")
                    return "You place ${item.itemName} on the altar. The Pearl Guardian nods. All three relics have been offered. Speak to the Guardian when you are ready."
                }
                return "You place the ${item.itemName} on the altar. The Guardian watches in silence."
            } else {
                return "This does not belong here."
            }
        }

        currentQuadrant.visibleObjects.items.add(item)
        itemSources.remove(item)
        playerInventory.remove(item)
        return "You drop ${item.itemName}."
    }

    fun inventory(): String {
        if (playerInventory.isEmpty()) return "Your inventory is empty."

        val result = StringBuilder("You are carrying:")
        playerInventory.forEach { result.appendLine("\n- ${it.itemName}") }
        return result.toString()
    }

    fun look(): String {
        val result = StringBuilder()
        result.appendLine(currentQuadrant.description.atmospheric)

        val npcs = currentQuadrant.visibleObjects.npcs.filter { it.isActive }
        if (npcs.isNotEmpty()) {
            result.appendLine("\nYou see:")
            npcs.forEach { result.appendLine("- ${it.npcName}") }
        }

        val items = currentQuadrant.visibleObjects.items
        if (items.isNotEmpty()) {
            result.appendLine("\nItems:")
            items.forEach { result.appendLine("- ${it.itemName}") }
        }

        val interactables = currentQuadrant.visibleObjects.interactables
        if (interactables.isNotEmpty()) {
            result.appendLine("\nYou notice:")
            interactables.forEach { result.appendLine("- ${it.name}") }
        }

        return result.toString()
    }

    fun examine(target: String): String {
        if (target.isBlank()) return "Examine what? Try: examine <name>"

        val interactable = currentQuadrant.visibleObjects.interactables
            .find { it.name.lowercase() == target.lowercase() }

        if (interactable != null) return interactable.examineText

        val item = currentQuadrant.visibleObjects.items
            .find { it.itemId.lowercase() == target.lowercase() }

        if (item != null) return "You see: ${item.itemId}"

        return "You don't see '$target' here."
    }

    fun use(target: String): String {
        if (target.isBlank()) return "Use what? Try: use <name>"

        val inventoryItem = playerInventory.find { it.itemName.lowercase() == target.lowercase() }
        val correctOrder = listOf("blue_crystal_key", "red_crystal_key", "green_crystal_key")
        if (inventoryItem != null) {

            if (currentQuadrant.quadrantId == "D5" && inventoryItem.itemName.lowercase().contains("crystal key")) {
                placedKeys.add(inventoryItem)
                playerInventory.remove(inventoryItem)

                if (placedKeys.size == 3) {

                    //Crystal quest
                    if (placedKeys.map { it.itemId } == correctOrder) {
                        gameFlags.add("crystal_keys_complete")
                        return  "The crystals begin to sing... \n All three keys glow in unison. A light and comfortable ringing echos trough the caves. Kira will be more than happy to help any seeker find their way to the clouds now."
                    } else {
                        placedKeys.forEach { key ->
                            currentQuadrant.visibleObjects.items.add(key)
                        }
                        placedKeys.clear()
                        return "The console rejects the sequence. The keys fall to the ground."
                    }
                }
                return "You place the ${inventoryItem.itemName} on the console."
            }

            //Desert compass quest
            if (currentQuadrant.quadrantId == "F2" && inventoryItem.itemId == "fathers_compass") {
                gameFlags.add("found_star_point")
                return "EPHEMERAL:The compass needle spins wildly, then locks in place. A vision burns into your mind: \n\n\"You see three stars lighting up in the night sky. The first sinks below the horizon. The second and third rise side by side where the sun rises each day follow them twice. The cove awaits the seeker worthy of understanding.\"\n\nThe compass goes still."
            }

            if (inventoryItem.itemType == "consumable") {
                val itemId = inventoryItem.itemId.lowercase()

                if (itemId.contains("water") || itemId == "coconut" || itemId ==
                    "cactus_fruit") {
                    hydration = minOf(hydration + 5,20)
                }

                if (!itemId.contains("water") || itemId == "coconut" || itemId ==
                    "cactus_fruit") {
                    saturation = minOf(saturation + 5, 20)
                }

                playerInventory.remove(inventoryItem)
            }
            return inventoryItem.itemMessage ?: "Nothing happens."
        }

        val interactable = currentQuadrant.visibleObjects.interactables
            .find { it.name.lowercase() == target.lowercase() }
            ?: return "You don't see '$target' here."

        if (interactable.id == "storm_chaser") {
            if ("cargo_delivered" !in gameFlags) {
                return "Zara is not letting anyone near her ship right now."
            }
            val crystal = playerInventory.find { it.itemId == "kiras_crystal" }
            if (crystal == null) {
                return "Zara looks you over and frowns. You are not ready yet. That crystal keeper in the caves left something for you, did she not? Go back and get it before we fly."
            }
            val e8 = world.quadrants.find { it.quadrantId == "E8" }
            if (e8 != null) {
                currentQuadrant = e8
                visitedQuadrants.add(e8.quadrantId)
            }
        }

        if (interactable.id == "sailing_boat") {
            gameFlags.add("island_sighted")
            gameFlags.add("reached_island")

            val i3 = world.quadrants.find { it.quadrantId == "I3" }
            if (i3 != null) {
                currentQuadrant = i3
                visitedQuadrants.add(i3.quadrantId)
            }
            return "You untie the knot keeping the boat in place and raise the sail. The boat glides out of the cove as if it has been waiting for this moment. The desert disappears behind you. For three days you sail south, guided by your father's compass. The ocean is vast and indifferent. Then, on the morning of the fourth day, the horizon turns gold.\n\nThe Island of Bliss."
        }

        if (interactable.id == "labyrinth_entrance") {
            labyrinthActive = true
            labyrinthQuestion = 0
            labyrinthAwaitingMove = false
            return "You and your father step through the arch together. The world outside vanishes. \n\nEvery surface is mirrors. Infinite versions of you and your father stretch in every direction. The reflections are perfectly still, until one steps forward. \n\n\"Why do people seek utopia?\"\n\nA) Because they are running from something, not towards something. \nB) Because they believe something better is possible. \nC) Because the world as it is has truly failed them."
        }

        if (interactable.id == "weathered_boat") {
            if ("mirror_labyrinth_complete" !in gameFlags) {
                return "The ocean stretches endlessly before you. You don't feel ready to leave yet. There is still something unfinished on this island."
            }
            return "WIN:You untie the weathered boat and push off from shore together. The island recedes behind you.\n\nHalfway across the cove, you reach into your pck and pull out The Utopian Chronicle. Your handwriting filling the pages he started.The story of a journey that began with his obsession and ended with yours.\n\nYour father sees it and goes still.\n\nThe Smiling Ones call from the shore: \"Stay. Stay. Be happy forever.\"\n\nNeither of you turns around.\n\n\"We should let it go,\" you say.\n\nHe looks at the book for along moment. At his handwriting on the first pages. At yours on the rest.\n\n\"Yes,\" he says quietly. \"We should.\"\n\nYou hold it out over the water together. The book falls, hits the surface, floats for a moment, pages spreading like wings, then sinks. Down into the blue.\n\n\"We don't need a map to paradise anymore,\" your father says. \"Because we're not going there. We're going home.\"\n\nYou sail toward the sunrise.\n\nWhat remains: two people, imperfect, real. Sailing toward an imperfect, beautiful world.\n\nThat's enough. That's everything."
        }


        if (!interactable.canInteract) return "You can't use that."

        interactable.unlockFlag?.let { flag ->
            gameFlags.add(flag)
        }

        return interactable.interactionText ?: "Nothing happens."
    }

    fun help(): String {
        return """Available commands:
- n / north — go north 
- e / east - go east 
- s / south - go south
- w / west - go west
- l / look — describe your surroundings
- x / examine <name> — examine something closely
- t / take <name> — pick up an item
- d / drop <name> — drop an item
- eat <name> - eat an item
- drink <name> - drink an item
- u / use <name> — use an object
- i / inventory — show items you carry
- sts / stats - show your hunger and thirst
- talk <name> — talk to someone
- h / help — show this list
- q / quit — quit the game"""
    }

    fun quit(): String {
        currentQuadrant = world.quadrants.first { it.quadrantId == "B2" }
        visitedQuadrants.clear()
        visitedQuadrants.add("B2")
        playerInventory.clear()
        gameFlags.clear()
        hydration = 20
        saturation = 20
        placedKeys.clear()
        itemSources.clear()
        labyrinthActive = false
        labyrinthQuestion = 0
        labyrinthAwaitingMove = false
        return "QUIT"
    }

    fun talk(target: String): String {
        if (target.isBlank()) return "Talk to whom? Try: talk <name>"

        val npc = currentQuadrant.visibleObjects.npcs
            .find { it.npcName.lowercase() == target.lowercase() && it.isActive }
            ?: return "There's no one called '$target' here."

        if (npc.npcId == "pearl_guardian" && "relics_offered" in gameFlags &&
            "guardian_trial_complete" !in gameFlags) {
            currentGuardianQuestion = 0
            guardianAnswersCorrect = true
            return "\"The relics are accepted. Now I will ask you three questions.\"\n\n\"Many have come seeking this island. What drives you to follow in their footsteps?\"\n\nA) I am following someone who came this way. I need to know what happened to them.\n\nB) I seek a perfect life, free from the pain of the world above.\n\nC) I will succeed where others have failed. That is reason enough."
        }

        val conditionalDialogue = npc.dialogueConditions.firstOrNull { it.flag in gameFlags }
        return conditionalDialogue?.text ?: npc.dialogue
    }

    fun dive(): String {
        if (currentQuadrant.quadrantId != "E8") return "There is nowhere to dive here."
        if ("entered_underwater" in gameFlags) return "You have already dived. The underwater realm is to the east."

        gameFlags.add("entered_underwater")
        return "You clutch Kira's crystal and dive beneath the waves. As the water closes over you, the crystal pulses with warmth and light. You open your mouth, expecting to choke, but instead you breathe. The crystal hums softly as the underwater realm opens up around you. Go east to explore."
    }

    fun answerGuardian(choice: String): String {
        if (currentQuadrant.quadrantId != "H8" || "relics_offered" !in gameFlags)
        {
            return "Unknown command: $choice. Type 'help' for a list of commands."
        }

        val correctAnswers = listOf("a", "b", "b")

        if (choice != correctAnswers[currentGuardianQuestion]) {
            guardianAnswersCorrect = false
        }
        currentGuardianQuestion++

        if (currentGuardianQuestion == 1) {
            return "\"Some call the island a utopia. What do you believe a world without suffering would cost?\"\n\nA) Nothing. Peace is not something that must be paid for.\n\nB) Everything that makes life worth living.\n\nC) Only the freedom of those too weak to endure it."
        }

        if (currentGuardianQuestion == 2) {
            return "\"If you reached the island and found nothing you hoped for, what would you do?\"\n\nA) I would stay. Any destination is better than the journey.\n\nB) I would leave and carry the truth back with me.\n\nC) I would make it into what I needed it to be."
        }

        if (guardianAnswersCorrect) {
            gameFlags.add("guardian_trial_complete")
            return "The Pearl Guardian nods slowly. \"You have answered well, seeker. Surface to the west and head for the desert. Cross it heading west. The Island of Bliss lies beyond the far shore.\""
        } else {
            currentGuardianQuestion = 0
            guardianAnswersCorrect = true
            return "The Pearl Guardian shakes his head. \"Your answers do not ring true. Speak to me again when you are ready.\""
        }
    }

    fun answerLabyrinth(choice: String): String {
        if (!labyrinthActive) return "Unknown command: $choice. Type 'help' for a list of commands."

        val correctAnswers = listOf("a", "b", "a", "b", "b", "c")
        val fatherTexts = listOf(
            "Your father's reflection speaks quietly beside yours: \"Because I was afraid that ordinary was all I would ever be. And I couldn't face that.\"",
            "Your father's reflection speaks: \"Three years. Your childhood. The chance to watch you grow. I didn't know I was paying until it was already gone.\"",
            "Your father's reflection speaks: \"I thought I was. Until I realised I hadn't felt anything in months. Not worry. Not longing. Not love. Nothing.\"",
            "Your father's reflection speaks: \"I watched it happen slowly. First the worry. Then the longing. Then the memories. Then the names of the people they loved.\"",
            "Your father's reflection speaks: \"Ask me again when I've had a chance to be imperfect for a while.\""
        )

        if (choice != correctAnswers[labyrinthQuestion]) {
            labyrinthActive = false
            labyrinthQuestion = 0
            labyrinthAwaitingMove = false
            return "The mirrors cloud over. The corridor behind you dissolves. You find yourself back at the entrance, your father beside you.\n\n\"Again,\" he says quietly. \"We face it again.\"\n\nUse the archway to enter once more."
        }

        if (labyrinthQuestion == 5) {
            labyrinthActive = false
            labyrinthQuestion = 0
            labyrinthAwaitingMove = false
            gameFlags.add("mirror_labyrinth_complete")

            val j3 = world.quadrants.find { it.quadrantId == "J3" }
            if (j3 != null) {
                currentQuadrant = j3
                visitedQuadrants.add(j3.quadrantId)
            }
            return "The mirrors shatter.\n\nNot with violence, with release. Every reflection, every version of you and your father, dissolves into light.The labyrinth opens. The western wall simply isn't there anymore.\n\nYou and your father walk out together.\n\n${j3?.description?.initial ?: ""}"
        }

        val fatherText = fatherTexts[labyrinthQuestion]
        labyrinthQuestion++
        labyrinthAwaitingMove = true
        return "$fatherText\n\nThe path ahead clears. Move deeper."
    }

    private fun nextLabyrinthQuestion(): String {
        val corridorTexts = listOf(
            "The mirror clears. Your reflections multiply as you walk deeper. Another intersection, another reflection waiting.",
            "The corridor shifts around you. Behind you, the entrance has disappeared. There is only forward.",
            "The air grows heavier. Your father walks beside you in silence. The reflections track your every step.",
            "The labyrinth breathes. The reflections watch you both with something that might be recognition.",
            "One final corridor. The western wall shimmers ahead. The last reflection steps forward."
        )

        val questionTexts = listOf(
            "\"What does a perfect world cost?\"\n\nA) The suffering of those who cannot enter it.\nB) Everything that makes us human.\nC) Only what we are willing to give.",
            "\"Could you be happy here?\"\n\nA) No. Happiness without struggle is not happiness.\nB) Yes. The peace is real. The beauty is real.\nC) Perhaps. If I forgot enough.",
            "\"What did the Smiling Ones lose?\"\n\nA) Nothing. They found what they were looking for.\nB) Everything that made them themselves.\nC) Only what caused them pain.",
            "\"Is the search for perfection worth pursuing?\"\n\nA) Yes. The pursuit itself makes us better.\nB) No. It blinds us to what we already have.\nC) Only if we accept we may never arrive.",
            "\"What is utopia?\"\n\nA) The world we build when we refuse to accept suffering.\nB) A destination that demands more than we can pay.\nC) A beautiful lie we tell ourselves to avoid living."
        )

        val idx = labyrinthQuestion - 1
        return "${corridorTexts[idx]}\n\n${questionTexts[idx]}"
    }

    private fun respawn (cause: String):String {

        playerInventory.forEach { item ->
            val sourceId = itemSources[item]
            val sourceQuadrant = world.quadrants.find { it.quadrantId == sourceId }
            sourceQuadrant?.visibleObjects?.items?.add(item)
        }
        playerInventory.clear()

        placedKeys.forEach { key ->
            val sourceId = itemSources[key]
            val sourceQuadrant = world.quadrants.find { it.quadrantId == sourceId }
            sourceQuadrant?.visibleObjects?.items?.add(key)
        }
        placedKeys.clear()

        itemSources.clear()

        val checkpoint = world.quadrants.first { it.quadrantId == checkpointQuadrantId }
        currentQuadrant = checkpoint
        hydration = 20
        saturation = 20
        return "CHECKPOINT:$cause"
    }

    fun stats(): String {
        return "Hydration: $hydration/20\n Saturation: $saturation/20"
    }

    fun getInventory(): List<Item> = playerInventory.toList()
    fun getHydration(): Int = hydration
    fun getSaturation(): Int = saturation
}