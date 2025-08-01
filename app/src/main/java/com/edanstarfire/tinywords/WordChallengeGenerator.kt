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
            ioException.printStackTrace()
            return emptyList()
        }

        return try {
            Json { ignoreUnknownKeys = true } // Good practice to add ignoreUnknownKeys
                .decodeFromString<List<WordDefinition>>(jsonString)
        } catch (e: Exception) {
            // Log the error or handle it appropriately
            e.printStackTrace()
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
            Log.w("WordChallengeGenerator", "Target word '$currentWordString' not found in definitions.")
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
        Log.w("WordChallengeGenerator", "Not enough suitable distractors found for '${targetDefinition.targetWord}'. Searched all letter positions.")
        val otherDistractors = if (deterministic) {
            allWordDefinitions.filter { it.targetWord != targetDefinition.targetWord }
        } else {
            allWordDefinitions.filter { it.targetWord != targetDefinition.targetWord }.shuffled()
        }
        val distinctOtherDistractors = otherDistractors.distinctBy { it.targetWord }
        if (distinctOtherDistractors.size < 2) {
            Log.e("WordChallengeGenerator", "Globally not enough distinct words to form a challenge for '${targetDefinition.targetWord}'")
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

    fun getNextWordTarget(previousCorrectWord: String): String? {
        if (allWordDefinitions.isEmpty()) return null

        val currentIndex = allWordDefinitions.indexOfFirst { it.targetWord.equals(previousCorrectWord, ignoreCase = true) }

        if (currentIndex == -1) {
            Log.w("WordChallengeGenerator", "Previous word '$previousCorrectWord' not found. Returning first word.")
            return allWordDefinitions.firstOrNull()?.targetWord // Fallback
        }

        // Simple sequential progression for now
        // For your CVC game, you'll want logic that finds a word with one sound changed
        // This is a placeholder for that more complex logic.
        val nextIndex = (currentIndex + 1) % allWordDefinitions.size // Loop back to the start
        if (nextIndex == currentIndex && allWordDefinitions.size > 1) {
            // This case is unlikely with simple looping unless only one word exists.
            // Handle if there's only one word, or if logic needs to be more robust.
            Log.w("WordChallengeGenerator", "Only one unique word or looped back to the same word immediately.")
            // Potentially pick a random one if not the same or if list is small.
            if (allWordDefinitions.size > 1) {
                return allWordDefinitions.filterNot { it.targetWord.equals(previousCorrectWord, ignoreCase = true) }.randomOrNull()?.targetWord
            }
        }
        return allWordDefinitions[nextIndex].targetWord
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

}
