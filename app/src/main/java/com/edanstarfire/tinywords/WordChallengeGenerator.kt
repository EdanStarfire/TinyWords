package com.edanstarfire.tinywords

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import java.io.IOException
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Or ViewModelComponent::class if WordChallengeGenerator's lifecycle should be tied to ViewModels
object GameModule { // You can name this module as you see fit

    // Provider for List<WordDefinition>
    @Provides
    @Singleton // Assuming word definitions are loaded once and reused
    fun provideWordDefinitions(@ApplicationContext context: Context): List<WordDefinition> {
        val jsonString: String
        try {
            // It's generally better to use applicationContext.assets to avoid context leaks
            // if this module/dependency lives longer than an Activity/Fragment context.
            jsonString = context.assets.open("word_definitions.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {
            // Log the error or handle it appropriately
            Log.e("GameModule", "Error reading word_definitions.json", ioException)
            return emptyList()
        }

        return try {
            // Phase 6.1: Enhanced JSON parsing with backward compatibility
            val json = Json { 
                ignoreUnknownKeys = true // Handle both old and new JSON formats
                coerceInputValues = true // Handle type mismatches gracefully
            }
            
            val definitions = json.decodeFromString<List<WordDefinition>>(jsonString)
            Log.i("GameModule", "Successfully loaded ${definitions.size} word definitions")
            
            // Validate the loaded definitions
            val validatedDefinitions = definitions.filter { definition ->
                val isValid = definition.targetWord.isNotBlank() && 
                             definition.imageResName.isNotBlank() &&
                             definition.phonicComplexity in 1..5 &&
                             definition.levelAvailability.isNotEmpty() &&
                             definition.levelAvailability.all { it in 1..5 }
                
                if (!isValid) {
                    Log.w("GameModule", "Invalid word definition: ${definition.targetWord}")
                }
                isValid
            }
            
            Log.i("GameModule", "Validated ${validatedDefinitions.size} word definitions")
            validatedDefinitions
            
        } catch (e: Exception) {
            // Log the error or handle it appropriately
            Log.e("GameModule", "Error parsing word definitions JSON", e)
            return emptyList()
        }
    }

    // Provider for WordChallengeGenerator
    @Provides
    // Consider if WordChallengeGenerator should be a @Singleton or scoped differently
    // If it's used by ViewModels and doesn't hold mutable state that needs to be shared globally,
    // ViewModelScoped might be more appropriate.
    // For simplicity, SingletonComponent is often a good start if it's stateless or its state is app-wide.
    @Singleton // If you want a single instance of the generator
    fun provideWordChallengeGenerator(
        @ApplicationContext context: Context, // Hilt provides this
        allWordDefinitions: List<WordDefinition> // Hilt provides this from the method above
    ): WordChallengeGenerator {
        return WordChallengeGenerator(context, allWordDefinitions)
    }
}

class WordChallengeGenerator (
    private val context: Context?, // May be null in unit tests
    private val allWordDefinitions: List<WordDefinition>, // Injected list
    private val deterministic: Boolean = false, // Use fixed order for tests if true
    private val skipImageLookupForTest: Boolean = false // New parameter for tests
) {

    fun generateChallenge(currentWordString: String): WordChallenge? {
        if (allWordDefinitions.isEmpty()) {
            Log.e("WordChallengeGenerator", "Cannot generate challenge: Word definitions list is empty.")
            return null
        }

        val targetDefinition = allWordDefinitions.find { it.targetWord.equals(currentWordString, ignoreCase = true) }
        if (targetDefinition == null) {
            if (context != null) Log.w("WordChallengeGenerator", "Target word '$currentWordString' not found in definitions.")
            return null
        }

        // Multi-position distractor search
        val positions = if (deterministic) listOf(0,1,2) else listOf(0,1,2).shuffled()
        val targetParts = listOf(targetDefinition.part1Sound, targetDefinition.part2Sound, targetDefinition.part3Sound)
        var foundDistractors: List<WordDefinition>? = null
        for(idx in positions) {
            val distractors = allWordDefinitions.filter { definition ->
                if (definition.targetWord == targetDefinition.targetWord) return@filter false
                val defParts = listOf(definition.part1Sound, definition.part2Sound, definition.part3Sound)
                defParts.withIndex().all { (i, v) -> if (i == idx) v != targetParts[i] else v == targetParts[i] }
            }
            val result = if (deterministic) distractors else distractors.shuffled()
            val distinctResult = result.distinctBy { it.targetWord }
            if (distinctResult.size >= 2) {
                foundDistractors = distinctResult.take(2)
                break
            }
        }
        if (foundDistractors != null) {
            val incorrect1Definition = foundDistractors[0]
            val incorrect2Definition = foundDistractors[1]
            return createWordChallenge(targetDefinition, incorrect1Definition, incorrect2Definition)
        }
        if (context != null) Log.w("WordChallengeGenerator", "Not enough suitable distractors found for '${targetDefinition.targetWord}'. Searched all letter positions.")
        val otherDistractors = if (deterministic) {
            allWordDefinitions.filter { it.targetWord != targetDefinition.targetWord }
        } else {
            allWordDefinitions.filter { it.targetWord != targetDefinition.targetWord }.shuffled()
        }
        val distinctOtherDistractors = otherDistractors.distinctBy { it.targetWord }
        if (distinctOtherDistractors.size < 2) {
            if (context != null) Log.e("WordChallengeGenerator", "Globally not enough distinct words to form a challenge for '${targetDefinition.targetWord}'")
            return null
        }
        val incorrect1Definition = distinctOtherDistractors[0]
        val incorrect2Definition = distinctOtherDistractors[1]
        return createWordChallenge(targetDefinition, incorrect1Definition, incorrect2Definition)
    }

    private fun createWordChallenge(
        targetDef: WordDefinition,
        incorrect1Def: WordDefinition,
        incorrect2Def: WordDefinition
    ): WordChallenge? {
        val targetImageResId = getDrawableResId(targetDef.imageResName)
        val incorrect1ImageResId = getDrawableResId(incorrect1Def.imageResName)
        val incorrect2ImageResId = getDrawableResId(incorrect2Def.imageResName)

        if (targetImageResId == 0 || incorrect1ImageResId == 0 || incorrect2ImageResId == 0) {
            Log.e(
                "WordChallengeGenerator", "Could not find one or more image resources for challenge: " +
                        "${targetDef.imageResName}(${targetImageResId}), " +
                        "${incorrect1Def.imageResName}(${incorrect1ImageResId}), " +
                        "${incorrect2Def.imageResName}(${incorrect2ImageResId})"
            )
        }

        val order = if (deterministic) listOf(0, 1, 2) else listOf(0, 1, 2).shuffled()
        return WordChallenge(
            targetWord = targetDef.targetWord,
            correctImageWord = targetDef.targetWord,
            correctImageRes = targetImageResId,
            incorrectImageWord1 = incorrect1Def.targetWord,
            incorrectImageRes1 = incorrect1ImageResId,
            incorrectImageWord2 = incorrect2Def.targetWord,
            incorrectImageRes2 = incorrect2ImageResId,
            choiceOrder = order
        )
    }

    private fun getDrawableResId(imageName: String): Int {
        if (skipImageLookupForTest) return 1
        if (context == null) return 0
        @SuppressLint("DiscouragedApi")
        val res = context.resources
        if (res == null) return 0
        val resId = res.getIdentifier(imageName, "drawable", context.packageName)
        if (resId == 0) {
            Log.w("WordChallengeGenerator", "Drawable resource not found for name: $imageName")
        }
        return resId
    }

    fun getRandomInitialChallenge(exclude: Set<String> = emptySet()): WordChallenge? {
        if (allWordDefinitions.isEmpty()) {
            Log.e("WordChallengeGenerator", "Word definitions list is empty. Cannot get random challenge.")
            return null
        }
        val candidateDefs = allWordDefinitions.filter { it.targetWord !in exclude }
        val useList = if (candidateDefs.isEmpty()) allWordDefinitions else candidateDefs
        val randomInitialWordDefinition = if (deterministic) useList.first() else useList.random()
        return generateChallenge(randomInitialWordDefinition.targetWord)
    }

    /**
     * Get a random initial challenge for a specific difficulty level
     * This ensures the target word is appropriate for the level complexity
     */
    fun getRandomLevelChallenge(level: Int, exclude: Set<String> = emptySet()): WordChallenge? {
        if (allWordDefinitions.isEmpty()) {
            Log.e("WordChallengeGenerator", "Word definitions list is empty. Cannot get random level challenge.")
            return null
        }

        // Filter words appropriate for this level - target should match complexity
        val levelAppropriateWords = allWordDefinitions.filter { definition ->
            definition.phonicComplexity == level &&
            definition.targetWord !in exclude
        }

        if (levelAppropriateWords.isEmpty()) {
            if (context != null) {
                Log.e("WordChallengeGenerator", "No words available for level $level")
            }
            return null
        }

        val randomWordDef = if (deterministic) levelAppropriateWords.first() else levelAppropriateWords.random()
        return generateLevelChallenge(randomWordDef.targetWord, level)
    }


    fun getNextWordTarget(previousCorrectWord: String): String? {
        if (allWordDefinitions.isEmpty()) return null

        val currentIndex = allWordDefinitions.indexOfFirst { it.targetWord.equals(previousCorrectWord, ignoreCase = true) }

        if (currentIndex == -1) {
            if (context != null) Log.w("WordChallengeGenerator", "Previous word '$previousCorrectWord' not found. Returning first word.")
            return allWordDefinitions.firstOrNull()?.targetWord // Fallback
        }

        // Simple sequential progression for now
        // For your CVC game, you'll want logic that finds a word with one sound changed
        // This is a placeholder for that more complex logic.
        val nextIndex = (currentIndex + 1) % allWordDefinitions.size // Loop back to the start
        if (nextIndex == currentIndex && allWordDefinitions.size > 1) {
            // This case is unlikely with simple looping unless only one word exists.
            // Handle if there's only one word, or if logic needs to be more robust.
            if (context != null) Log.w("WordChallengeGenerator", "Only one unique word or looped back to the same word immediately.")
            // Potentially pick a random one if not the same or if list is small.
            if (allWordDefinitions.size > 1) {
                return allWordDefinitions.filterNot { it.targetWord.equals(previousCorrectWord, ignoreCase = true) }.randomOrNull()?.targetWord
            }
        }
        return allWordDefinitions[nextIndex].targetWord
    }

    // Phase 6.2: Level-aware challenge generation methods
    
    /**
     * Generate a challenge for a specific target word at a given difficulty level
     */
    fun generateLevelChallenge(targetWord: String, difficultyLevel: Int): WordChallenge? {
        if (allWordDefinitions.isEmpty()) {
            Log.e("WordChallengeGenerator", "Cannot generate level challenge: Word definitions list is empty.")
            return null
        }

        if (difficultyLevel !in 1..5) {
            Log.e("WordChallengeGenerator", "Invalid difficulty level: $difficultyLevel. Must be 1-5.")
            return null
        }

        val targetDefinition = allWordDefinitions.find { it.targetWord.equals(targetWord, ignoreCase = true) }
        if (targetDefinition == null) {
            if (context != null) Log.w("WordChallengeGenerator", "Target word '$targetWord' not found in definitions.")
            return null
        }

        // Check if word is appropriate for the level
        if (!targetDefinition.levelAvailability.contains(difficultyLevel)) {
            if (context != null) Log.w("WordChallengeGenerator", "Word '$targetWord' not available for level $difficultyLevel")
            return null
        }

        val distractors = findLevelAppropriateDistractors(targetDefinition, difficultyLevel)
        
        if (distractors.size < 2) {
            if (context != null) Log.e("WordChallengeGenerator", "Not enough level-appropriate distractors for '$targetWord' at level $difficultyLevel")
            return null
        }

        val incorrect1Definition = distractors[0]
        val incorrect2Definition = distractors[1]
        val challenge = createWordChallenge(targetDefinition, incorrect1Definition, incorrect2Definition)
        
        return if (validateChallengeAppropriateForLevel(challenge, difficultyLevel)) {
            challenge
        } else {
            if (context != null) Log.w("WordChallengeGenerator", "Generated challenge failed level validation for level $difficultyLevel")
            null
        }
    }

    /**
     * Find distractors appropriate for the given difficulty level
     */
    fun findLevelAppropriateDistractors(target: WordDefinition, level: Int): List<WordDefinition> {
        return when (level) {
            1 -> findLevel1Distractors(target)
            2 -> findLevel2Distractors(target)
            3 -> findLevel3Distractors(target)
            4 -> findLevel4Distractors(target)
            5 -> findLevel5Distractors(target)
            else -> {
                if (context != null) Log.w("WordChallengeGenerator", "Unsupported level $level, falling back to level 1")
                findLevel1Distractors(target)
            }
        }
    }

    /**
     * Validate that a challenge is appropriate for the given level
     */
    fun validateChallengeAppropriateForLevel(challenge: WordChallenge?, level: Int): Boolean {
        if (challenge == null) return false
        
        // Find the definitions for all words in the challenge
        val targetDef = allWordDefinitions.find { it.targetWord.equals(challenge.targetWord, ignoreCase = true) }
        val distractor1Def = allWordDefinitions.find { it.targetWord.equals(challenge.incorrectImageWord1, ignoreCase = true) }
        val distractor2Def = allWordDefinitions.find { it.targetWord.equals(challenge.incorrectImageWord2, ignoreCase = true) }
        
        if (targetDef == null || distractor1Def == null || distractor2Def == null) {
            return false
        }
        
        // Check if all words are available at the specified level
        return targetDef.levelAvailability.contains(level) &&
               distractor1Def.levelAvailability.contains(level) &&
               distractor2Def.levelAvailability.contains(level)
    }

    // Level-specific distractor finding algorithms

    /**
     * Level 1: Single Letter Variation - Enhanced phonetic matching for CVC patterns
     * Focus on simple consonant-vowel-consonant words with single position changes
     */
    private fun findLevel1Distractors(target: WordDefinition): List<WordDefinition> {
        // Filter candidates to only include Level 1 appropriate words
        val level1Words = allWordDefinitions.filter { definition ->
            definition.targetWord != target.targetWord &&
            definition.levelAvailability.contains(1) &&
            definition.phonicComplexity == 1 &&
            definition.soundType.pattern == "CVC"
        }

        // Prioritize single-position phonetic changes
        val positions = if (deterministic) listOf(0, 1, 2) else listOf(0, 1, 2).shuffled()
        val targetParts = listOf(target.part1Sound, target.part2Sound, target.part3Sound)
        
        for (idx in positions) {
            val distractors = level1Words.filter { definition ->
                val defParts = listOf(definition.part1Sound, definition.part2Sound, definition.part3Sound)
                // Ensure exactly one position differs and the other two match
                defParts.withIndex().all { (i, v) -> 
                    if (i == idx) v != targetParts[i] else v == targetParts[i] 
                }
            }
            
            val result = if (deterministic) distractors else distractors.shuffled()
            val distinctResult = result.distinctBy { it.targetWord }
            
            if (distinctResult.size >= 2) {
                return distinctResult.take(2)
            }
        }
        
        // Fallback to any Level 1 words if specific phonetic matching fails
        if (context != null) {
            Log.w("WordChallengeGenerator", "Level 1: Using fallback distractors for '${target.targetWord}'")
        }
        val fallbackDistractors = if (deterministic) level1Words else level1Words.shuffled()
        return fallbackDistractors.distinctBy { it.targetWord }.take(2)
    }

    /**
     * Level 2: Consonant Blends & Digraphs - Focus on CCVC patterns and consonant clusters
     * Includes words with consonant blends (br, cl, st) and digraphs (ch, th, sh)
     */
    private fun findLevel2Distractors(target: WordDefinition): List<WordDefinition> {
        // Filter candidates to include Level 2 appropriate words
        val level2Words = allWordDefinitions.filter { definition ->
            definition.targetWord != target.targetWord &&
            definition.levelAvailability.contains(2) &&
            definition.phonicComplexity <= 2
        }

        // Try to find distractors with phonetic patterns that differ by one position
        val targetParts = listOf(target.part1Sound, target.part2Sound, target.part3Sound)
        val positions = if (deterministic) listOf(0, 1, 2) else listOf(0, 1, 2).shuffled()
        
        for (idx in positions) {
            val distractors = level2Words.filter { definition ->
                val defParts = listOf(definition.part1Sound, definition.part2Sound, definition.part3Sound)
                
                // For consonant positions (0, 2), allow blend/digraph variations
                // For vowel position (1), require exact match to maintain phonetic consistency
                when (idx) {
                    0, 2 -> {
                        // Different consonant sound but similar complexity level
                        defParts[idx] != targetParts[idx] &&
                        defParts[1] == targetParts[1] && // Same vowel
                        (if (idx == 0) defParts[2] == targetParts[2] else defParts[0] == targetParts[0])
                    }
                    1 -> {
                        // Different vowel but same consonant framework
                        defParts[1] != targetParts[1] &&
                        defParts[0] == targetParts[0] &&
                        defParts[2] == targetParts[2]
                    }
                    else -> false
                }
            }
            
            val result = if (deterministic) distractors else distractors.shuffled()
            val distinctResult = result.distinctBy { it.targetWord }
            
            if (distinctResult.size >= 2) {
                return distinctResult.take(2)
            }
        }
        
        // Fallback 1: Prefer words with same phonicComplexity level
        val sameComplexityWords = level2Words.filter { it.phonicComplexity == target.phonicComplexity }
        if (sameComplexityWords.size >= 2) {
            if (context != null) {
                Log.w("WordChallengeGenerator", "Level 2: Using same complexity fallback for '${target.targetWord}'")
            }
            val fallbackDistractors = if (deterministic) sameComplexityWords else sameComplexityWords.shuffled()
            return fallbackDistractors.distinctBy { it.targetWord }.take(2)
        }
        
        // Fallback 2: Any Level 2 words if complexity matching also fails
        if (context != null) {
            Log.w("WordChallengeGenerator", "Level 2: Using broad fallback distractors for '${target.targetWord}'")
        }
        val fallbackDistractors = if (deterministic) level2Words else level2Words.shuffled()
        return fallbackDistractors.distinctBy { it.targetWord }.take(2)
    }

    /**
     * Level 3: Complex Vowel Patterns (placeholder for future implementation)
     */
    private fun findLevel3Distractors(target: WordDefinition): List<WordDefinition> {
        // Placeholder implementation - filter to Level 3 words
        val level3Words = allWordDefinitions.filter { definition ->
            definition.targetWord != target.targetWord &&
            definition.levelAvailability.contains(3) &&
            definition.phonicComplexity <= 3
        }
        
        val result = if (deterministic) level3Words else level3Words.shuffled()
        return result.distinctBy { it.targetWord }.take(2)
    }

    /**
     * Level 4: Plural Forms & Morphology (placeholder for future implementation)
     */
    private fun findLevel4Distractors(target: WordDefinition): List<WordDefinition> {
        // Placeholder implementation - filter to Level 4 words
        val level4Words = allWordDefinitions.filter { definition ->
            definition.targetWord != target.targetWord &&
            definition.levelAvailability.contains(4) &&
            definition.phonicComplexity <= 4
        }
        
        val result = if (deterministic) level4Words else level4Words.shuffled()
        return result.distinctBy { it.targetWord }.take(2)
    }

    /**
     * Level 5: Multi-Word Concepts (placeholder for future implementation)
     */
    private fun findLevel5Distractors(target: WordDefinition): List<WordDefinition> {
        // Placeholder implementation - filter to Level 5 words
        val level5Words = allWordDefinitions.filter { definition ->
            definition.targetWord != target.targetWord &&
            definition.levelAvailability.contains(5)
        }
        
        val result = if (deterministic) level5Words else level5Words.shuffled()
        return result.distinctBy { it.targetWord }.take(2)
    }
// Utility function to load definitions - this can live outside, e.g., in a Repository or ViewModel,
// or as a top-level function in a utility file.
// For simplicity, let's assume it's moved to where WordChallengeGenerator is instantiated.
// For example, in your ViewModel or a data Repository:
    /*
    fun loadWordDefinitionsFromAssets(appContext: Context): List<WordDefinition> {
        val jsonString: String
        try {
            jsonString = appContext.assets.open("word_definitions.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {
            Log.e("DataLoading", "Error reading word_definitions.json", ioException)
            return emptyList()
        }

        return try {
            Json.decodeFromString<List<WordDefinition>>(jsonString)
        } catch (e: Exception) {
            Log.e("DataLoading", "Error parsing JSON from word_definitions.json", e)
            return emptyList()
        }
    }
    */

    /**
     * Get word definition by target word for UI highlighting purposes
     */
    fun getWordDefinition(targetWord: String): WordDefinition? {
        return allWordDefinitions.find { it.targetWord.equals(targetWord, ignoreCase = true) }
    }

}
