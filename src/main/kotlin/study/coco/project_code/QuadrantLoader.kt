package study.coco.project_code

class QuadrantLoader {
    val world = World()
    val engine: GameEngine

    init {
        try {
            world.loadQuadrants("quadrants")
            engine = GameEngine(world)
            println("Loaded ${world.quadrants.size} quadrants")
        } catch (e: Exception) {
            println("Failed to initialize game: ${e.message}")
            throw e
        }
    }
}
