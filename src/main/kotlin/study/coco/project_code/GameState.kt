package study.coco.project_code

// holds all mutable game state, shared across handlers
class GameState(world: World) {

    // player location and exploration tracking
    var currentQuadrant: Quadrant = world.quadrants.first { it.quadrantId == "B2" }
    val visitedQuadrants = mutableSetOf("B2")

    // player stats
    var hydration = 20
    var saturation = 20

    // inventory and item tracking
    val playerInventory = mutableListOf<Item>()
    val placedKeys = mutableListOf<Item>()
    val itemSources = mutableMapOf<Item, String>()

    // story progress flags
    val gameFlags = mutableSetOf<String>()

    // checkpoint respawn tracking
    val checkpointQuadrants = setOf("B2", "C4", "A7", "E8", "F5", "I4")
    var checkpointQuadrantId = "B2"

    // guardian trial state
    var currentGuardianQuestion = 0
    var guardianAnswersCorrect = true

    // mirror labyrinth state
    var labyrinthActive = false
    var labyrinthQuestion = 0
    var labyrinthAwaitingMove = false
}