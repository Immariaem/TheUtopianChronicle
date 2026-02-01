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

    @PostMapping("/command")
    fun command(@RequestParam input: String): Map<String, Any> {
        val message = engine.processCommand(input)
        return mapOf(
            "quadrant" to engine.currentQuadrant.name,
            "description" to message
        )
    }
}
