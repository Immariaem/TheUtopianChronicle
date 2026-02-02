package study.coco.project_code

class GameEngine(val world: World) {
    var currentQuadrant: Quadrant
        private set
    val visitedQuadrants = mutableSetOf<String>()
    val playerInventory = mutableListOf<String>()

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
            "l","look" -> look()
            "x","examine" -> examine(argument)
            "u","use" -> use(argument)
            "talk" -> talk(argument)
            "h","help" -> help()
            "q","quit" -> quit()
            else -> "Unknown command: $command. Type 'help' for a list of commands."
        }
    }

    fun move(direction: String): String {
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

        currentQuadrant = target
        val message = if (currentQuadrant.quadrantId in visitedQuadrants) {
            currentQuadrant.description.returnText
        } else {
            visitedQuadrants.add(currentQuadrant.quadrantId)
            currentQuadrant.description.initial
        }
        return message
    }

    fun take(target: String): String {
        if (target.isBlank()) return "Take what? Try: t <name>"

        val item = currentQuadrant.visibleObjects.items
            .find { it.itemId.lowercase() == target.lowercase() }
            ?: return "There's no '$target' here to take."

        if (!item.isCollectable) return "You can't take that."

        playerInventory.add(item.itemId)
        return "You pick up ${item.itemId}."
    }

    fun drop(target: String): String {
        if (target.isBlank()) return "Drop what? Try: d <name>"

        val item = playerInventory.find { it.lowercase() == target.lowercase() }
            ?: return "You don't have '$target' in your inventory."

        playerInventory.remove(item)
        return "You drop $item."
    }

    fun inventory(): String {
        if (playerInventory.isEmpty()) return "Your inventory is empty."

        val result = StringBuilder("You are carrying:")
        playerInventory.forEach { result.appendLine("\n- $it") }
        return result.toString()
    }

    fun look(): String {
        val result = StringBuilder()
        result.appendLine(currentQuadrant.description.atmospheric)

        val npcs = currentQuadrant.visibleObjects.npcs.filter { it.isActive }
        if (npcs.isNotEmpty()) {
            result.appendLine("\nYou see:")
            npcs.forEach { result.appendLine("- ${it.npcId}") }
        }

        val items = currentQuadrant.visibleObjects.items
        if (items.isNotEmpty()) {
            result.appendLine("\nItems:")
            items.forEach { result.appendLine("- ${it.itemId}") }
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
        if (target.isBlank()) return "Interact with what? Try: interact <name>"

        val interactable = currentQuadrant.visibleObjects.interactables
            .find { it.name.lowercase() == target.lowercase() }
            ?: return "You don't see '$target' here."

        if (!interactable.canInteract) return "You can't interact with that."

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
- u / use <name> — use an object
- i / inventory — show items you carry
- talk <name> — talk to someone
- h / help — show this list
- q / quit — quit the game"""
    }

    fun quit(): String {
        currentQuadrant = world.quadrants.first { it.quadrantId == "B2" }
        visitedQuadrants.clear()
        visitedQuadrants.add("B2")
        playerInventory.clear()
        return "QUIT"
    }

    fun talk(target: String): String {
        if (target.isBlank()) return "Talk to whom? Try: talk <name>"

        val npc = currentQuadrant.visibleObjects.npcs
            .find { it.npcId.lowercase() == target.lowercase() && it.isActive }
            ?: return "There's no one called '$target' here."

        return npc.dialogue
    }
}