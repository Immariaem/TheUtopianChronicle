package study.coco.project_code

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonElement

@Serializable
data class Coordinates(val row: String, val column: Int, val x: Int, val y: Int)

@Serializable
data class Description(
    val initial: String,
    @SerialName("return") val returnText: String,
    val atmospheric: String
)

@Serializable
data class Connection(
    val quadrantId: String? = null,
    val blocked: Boolean,
    val blockedMessage: String? = null,
    val travelMethod: String? = null
)

@Serializable
data class Connections(
    val north: Connection,
    val south: Connection,
    val east: Connection,
    val west: Connection
)

@Serializable
data class Npc(
    val npcId: String,
    val spawnCondition: String? = null,
    val dialogue: String,
    val isActive: Boolean
)

@Serializable
data class Item(
    val itemId: String,
    val spawnCondition: String? = null,
    val isCollectable: Boolean
)

@Serializable
data class Interactable(
    val id: String,
    val name: String,
    val examineText: String,
    val canInteract: Boolean,
    val interactionText: String? = null
)

@Serializable
data class VisibleObjects(
    val npcs: List<Npc> = emptyList(),
    val items: List<Item> = emptyList(),
    val interactables: List<Interactable> = emptyList()
)

@Serializable
data class Metadata(val isCriticalPath: Boolean)

@Serializable
data class Quadrant(
    val quadrantId: String,
    val coordinates: Coordinates,
    val name: String,
    val region: String,
    val function: String,
    val description: Description,
    val connections: Connections,
    val visibleObjects: VisibleObjects,
    val requiredFlags: List<String> = emptyList(),
    val unlockFlags: List<String> = emptyList(),
    val storyCheckpoint: String? = null,
    val puzzleData: JsonElement? = null,
    val questData: JsonElement? = null,
    val specialMechanics: JsonElement? = null,
    val metadata: Metadata
)
