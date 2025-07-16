import com.edanstarfire.tinywords.WordDefinition
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File

class WordChallengeDistractorTest {
    @Test
    fun allWordsHaveAtLeastTwoSingleLetterDistractors() {
        val jsonPath = "src/main/assets/word_definitions.json"
        val jsonString = File(jsonPath).readText()
        val wordDefs: List<WordDefinition> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)

        val failingWords = mutableListOf<String>()
        wordDefs.forEach { word ->
            val candidatesByPosition = (0..2).map { pos ->
                wordDefs.filter { other ->
                    other != word &&
                        when(pos) {
                            0 -> other.part1Sound != word.part1Sound && other.part2Sound == word.part2Sound && other.part3Sound == word.part3Sound
                            1 -> other.part1Sound == word.part1Sound && other.part2Sound != word.part2Sound && other.part3Sound == word.part3Sound
                            2 -> other.part1Sound == word.part1Sound && other.part2Sound == word.part2Sound && other.part3Sound != word.part3Sound
                            else -> false
                        }
                }
            }
            val validDistractors = candidatesByPosition.any { it.size >= 2 }
            if (!validDistractors) {
                failingWords += word.targetWord
            }
        }

        if (failingWords.isNotEmpty()) {
            println("Words with insufficient distractors: $failingWords")
        }
        assert(failingWords.isEmpty()) {
            "The following words have fewer than 2 valid one-letter-differing distractors: $failingWords"
        }
    }
}
