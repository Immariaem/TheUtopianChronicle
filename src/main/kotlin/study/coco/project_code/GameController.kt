package study.coco.project_code

import org.springframework.web.bind.annotation.*
import org.springframework.web.context.annotation.SessionScope

@RestController
@RequestMapping("/api/game")
@SessionScope
class GameController {
    private val loader = QuadrantLoader()
    private val engine = loader.engine

    @GetMapping("/status")
    fun status(): Map<String, Any> {
        return try {  mapOf (
            "quadrant" to engine.currentQuadrant.name,
            "description" to engine.currentQuadrant.description.initial,
            "inventory" to engine.getInventory().map { it.itemName },
            "hydration" to engine.getHydration(),
            "saturation" to engine.getSaturation(),
            "discoveredAreas" to engine.getDiscoveredAreas()
        )
        }catch (e: Exception) {
            mapOf("description" to "Failed to load status:${e.message}")
        }
    }

    @PostMapping("/command")
    fun command(@RequestParam input: String): Map<String, Any> {
        return try {
            val message = engine.processCommand(input)
            val isCheckpoint = message.startsWith("CHECKPOINT:")
            val isReset = isCheckpoint || message.contains("Game has been reset")
            mapOf(
                "quadrant" to engine.currentQuadrant.name,
                "description" to message,
                "quadrantDescription" to engine.currentQuadrant.description.initial,
                "reset" to isReset,
                "checkpoint" to isCheckpoint,
                "inventory" to engine.getInventory().map { it.itemName },
                "hydration" to engine.getHydration(),
                "saturation" to engine.getSaturation(),
                "discoveredAreas" to engine.getDiscoveredAreas()
            )
        } catch (e: Exception) {
            mapOf("description" to "An error occurred: ${e.message}")
        }
    }
}
