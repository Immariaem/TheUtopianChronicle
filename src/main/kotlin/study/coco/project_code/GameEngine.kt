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
            quit()
            return "You died of dehydration! Game has been reset.\n\n${currentQuadrant.description.initial}"
        }

        if (saturation <= 0) {
            quit()
            return "You starved to death! Game has been reset.\n\n${currentQuadrant.description.initial}"
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
        return "QUIT"
    }

    fun talk(target: String): String {
        if (target.isBlank()) return "Talk to whom? Try: talk <name>"

        val npc = currentQuadrant.visibleObjects.npcs
            .find { it.npcName.lowercase() == target.lowercase() && it.isActive }
            ?: return "There's no one called '$target' here."

        val conditionalDialogue = npc.dialogueConditions.firstOrNull { it.flag in gameFlags }
        return conditionalDialogue?.text ?: npc.dialogue
    }

    fun dive(): String {
        if (currentQuadrant.quadrantId != "E8") return "There is nowhere to dive here."
        if ("entered_underwater" in gameFlags) return "You have already dived. The underwater realm is to the east."

        gameFlags.add("entered_underwater")
        return "You clutch Kira's crystal and dive beneath the waves. As the water closes over you, the crystal pulses with warmth and light. You open your mouth, expecting to choke, but instead you breathe. The crystal hums softly as the underwater realm opens up around you. Go east to explore."
    }

    fun stats(): String {
        return "Hydration: $hydration/20\n Saturation: $saturation/20"
    }

    fun getInventory(): List<Item> = playerInventory.toList()
    fun getHydration(): Int = hydration
    fun getSaturation(): Int = saturation
}