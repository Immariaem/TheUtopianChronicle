package study.coco.project_code

// handles all handlers and routes player commands
class GameEngine(private val world: World) {

    // shared game state
    private val state = GameState(world)

    // handlers
    private val dialogue = DialogueHandler(state, world)
    private val movement = MovementHandler(state, world, dialogue)
    private val inventory = InventoryHandler(state, world)
    private val use = UseHandler(state, world)
    private val exploration = ExplorationHandler(state, world)

    // full item registry built once at startup, before any items are removed from quadrants
    private val allItems = world.quadrants.flatMap { it.visibleObjects.items }

    init {
        println("Game started in: ${state.currentQuadrant.name}")
    }

    // exposes current quadrant for the controller
    val currentQuadrant get() = state.currentQuadrant

    // parses input and delegates to the correct handler
    fun processCommand(input: String): String {
        val parts = input
            .trim()
            .replace(Regex("\\s+"), " ")
            .lowercase()
            .split(" ", limit = 2)
        val command = parts[0]
        val argument = if (parts.size > 1) parts[1] else ""

        return when (command) {
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
            "talk" -> dialogue.talk(argument.removePrefix("to").trim())
            "a" -> if(state.labyrinthActive) dialogue.answerLabyrinth(command) else dialogue.answerGuardian(command)
            "b" -> if(state.labyrinthActive) dialogue.answerLabyrinth(command) else dialogue.answerGuardian(command)
            "c" -> if(state.labyrinthActive) dialogue.answerLabyrinth(command) else dialogue.answerGuardian(command)
            // utility
            "sts", "stats" -> stats()
            "h","help" -> help()
            "q","quit" -> quit()

            else -> "Unknown command: $command. Type 'help' for a list of commands."
        }
    }

    // public getters for the controller
    fun getInventory(): List<Item> = state.playerInventory.toList()
    fun getHydration(): Int = state.hydration
    fun getSaturation(): Int = state.saturation

    // maps current checkpoint to a music area id for the frontend
    fun getCurrentArea(): String {
        val qId = state.currentQuadrant.quadrantId
        val row = qId.firstOrNull() ?: return "forest"
        val col = qId.drop(1).toIntOrNull() ?: return "forest"
        return when {
            (row == 'I' && col in 3..5) || (row == 'J' && col in 3..5) -> "island"
            row == 'H' || row == 'I' || row == 'J' ||
            (row == 'G' && col >= 5) || (row == 'F' && col >= 6) ||
            (row == 'E' && col >= 7) || (row == 'D' && col >= 7) ||
            (row == 'C' && col == 10) -> "underwater"
            (row == 'E' && col <= 3) || (row == 'F' && col <= 5) || (row == 'G' && col <= 4) -> "desert"
            (row == 'A' && col >= 6) || (row == 'B' && col >= 7) || (row == 'C' && col in 8..9) -> "sky"
            qId == "B5" || qId == "B6" || (row == 'C' && col in 4..7) ||
            (row == 'D' && col in 4..6) || (row == 'E' && col in 4..6) -> "caves"
            else -> "forest"
        }
    }

    // exposes current flags so the controller can detect quest completions
    fun getGameFlags(): Set<String> = state.gameFlags.toSet()

    // returns the current quest text based on progression flags
    fun getCurrentQuest(): String {
        val flags = state.gameFlags
        return when {
            "mirror_labyrinth_complete" in flags -> "Bring your father and yourself home"
            "met_smiling_ones" in flags          -> "Complete the mirror labyrinth"
            "reached_island" in flags            -> "Talk to the people of the island"
            "star_navigation_complete" in flags  -> "Sail south toward the island of bliss"
            "met_hermit" in flags                -> "Find the hidden cove"
            "guardian_trial_complete" in flags   -> "Talk to the hermit"
            "found_coral_relic" in flags && "found_oyster_relic" in flags && "found_ruins_relic" in flags -> "Complete the guardian's trial"
            "cargo_delivered" in flags           -> "Find the three ancient relics"
            "reached_sky_city" in flags          -> "Find and bring back Zara's cargo"
            "crystal_keys_complete" in flags     -> "Find a way above to the clouds"
            "viewed_blue_tablet" in flags && "viewed_red_tablet" in flags && "viewed_green_tablet" in flags -> "Solve the crystal puzzle"
            "met_kira" in flags                  -> "Find the three crystal keys"
            "spirit_rescued" in flags            -> "Find the keeper of the caves"
            else                                 -> "Find the trapped spirit and free it"
        }
    }

