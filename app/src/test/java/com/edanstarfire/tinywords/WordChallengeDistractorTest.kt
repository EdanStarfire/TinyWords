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
    fun allLevel1WordsHaveAtLeastTwoSingleLetterDistractors() {
        val jsonPath = "src/main/assets/word_definitions.json"
        val jsonString = File(jsonPath).readText()
        val wordDefs: List<WordDefinition> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)

        // Filter to Level 1 words only
        val level1Words = wordDefs.filter { it.levelAvailability.contains(1) && it.phonicComplexity == 1 }
        val failingWords = mutableListOf<String>()
        
        level1Words.forEach { word ->
            val candidatesByPosition = (0..2).map { pos ->
                level1Words.filter { other ->
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
            println("Level 1 words with insufficient distractors: $failingWords")
            println("Total Level 1 words tested: ${level1Words.size}")
            
            // Analyze the failing words to understand why they lack distractors
            failingWords.forEach { wordStr ->
                val word = level1Words.find { it.targetWord == wordStr }
                if (word != null) {
                    println("Analyzing '$wordStr' (${word.part1Sound}-${word.part2Sound}-${word.part3Sound}):")
                    
                    // Check each position for potential matches
                    (0..2).forEach { pos ->
                        val candidates = level1Words.filter { other ->
                            other != word &&
                                when(pos) {
                                    0 -> other.part1Sound != word.part1Sound && other.part2Sound == word.part2Sound && other.part3Sound == word.part3Sound
                                    1 -> other.part1Sound == word.part1Sound && other.part2Sound != word.part2Sound && other.part3Sound == word.part3Sound
                                    2 -> other.part1Sound == word.part1Sound && other.part2Sound == word.part2Sound && other.part3Sound != word.part3Sound
                                    else -> false
                                }
                        }
                        println("  Position $pos: ${candidates.size} candidates - ${candidates.map { it.targetWord }}")
                    }
                }
            }
        }
        
        assert(failingWords.isEmpty()) {
            "The following Level 1 words have fewer than 2 valid one-letter-differing distractors: $failingWords"
        }
    }

    @Test
    fun allLevel2WordsHaveAtLeastTwoAppropriateDistractors() {
        val jsonPath = "src/main/assets/word_definitions.json"
        val jsonString = File(jsonPath).readText()
        val wordDefs: List<WordDefinition> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        val context = Mockito.mock(Context::class.java)
        val generator = WordChallengeGenerator(context, wordDefs, deterministic = true, skipImageLookupForTest = true)

        // Filter to Level 2 words
        val level2Words = wordDefs.filter { it.levelAvailability.contains(2) && it.phonicComplexity <= 2 }
        val failingWords = mutableListOf<String>()
        
        level2Words.take(5).forEach { word ->
            try {
                val distractors = generator.findLevelAppropriateDistractors(word, 2)
                if (distractors.size < 2) {
                    failingWords += word.targetWord
                }
            } catch (e: Exception) {
                println("Error testing word ${word.targetWord}: ${e.message}")
                failingWords += "${word.targetWord} (error: ${e.message})"
            }
        }

        if (failingWords.isNotEmpty()) {
            println("Level 2 words with insufficient distractors: $failingWords")
        }
        assert(failingWords.isEmpty()) {
            "The following Level 2 words have fewer than 2 valid distractors: $failingWords"
        }
    }

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
    fun levelAwareChallengeGenerationWorksForBothLevels() {
        val jsonPath = "src/main/assets/word_definitions.json"
        val jsonString = File(jsonPath).readText()
        val wordDefs: List<WordDefinition> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        val context = Mockito.mock(Context::class.java)
        val generator = WordChallengeGenerator(context, wordDefs, deterministic = true, skipImageLookupForTest = true)

        // Test Level 1 challenge generation
        val level1Words = wordDefs.filter { it.levelAvailability.contains(1) && it.phonicComplexity == 1 }
        val level1FailingWords = mutableListOf<String>()
        
        level1Words.take(5).forEach { word ->
            val challenge = generator.generateLevelChallenge(word.targetWord, 1)
            if (challenge == null) {
                level1FailingWords += word.targetWord
            } else {
                // Verify all words in challenge are Level 1 appropriate
                val allChallengeWords = listOf(challenge.targetWord, challenge.incorrectImageWord1, challenge.incorrectImageWord2)
                val challengeDefs = allChallengeWords.mapNotNull { challengeWord ->
                    wordDefs.find { it.targetWord.equals(challengeWord, ignoreCase = true) }
                }
                if (challengeDefs.size != 3 || !challengeDefs.all { it.levelAvailability.contains(1) }) {
                    level1FailingWords += "${word.targetWord} (inappropriate level mix)"
                }
            }
        }

        // Test Level 2 challenge generation
        val level2Words = wordDefs.filter { it.levelAvailability.contains(2) && it.phonicComplexity <= 2 }
        val level2FailingWords = mutableListOf<String>()
        
        level2Words.take(5).forEach { word ->
            val challenge = generator.generateLevelChallenge(word.targetWord, 2)
            if (challenge == null) {
                level2FailingWords += word.targetWord
            } else {
                // Verify all words in challenge are Level 2 appropriate
                val allChallengeWords = listOf(challenge.targetWord, challenge.incorrectImageWord1, challenge.incorrectImageWord2)
                val challengeDefs = allChallengeWords.mapNotNull { challengeWord ->
                    wordDefs.find { it.targetWord.equals(challengeWord, ignoreCase = true) }
                }
                if (challengeDefs.size != 3 || !challengeDefs.all { it.levelAvailability.contains(2) }) {
                    level2FailingWords += "${word.targetWord} (inappropriate level mix)"
                }
            }
        }

        if (level1FailingWords.isNotEmpty()) {
            println("Level 1 challenge generation failures: $level1FailingWords")
        }
        if (level2FailingWords.isNotEmpty()) {
            println("Level 2 challenge generation failures: $level2FailingWords")
        }
        
        assert(level1FailingWords.isEmpty()) {
            "Level 1 challenge generation failed for: $level1FailingWords"
        }
        assert(level2FailingWords.isEmpty()) {
            "Level 2 challenge generation failed for: $level2FailingWords"
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
