package study.coco.project_code

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

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
    val blockedMessage: String? = null
)

@Serializable
data class Connections(
    val north: Connection,
    val south: Connection,
    val east: Connection,
    val west: Connection
)

@Serializable
data class DialogueCondition(
    val flag: String,
    val text: String
)

@Serializable
data class Npc(
    val npcId: String,
    val npcName: String,
    val dialogue: String,
    val dialogueConditions: List<DialogueCondition> = emptyList(),
    val isActive: Boolean
)

@Serializable
data class Item(
    val itemId: String,
    val itemName: String,
    val isCollectable: Boolean,
    val itemType: String = "generic",
    val itemMessage: String? = null
)

@Serializable
data class Interactable(
    val id: String,
    val name: String,
    val examineText: String,
    val canInteract: Boolean,
    val interactionText: String? = null,
    val unlockFlag: String? = null
)

@Serializable
data class VisibleObjects(
    val npcs: List<Npc> = emptyList(),
    val items: MutableList<Item> = mutableListOf(),
    val interactables: List<Interactable> = emptyList()
)

@Serializable
data class Quadrant(
    val quadrantId: String,
    val name: String,
    val description: Description,
    val connections: Connections,
    val visibleObjects: VisibleObjects,
    val requiredFlags: List<String> = emptyList(),
    val unlockFlags: List<String> = emptyList()
)
