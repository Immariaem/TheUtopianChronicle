package study.coco.project_code

import kotlinx.serialization.Serializable

@Serializable
// snapshot of game state using only ids and primitives
data class SaveData(
    val currentQuadrantId: String,
    val visitedQuadrants: Set<String>,
    val gameFlags: Set<String>,
    val hydration: Int,
    val saturation: Int,
    val checkpointQuadrantId: String,
    val labyrinthActive: Boolean,
    val labyrinthQuestion: Int,
    val labyrinthAwaitingMove: Boolean,
    val currentGuardianQuestion: Int,
    val guardianAnswersCorrect: Boolean,
    val inventoryItemIds: List<String>,
    val placedKeyIds: List<String>,
    val itemSourceIds: Map<String, String>
)
