package study.coco.project_code
import java.io.File
import kotlinx.serialization.json.Json

class World {
    var quadrants = listOf<Quadrant>()

    fun loadQuadrants(directory: String) {
        val json = Json { ignoreUnknownKeys = true }
        val resourceUrl = this::class.java.classLoader.getResource("gameFiles/$directory")

        if (resourceUrl == null) {
            println("Directory not found: gameFiles/$directory")
            return
        }

        val dir = File(resourceUrl.toURI())
        quadrants = dir.listFiles { file -> file.isFile && file.name.endsWith(".json") }
            ?.mapNotNull { file ->
                try {
                    val jsonString = file.readText()
                    json.decodeFromString<Quadrant>(jsonString)
                } catch (e: Exception) {
                    println("Failed to parse ${file.name}: ${e.message}")
                    null
                }
            } ?: emptyList()
    }
}