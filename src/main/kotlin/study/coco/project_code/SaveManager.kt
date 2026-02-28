package study.coco.project_code

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import java.io.File

// stores and retrieves save data by player token, persisted to disk
@Component
class SaveManager {

    private val saveDir = File(System.getProperty("user.home"), ".utopian_chronicle_saves").also { it.mkdirs() }
    private val json = Json { ignoreUnknownKeys = true }

    private fun fileFor(token: String) = File(saveDir, "${token.replace(Regex("[^a-zA-Z0-9\\-]"), "_")}.json")

    fun save(token: String, data: SaveData) {
        try {
            fileFor(token).writeText(json.encodeToString(data))
        } catch (e: Exception) {
            println("Save failed for token $token: ${e.message}")
        }
    }

    fun load(token: String): SaveData? {
        return try {
            val file = fileFor(token)
            if (!file.exists()) return null
            json.decodeFromString<SaveData>(file.readText())
        } catch (e: Exception) {
            println("Load failed for token $token: ${e.message}")
            null
        }
    }

    fun delete(token: String) {
        try {
            fileFor(token).delete()
        } catch (e: Exception) {
            println("Delete failed for token $token: ${e.message}")
        }
    }
}
