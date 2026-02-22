package study.coco.project_code

// handles all handlers and routes player commands
open class GameEngine(private val world: World) {

    // shared game state
    private val state = GameState(world)

    // handlers
    private val dialogue = DialogueHandler(state, world)
    private val movement = MovementHandler(state, world, dialogue)
    private val inventory = InventoryHandler(state, world)
    private val use = UseHandler(state, world)
    private val exploration = ExplorationHandler(state, world)

    init {
        println("Game started in: ${state.currentQuadrant.name}")
    }

    // exposes current quadrant for the controller
    val currentQuadrant get() = state.currentQuadrant

    // parses input and delegates to the correct handler
    fun processCommand(input: String): String {
        return try {
            val parts = input
                .trim()
                .replace(Regex("\\s+"), " ")
                .lowercase()
                .split(" ", limit = 2)
            val command = parts[0]
            val argument = if (parts.size > 1) parts[1] else ""

            when (command) {
                // movement
                "n","north" -> movement.move("north")
                "s","south" -> movement.move("south")
                "e","east" -> movement.move("east")
                "w","west" -> movement.move("west")
                "go" -> movement.move(argument)
                "dive" -> movement.dive()
                // inventory
                "t","take" -> inventory.take(argument)
                "d","drop" -> inventory.drop(argument)
                "i","inventory" -> inventory.inventory()
                // exploration
                "l","look" -> exploration.look()
                "x","examine" -> exploration.examine(argument)
                // item use
                "u","use", "eat", "drink" -> use.use(argument)
                // dialogue and puzzles
                "talk" -> dialogue.talk(argument)
                "a" -> if(state.labyrinthActive) dialogue.answerLabyrinth(command) else dialogue.answerGuardian(command)
                "b" -> if(state.labyrinthActive) dialogue.answerLabyrinth(command) else dialogue.answerGuardian(command)
                "c" -> if(state.labyrinthActive) dialogue.answerLabyrinth(command) else dialogue.answerGuardian(command)
                // utility
                "sts", "stats" -> stats()
                "h","help" -> help()
                "q","quit" -> quit()
                // dev tools
                "warp" -> warp(argument)
                "flags" -> setFlags(argument)

                else -> "Unknown command: $command. Type 'help' for a list of commands."
            }
        } catch (e: Exception) {
            "Something went wrong processing that command. Try again."
        }
    }

    // public getters for the controller
    fun getInventory(): List<Item> = state.playerInventory.toList()
    fun getHydration(): Int = state.hydration
    fun getSaturation(): Int = state.saturation

    // returns list of area ids unlocked so far, used for map reveal
    fun getDiscoveredAreas(): List<String> {
        val areas = mutableListOf("enchantedForest")
        if ("met_kira" in state.gameFlags) areas.add("mountainRange")
        if ("met_zara" in state.gameFlags) areas.add("nephelia")
        if ("reached_desert" in state.gameFlags) areas.add("desertOfTruth")
        if ("entered_underwater" in state.gameFlags) areas.add("underwaterRealm")
        if ("reached_island" in state.gameFlags) areas.add("islandOfBliss")
        return areas
    }

    // formats current hydration and saturation for display
    fun stats(): String {
        return "Hydration: ${state.hydration}/20\n Saturation: ${state.saturation}/20"
    }

    // lists all available commands
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

    // resets all state back to the starting conditions
    fun quit(): String {
        state.currentQuadrant = world.quadrants.first { it.quadrantId == "B2" }
        state.visitedQuadrants.clear()
        state.visitedQuadrants.add("B2")
        state.playerInventory.clear()
        state.gameFlags.clear()
        state.hydration = 20
        state.saturation = 20
        state.placedKeys.clear()
        state.itemSources.clear()
        state.labyrinthActive = false
        state.labyrinthQuestion = 0
        state.labyrinthAwaitingMove = false
        return "QUIT"
    }

    // dev tool: teleports to any quadrant by id
    fun warp(target: String): String {
        if (target.isBlank()) return "Warp where? Try: warp B5"
        val quadrant = world.quadrants.find { it.quadrantId == target.uppercase() }
            ?: return "Quadrant '$target' not found."
        state.currentQuadrant = quadrant
        state.visitedQuadrants.add(quadrant.quadrantId)
        return "Warped to ${quadrant.name}(${quadrant.quadrantId})\n\n${quadrant.description.initial}"
    }

    // dev tool: sets one or more flags manually
    fun setFlags(flagList: String): String {
        if (flagList.isBlank()) return "Current flags: ${state.gameFlags}"
        flagList.split(",").forEach { state.gameFlags.add(it.trim()) }
        return "Flags set: ${state.gameFlags}"
    }
}