    // returns list of area ids unlocked so far, used for map reveal
    fun getDiscoveredAreas(): List<String> {
        val areas = mutableListOf("enchantedForest")
        if ("entered_caves" in state.gameFlags) areas.add("mountainRange")
        if ("explored_sky_docks" in state.gameFlags) areas.add("nephelia")
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
- n / north - go north
- e / east - go east
- s / south - go south
- w / west - go west
- dive - dive below the ocean surface
- l / look - describe your surroundings
- x / examine <name> - examine something closely
- t / take <name> - pick up an item
- d / drop <name> - drop an item
- eat <name> - eat an item
- drink <name> - drink an item
- u / use <name> - use an object
- i / inventory - show items you carry
- sts / stats - show your hunger and thirst
- talk <name> - talk to someone
- h / help - show this list
- q / quit - quit the game"""
    }

    // resets all state back to the starting conditions
    fun quit(): String {
        // restore all taken items back to their source quadrants before clearing state
        state.itemSources.forEach { (item, sourceId) ->
            val sourceQuadrant = world.quadrants.find { it.quadrantId == sourceId }
            if (sourceQuadrant != null && item !in sourceQuadrant.visibleObjects.items) {
                sourceQuadrant.visibleObjects.items.add(item)
            }
        }
        state.currentQuadrant = world.quadrants.first { it.quadrantId == "B2" }
        state.visitedQuadrants.clear()
        state.visitedQuadrants.add("B2")
        state.playerInventory.clear()
        state.gameFlags.clear()
        state.hydration = 20
        state.saturation = 20
        state.placedKeys.clear()
        state.itemSources.clear()
        state.checkpointQuadrantId = "B2"
        state.currentGuardianQuestion = 0
        state.guardianAnswersCorrect = true
        state.labyrinthActive = false
        state.labyrinthQuestion = 0
        state.labyrinthAwaitingMove = false
        return "QUIT"
    }

    // captures current state as a serialisable save snapshot
    fun saveState(): SaveData = SaveData(
        currentQuadrantId = state.currentQuadrant.quadrantId,
        visitedQuadrants = state.visitedQuadrants.toSet(),
        gameFlags = state.gameFlags.toSet(),
        hydration = state.hydration,
        saturation = state.saturation,
        checkpointQuadrantId = state.checkpointQuadrantId,
        labyrinthActive = state.labyrinthActive,
        labyrinthQuestion = state.labyrinthQuestion,
        labyrinthAwaitingMove = state.labyrinthAwaitingMove,
        currentGuardianQuestion = state.currentGuardianQuestion,
        guardianAnswersCorrect = state.guardianAnswersCorrect,
        inventoryItemIds = state.playerInventory.map { it.itemId },
        placedKeyIds = state.placedKeys.map { it.itemId },
        itemSourceIds = state.itemSources.map { (item, quadId) -> item.itemId to quadId }.toMap()
    )

    // restores state from a save snapshot
    fun loadState(data: SaveData) {
        // restore location
        state.currentQuadrant = world.quadrants.firstOrNull { it.quadrantId == data.currentQuadrantId }
            ?: world.quadrants.first { it.quadrantId == "B2" }
        state.visitedQuadrants.clear()
        state.visitedQuadrants.addAll(data.visitedQuadrants)

        // restore stats
        state.hydration = data.hydration
        state.saturation = data.saturation
        state.checkpointQuadrantId = data.checkpointQuadrantId

        // restore puzzle state
        state.labyrinthActive = data.labyrinthActive
        state.labyrinthQuestion = data.labyrinthQuestion
        state.labyrinthAwaitingMove = data.labyrinthAwaitingMove
        state.currentGuardianQuestion = data.currentGuardianQuestion
        state.guardianAnswersCorrect = data.guardianAnswersCorrect

        // restore flags
        state.gameFlags.clear()
        state.gameFlags.addAll(data.gameFlags)

        // restore inventory
        state.playerInventory.clear()
        data.inventoryItemIds
            .mapNotNull { id -> allItems.find { it.itemId == id } }
            .forEach { state.playerInventory.add(it) }

        state.placedKeys.clear()
        data.placedKeyIds
            .mapNotNull { id -> allItems.find { it.itemId == id } }
            .forEach { state.placedKeys.add(it) }

        // restore item sources
        state.itemSources.clear()
        data.itemSourceIds.forEach { (itemId, quadId) ->
            allItems.find { it.itemId == itemId }?.let { state.itemSources[it] = quadId }
        }

        // remove taken items from their original quadrants to prevent duplicates
        state.itemSources.keys.forEach { item ->
            val sourceQuadId = state.itemSources[item]
            world.quadrants.find { it.quadrantId == sourceQuadId }
                ?.visibleObjects?.items?.removeIf { it.itemId == item.itemId }
        }
    }
}
