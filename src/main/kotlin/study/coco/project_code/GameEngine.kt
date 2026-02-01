package study.coco.project_code

class GameEngine(val world: World) {
    var currentQuadrant: Quadrant

    init {
        currentQuadrant = world.quadrants.first { it.quadrantId == "B2" }
        println("Game started in: ${currentQuadrant.name}")
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
}