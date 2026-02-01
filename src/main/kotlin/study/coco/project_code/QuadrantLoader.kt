package study.coco.project_code

class QuadrantLoader {
    val world = World()
    val engine: GameEngine

    init {
        world.loadQuadrants("quadrants")
        engine = GameEngine(world)
        println("Loaded ${world.quadrants.size} quadrants")
    }
}

