package com.edanstarfire.tinywords

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import com.google.common.base.Joiner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class WordChallengeGeneratorTest {

    private lateinit var mockContext: Context
    private lateinit var mockResources: Resources
    private lateinit var generator: WordChallengeGenerator

    // --- Test Data ---
    private val wordCat = WordDefinition("CAT", "cat_image", "C", "A", "T")
    private val wordBat = WordDefinition("BAT", "bat_image", "B", "A", "T")
    private val wordHat = WordDefinition("HAT", "hat_image", "H", "A", "T")
    private val wordDog = WordDefinition("DOG", "dog_image", "D", "O", "G") // A different word

    private val threeWordsList = listOf(wordCat, wordBat, wordHat)
    private val singleWordList = listOf(wordDog)
    private val emptyList = emptyList<WordDefinition>()

    @Before
    fun setUp() {
        mockResources = mock()
        mockContext = mock {
            // Correct way to stub properties/methods using Mockito-Kotlin
            on { resources } doReturn mockResources
            on { packageName } doReturn "com.edanstarfire.tinywords.test" // Or any test package name
        }

        // Default stubbing for getIdentifier - return 1 for known, 0 for unknown
        // This makes tests less brittle to actual resource IDs.
        whenever(mockResources.getIdentifier(eq("cat_image"), eq("drawable"), any())).thenReturn(1)
        whenever(mockResources.getIdentifier(eq("bat_image"), eq("drawable"), any())).thenReturn(2)
        whenever(mockResources.getIdentifier(eq("hat_image"), eq("drawable"), any())).thenReturn(3)
        whenever(mockResources.getIdentifier(eq("dog_image"), eq("drawable"), any())).thenReturn(4)
        whenever(
            mockResources.getIdentifier(
                eq("non_existent_image"),
                eq("drawable"),
                any()
            )
        ).thenReturn(0)
    }

    @Test
    fun `generateChallenge - with empty list - returns null`() {
        generator = WordChallengeGenerator(mockContext, emptyList)
        val challenge = generator.generateChallenge("CAT")
        assertThat(challenge).isNull()
    }

    @Test
    fun `generateChallenge - target word not in list - returns null`() {
        generator = WordChallengeGenerator(mockContext, threeWordsList)
        val challenge = generator.generateChallenge("PIG")
        assertThat(challenge).isNull()
    }

    @Test
    fun `generateChallenge - valid target CAT from three words - returns challenge`() {
        generator = WordChallengeGenerator(mockContext, threeWordsList)
        val challenge = generator.generateChallenge("CAT")

        assertThat(challenge).isNotNull()
        assertThat(challenge?.targetWord).isEqualTo("CAT")
        assertThat(challenge?.correctImageWord).isEqualTo("CAT")
        assertThat(challenge?.correctImageRes).isEqualTo(1) // from mock setup

        // Check distractors (order might be random due to .shuffled())
        val incorrectWords = listOf(challenge?.incorrectImageWord1, challenge?.incorrectImageWord2)
        assertThat(incorrectWords).containsExactlyElementsIn(listOf("BAT", "HAT"))
            .inOrder() // Or .containsExactly() if order doesn't matter and you sort
        assertThat(challenge?.incorrectImageWord1).isNotEqualTo(challenge?.incorrectImageWord2)

        val incorrectImageRes = listOf(challenge?.incorrectImageRes1, challenge?.incorrectImageRes2)
        assertThat(incorrectImageRes).containsExactlyElementsIn(listOf(2, 3))
            .inOrder() // Or .containsExactly()
    }

    @Test
    fun `generateChallenge - not enough suitable distractors - returns null`() {
        // Current logic might still find 0 specific distractors, and then fallback also finds 0 others
        generator = WordChallengeGenerator(mockContext, listOf(wordCat)) // Only "CAT" in the list
        val challenge = generator.generateChallenge("CAT")
        assertThat(challenge).isNull()
    }

    @Test
    fun `generateChallenge - one image resource missing - returns null`() {
        // Make "bat_image" return 0 from getIdentifier for this test
        whenever(mockResources.getIdentifier(eq("bat_image"), eq("drawable"), any())).thenReturn(0)
        generator = WordChallengeGenerator(mockContext, threeWordsList)
        val challenge = generator.generateChallenge("CAT")
        assertThat(challenge).isNull()
    }


    @Test
    fun `getRandomInitialChallenge - with valid list - returns a challenge`() {
        generator = WordChallengeGenerator(mockContext, threeWordsList)
        val challenge = generator.getRandomInitialChallenge()
        assertThat(challenge).isNotNull()
        assertThat(challenge?.targetWord).isIn(threeWordsList.map { it.targetWord })
    }

    @Test
    fun `getRandomInitialChallenge - with empty list - returns null`() {
        generator = WordChallengeGenerator(mockContext, emptyList)
        val challenge = generator.getRandomInitialChallenge()
        assertThat(challenge).isNull()
    }

    @Test
    fun `getNextWordTarget - sequential progression basic`() {
        generator = WordChallengeGenerator(mockContext, threeWordsList) // CAT, BAT, HAT
        var next = generator.getNextWordTarget("CAT")
        assertThat(next).isEqualTo("BAT")
        next = generator.getNextWordTarget("BAT")
        assertThat(next).isEqualTo("HAT")
        next = generator.getNextWordTarget("HAT")
        assertThat(next).isEqualTo("CAT") // Loops back
    }

    @Test
    fun `getNextWordTarget - previous word not found - returns first word from list`() {
        generator = WordChallengeGenerator(mockContext, threeWordsList)
        val next = generator.getNextWordTarget("DOG")
        assertThat(next).isEqualTo("CAT") // Fallback to first
    }

    @Test
    fun `getNextWordTarget - with empty list - returns null`() {
        generator = WordChallengeGenerator(mockContext, emptyList)
        val next = generator.getNextWordTarget("ANYTHING")
        assertThat(next).isNull()
    }

    @Test
    fun `getNextWordTarget - with single word list - returns the same word`() {
        generator = WordChallengeGenerator(mockContext, singleWordList) // Only "DOG"
        val next = generator.getNextWordTarget("DOG")
        // The current logic: (currentIndex + 1) % allWordDefinitions.size
        // if nextIndex == currentIndex && allWordDefinitions.size > 1 -> special handling
        // Otherwise, it returns the word at nextIndex. If size is 1, nextIndex is 0.
        assertThat(next).isEqualTo("DOG")
    }

    // getDrawableResId is private, but tested implicitly via generateChallenge.
    // If you wanted to test it directly, you could make it internal or use reflection (not recommended for general testing).
    // Or, extract it to a separate testable utility if it becomes complex.
    // We are testing its effect by mocking context.resources.getIdentifier.
}