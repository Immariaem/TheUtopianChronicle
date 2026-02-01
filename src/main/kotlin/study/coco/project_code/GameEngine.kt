package study.coco.project_code

class GameEngine(val world: World) {
    var currentQuadrant: Quadrant

    init {
        currentQuadrant = world.quadrants.first { it.quadrantId == "B2" }
        println("Game started in: ${currentQuadrant.name}")
    }

    fun processCommand(input: String): String {
        val parts = input.trim().lowercase().split(" ", limit = 2)
        val command = parts[0]
        val argument = if (parts.size > 1) parts[1] else ""

        return when (command) {
            "north", "south", "east", "west" -> move(command)
            "look" -> look()
            "examine" -> examine(argument)
            "use" -> use(argument)
            "talk" -> talk(argument)
            "help" -> help()
            "quit" -> quit()
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
            return connection.blockedMessage ?: "You can't go that way."
        }

        val targetId = connection.quadrantId ?: return "There's nothing in that direction."
        val target = world.quadrants.find { it.quadrantId == targetId }
            ?: return "Error: quadrant $targetId not found."

        currentQuadrant = target
        return currentQuadrant.description.initial
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
- north / south / east / west — move in a direction
- look — describe your surroundings
- examine <name> — examine something closely
- use <name> — interact with something
- talk <name> — talk to someone
- help — show this list
- quit — quit the game"""
    }

    fun quit(): String {
        currentQuadrant = world.quadrants.first { it.quadrantId == "B2" }
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