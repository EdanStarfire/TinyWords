package com.edanstarfire.tinywords

import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*
import java.io.File

/**
 * Test suite for validating the new WordDefinition schema and migration results.
 * This ensures all words in the assets have been properly migrated to the enhanced format.
 */
class WordDefinitionSchemaTest {
    
    @Test
    fun allWordDefinitionsHaveNewSchemaFields() {
        val jsonPath = "src/main/assets/word_definitions.json"
        val jsonString = File(jsonPath).readText()
        val wordDefs: List<WordDefinition> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        
        assertTrue("Should have at least 100 word definitions", wordDefs.size >= 100)
        
        wordDefs.forEach { word ->
            // Verify all new fields are present and valid
            assertTrue("${word.targetWord}: phonicComplexity should be 1-5", 
                word.phonicComplexity in 1..5)
            
            assertNotNull("${word.targetWord}: soundType should not be null", word.soundType)
            
            assertTrue("${word.targetWord}: levelAvailability should not be empty", 
                word.levelAvailability.isNotEmpty())
            
            assertTrue("${word.targetWord}: levelAvailability should contain valid levels", 
                word.levelAvailability.all { it in 1..5 })
            
            assertNotNull("${word.targetWord}: tags should not be null", word.tags)
            
            // Verify soundType fields are valid
            assertTrue("${word.targetWord}: consonant1 should be valid", 
                word.soundType.consonant1 in setOf("simple", "blend", "digraph", "none"))
            
            assertTrue("${word.targetWord}: vowel should be valid", 
                word.soundType.vowel in setOf("short", "long", "digraph", "diphthong", "silent"))
            
            assertTrue("${word.targetWord}: consonant2 should be valid", 
                word.soundType.consonant2 in setOf("simple", "blend", "digraph", "none"))
            
            assertTrue("${word.targetWord}: pattern should be valid", 
                word.soundType.pattern in setOf("CVC", "CCVC", "CVCC", "CVCe", "CVVC"))
        }
    }
    
    @Test
    fun migrationStatisticsAreReasonable() {
        val jsonPath = "src/main/assets/word_definitions.json"
        val jsonString = File(jsonPath).readText()
        val wordDefs: List<WordDefinition> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        
        // Check complexity distribution
        val complexityStats = wordDefs.groupBy { it.phonicComplexity }.mapValues { it.value.size }
        
        // Most words should be complexity 1 (simple CVC)
        assertTrue("Should have complexity 1 words", complexityStats.getOrDefault(1, 0) > 0)
        assertTrue("Complexity 1 should be the majority", 
            complexityStats.getOrDefault(1, 0) > wordDefs.size / 2)
        
        // Check that we have some tagged words
        val taggedWords = wordDefs.filter { it.tags.isNotEmpty() }
        assertTrue("Should have some words with tags", taggedWords.size > 20)
        
        // Check common tags exist
        val allTags = wordDefs.flatMap { it.tags }.toSet()
        val expectedTags = setOf("animal", "object", "action", "nature", "food", "body")
        val foundTags = allTags.intersect(expectedTags)
        assertTrue("Should have common semantic tags", foundTags.size >= 5)
        
        // Check level availability makes sense
        val level1Words = wordDefs.filter { 1 in it.levelAvailability }
        val level5Words = wordDefs.filter { 5 in it.levelAvailability }
        
        assertTrue("Most words should be available at level 1", level1Words.size > wordDefs.size / 2)
        assertTrue("All words should be available at level 5", level5Words.size == wordDefs.size)
    }
    
    @Test
    fun complexityLevelConsistencyWithAvailability() {
        val jsonPath = "src/main/assets/word_definitions.json"
        val jsonString = File(jsonPath).readText()
        val wordDefs: List<WordDefinition> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        
        wordDefs.forEach { word ->
            // Words with higher complexity should not be available at lower levels
            when (word.phonicComplexity) {
                1 -> {
                    assertTrue("${word.targetWord}: Complexity 1 should be available at level 1", 
                        1 in word.levelAvailability)
                }
                2 -> {
                    assertFalse("${word.targetWord}: Complexity 2 should NOT be available at level 1", 
                        1 in word.levelAvailability)
                    assertTrue("${word.targetWord}: Complexity 2 should be available at level 2", 
                        2 in word.levelAvailability)
                }
                3 -> {
                    assertFalse("${word.targetWord}: Complexity 3 should NOT be available at levels 1-2", 
                        word.levelAvailability.any { it < 3 })
                    assertTrue("${word.targetWord}: Complexity 3 should be available at level 3", 
                        3 in word.levelAvailability)
                }
                4 -> {
                    assertFalse("${word.targetWord}: Complexity 4 should NOT be available at levels 1-3", 
                        word.levelAvailability.any { it < 4 })
                    assertTrue("${word.targetWord}: Complexity 4 should be available at level 4", 
                        4 in word.levelAvailability)
                }
                5 -> {
                    assertEquals("${word.targetWord}: Complexity 5 should ONLY be available at level 5", 
                        listOf(5), word.levelAvailability)
                }
            }
        }
    }
    
    @Test
    fun allOriginalFieldsPreserved() {
        val jsonPath = "src/main/assets/word_definitions.json"
        val jsonString = File(jsonPath).readText()
        val wordDefs: List<WordDefinition> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        
        wordDefs.forEach { word ->
            // Verify original fields are still present and valid
            assertTrue("${word.targetWord}: targetWord should not be blank", 
                word.targetWord.isNotBlank())
            
            assertTrue("${word.targetWord}: imageResName should not be blank", 
                word.imageResName.isNotBlank())
            
            assertTrue("${word.targetWord}: imageResName should end with _image", 
                word.imageResName.endsWith("_image"))
            
            assertTrue("${word.targetWord}: part1Sound should not be blank", 
                word.part1Sound.isNotBlank())
            
            assertTrue("${word.targetWord}: part2Sound should not be blank", 
                word.part2Sound.isNotBlank())
            
            assertTrue("${word.targetWord}: part3Sound should not be blank", 
                word.part3Sound.isNotBlank())
            
            // Verify sound parts make sense
            assertEquals("${word.targetWord}: targetWord length should match parts", 
                word.targetWord.length, 
                word.part1Sound.length + word.part2Sound.length + word.part3Sound.length)
        }
    }
    
    @Test
    fun soundTypeClassificationConsistency() {
        val jsonPath = "src/main/assets/word_definitions.json"
        val jsonString = File(jsonPath).readText()
        val wordDefs: List<WordDefinition> = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        
        wordDefs.forEach { word ->
            val soundType = word.soundType
            
            // Most of our current words should be CVC pattern
            if (word.targetWord.length == 3) {
                assertEquals("${word.targetWord}: 3-letter words should have CVC pattern", 
                    "CVC", soundType.pattern)
            }
            
            // Complexity factors should match the complexity level
            if (word.phonicComplexity >= 3 && soundType.complexityFactors.contains("complex_vowel")) {
                assertTrue("${word.targetWord}: Words with complex vowels should end in Y or W", 
                    word.targetWord.endsWith("Y") || word.targetWord.endsWith("W"))
            }
            
            // hasSilentLetters should be false for most simple words
            if (word.phonicComplexity == 1) {
                assertFalse("${word.targetWord}: Complexity 1 words should not have silent letters", 
                    soundType.hasSilentLetters)
            }
        }
    }
}