package study.coco.project_code

import kotlinx.serialization.json.Json
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

class World {
    var quadrants = listOf<Quadrant>()

    fun loadQuadrants(directory: String) {
        val json = Json { ignoreUnknownKeys = true }
        val resolver = PathMatchingResourcePatternResolver()
        val resources = resolver.getResources("classpath:gameFiles/$directory/*.json")

        quadrants = resources.mapNotNull { resource ->
            try {
                val jsonString = resource.inputStream.bufferedReader().readText()
                json.decodeFromString<Quadrant>(jsonString)
            } catch (e: Exception) {
                println("Failed to parse ${resource.filename}: ${e.message}")
                null
            }
        }
    }
}