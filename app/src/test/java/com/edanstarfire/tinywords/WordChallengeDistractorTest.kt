import com.edanstarfire.tinywords.WordDefinition
import com.edanstarfire.tinywords.WordChallengeGenerator
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import android.content.Context
import org.junit.Test
import org.mockito.Mockito
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

    @Test
    fun noChallengeHasDuplicateTargetWordsAmongChoices() {
        val jsonPath = "src/main/assets/word_definitions.json"
        val jsonString = File(jsonPath).readText()
        val wordDefs: List<WordDefinition> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        val context = Mockito.mock(Context::class.java)
        val generator = WordChallengeGenerator(context, wordDefs, deterministic = true, skipImageLookupForTest = true)

        val errors = mutableListOf<String>()
        for (word in wordDefs) {
            val challenge = generator.generateChallenge(word.targetWord)
            if (challenge != null) {
                val choices = listOf(
                    challenge.targetWord,
                    challenge.incorrectImageWord1,
                    challenge.incorrectImageWord2
                )
                if (choices.size != choices.toSet().size) {
                    errors.add("Duplicate targetWord(s) found in challenge for '${word.targetWord}': $choices")
                }
            }
        }
        assert(errors.isEmpty()) {
            "One or more challenges contain duplicate targetWords among choices: ${"\n" + errors.joinToString("\n")}" 
        }
    }
}
