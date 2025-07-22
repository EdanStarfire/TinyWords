import com.edanstarfire.tinywords.WordDefinition
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File

class WordDefinitionTest {
    @Test
    fun allWordDefinitionsHaveMatchingImages() {
        val jsonPath = "src/main/assets/word_definitions.json"
        val jsonString = File(jsonPath).readText()
        val wordDefs: List<WordDefinition> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        val drawablePath = "src/main/res/drawable/"
        val missingImages = mutableListOf<String>()
        for (word in wordDefs) {
            val imageFile = File(drawablePath + word.imageResName + ".png")
            if (!imageFile.exists()) {
                missingImages += word.targetWord + " (" + word.imageResName + ".png" + ")"
            }
        }
        if (missingImages.isNotEmpty()) {
            println("${missingImages.size}/${wordDefs.size} word definitions reference missing images.")
        }
        assert(missingImages.isEmpty()) {
            "These word definitions reference missing drawable images: ${"\n * " + missingImages.joinToString(separator = "\n * ")}"
        }
    }

    @Test
    fun noDuplicateTargetWordsInDefinitions() {
        val jsonPath = "src/main/assets/word_definitions.json"
        val jsonString = File(jsonPath).readText()
        val wordDefs: List<WordDefinition> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        val duplicates = wordDefs.groupBy { it.targetWord.lowercase() }.filter { it.value.size > 1 }
        assert(duplicates.isEmpty()) {
            "Duplicate targetWord entries found in word_definitions.json: ${duplicates.keys.joinToString(", ")}"
        }
    }
}
