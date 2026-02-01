package study.coco.project_code

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/game")
class GameController {
    private val loader = QuadrantLoader()
    private val engine = loader.engine

    @GetMapping("/status")
    fun status(): Map<String, Any> {
        return mapOf(
            "quadrant" to engine.currentQuadrant.name,
            "description" to engine.currentQuadrant.description.initial
        )
    }

    @PostMapping("/move")
    fun move(@RequestParam direction: String): Map<String, Any> {
        val message = engine.move(direction)
        return mapOf(
            "quadrant" to engine.currentQuadrant.name,
            "description" to message
        )
    }
}
