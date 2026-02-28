package study.coco.project_code

import org.springframework.web.bind.annotation.*
import org.springframework.web.context.annotation.SessionScope

@RestController
@RequestMapping("/api/game")
@SessionScope
class GameController(private val saveManager: SaveManager) {
    private val loader = QuadrantLoader()
    private val engine = loader.engine

    // tracks whether save has been restored in this session
    private var saveRestored = false

    // restores save from SaveManager if not already done this session
    private fun ensureSaveRestored(token: String?): Boolean {
        if (token == null) return false
        if (saveRestored) return true
        saveRestored = true
        val saved = saveManager.load(token) ?: return false
        engine.loadState(saved)
        return true
    }

    @GetMapping("/status")
    fun status(@RequestParam(required = false) token: String?): Map<String, Any> {
        return try {
            // restore save if token provided and a save exists
            val restored = ensureSaveRestored(token)

            mapOf(
                "quadrant" to engine.currentQuadrant.name,
                "description" to engine.currentQuadrant.description.initial,
                "inventory" to engine.getInventory().map { it.itemName },
                "hydration" to engine.getHydration(),
                "saturation" to engine.getSaturation(),
                "discoveredAreas" to engine.getDiscoveredAreas(),
                "currentArea" to engine.getCurrentArea(),
                "currentQuadrantId" to engine.currentQuadrant.quadrantId,
                "currentQuest" to engine.getCurrentQuest(),
                "restored" to restored
            )
        } catch (e: Exception) {
            mapOf("description" to "Failed to load status: ${e.message}")
        }
    }

    @PostMapping("/command")
    fun command(
        @RequestParam input: String,
        @RequestParam(required = false) token: String?
    ): Map<String, Any> {
        return try {
            // restore save on first command if status was never called (e.g. after session expiry)
            ensureSaveRestored(token)
            val questFlags = setOf("spirit_rescued", "crystal_keys_complete", "cargo_delivered", "star_navigation_complete", "guardian_trial_complete", "mirror_labyrinth_complete")
            val flagsBefore = engine.getGameFlags()
            val message = engine.processCommand(input)
            val questComplete = (engine.getGameFlags() - flagsBefore).any { it in questFlags }
            val isCheckpoint = message.startsWith("CHECKPOINT:")
            val isReset = isCheckpoint || message.contains("Game has been reset")

            // auto-save after every command, delete save on quit
            if (token != null) {
                if (message == "QUIT") saveManager.delete(token)
                else saveManager.save(token, engine.saveState())
            }

            mapOf(
                "quadrant" to engine.currentQuadrant.name,
                "description" to message,
                "quadrantDescription" to engine.currentQuadrant.description.initial,
                "reset" to isReset,
                "checkpoint" to isCheckpoint,
                "inventory" to engine.getInventory().map { it.itemName },
                "hydration" to engine.getHydration(),
                "saturation" to engine.getSaturation(),
                "discoveredAreas" to engine.getDiscoveredAreas(),
                "currentArea" to engine.getCurrentArea(),
                "currentQuadrantId" to engine.currentQuadrant.quadrantId,
                "currentQuest" to engine.getCurrentQuest(),
                "questComplete" to questComplete
            )
        } catch (e: Exception) {
            println("ERROR processing command '$input': ${e.javaClass.simpleName}: ${e.message}")
            mapOf(
                "quadrant" to engine.currentQuadrant.name,
                "description" to "Something went wrong: ${e.message}",
                "quadrantDescription" to engine.currentQuadrant.description.initial,
                "reset" to false,
                "checkpoint" to false,
                "inventory" to engine.getInventory().map { it.itemName },
                "hydration" to engine.getHydration(),
                "saturation" to engine.getSaturation(),
                "discoveredAreas" to engine.getDiscoveredAreas(),
                "currentArea" to engine.getCurrentArea(),
                "currentQuadrantId" to engine.currentQuadrant.quadrantId,
                "currentQuest" to engine.getCurrentQuest(),
                "questComplete" to false
            )
        }
    }
}
