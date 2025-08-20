package com.edanstarfire.tinywords

import org.junit.Test
import org.junit.Assert.*

/**
 * Test suite for validating phonetic classification accuracy and consistency.
 * These tests ensure that the PhoneticAnalyzer correctly classifies words
 * according to the multi-level complexity progression system.
 */
class PhoneticClassificationTest {
    
    @Test
    fun testSimpleComplexity1Words() {
        // Level 1: Simple CVC words with single letter variations
        val level1Words = listOf("CAT", "BAT", "HAT", "DOG", "PIG", "SUN", "CUP")
        
        level1Words.forEach { word ->
            val complexity = PhoneticAnalyzer.analyzePhoneticComplexity(word)
            assertEquals("$word should be complexity level 1", 1, complexity)
            
            val soundType = PhoneticAnalyzer.analyzeSoundType(word)
            assertEquals("$word should have simple consonant1", "simple", soundType.consonant1)
            assertEquals("$word should have short vowel", "short", soundType.vowel) 
            assertEquals("$word should have simple consonant2", "simple", soundType.consonant2)
            assertEquals("$word should have CVC pattern", "CVC", soundType.pattern)
            assertFalse("$word should not have silent letters", soundType.hasSilentLetters)
        }
    }
    
    @Test
    fun testComplexVowelComplexity3Words() {
        // Level 3: Complex vowel patterns (oy, ow, ay patterns)
        val level3Words = listOf("BOY", "TOY", "JOY", "BOW", "COW", "MOW", "HAY")
        
        level3Words.forEach { word ->
            val complexity = PhoneticAnalyzer.analyzePhoneticComplexity(word)
            assertEquals("$word should be complexity level 3", 3, complexity)
            
            val soundType = PhoneticAnalyzer.analyzeSoundType(word)
            assertTrue("$word should have complex_vowel in complexity factors", 
                soundType.complexityFactors.contains("complex_vowel"))
        }
    }
    
    @Test
    fun testPluralFormComplexity4Words() {
        // Level 4: Actual plural words (ending in 's')
        // Note: Morphological complexity (words ending in 'ed') is disabled in Phase 6.1
        val level4Words = listOf("CATS", "DOGS", "HATS")
        
        level4Words.forEach { word ->
            val complexity = PhoneticAnalyzer.analyzePhoneticComplexity(word)
            assertEquals("$word should be complexity level 4 (plural form)", 4, complexity)
            
            val soundType = PhoneticAnalyzer.analyzeSoundType(word)
            assertTrue("$word should have plural_form in complexity factors", 
                soundType.complexityFactors.contains("plural_form"))
        }
    }
    
    @Test
    fun testSemanticTagGeneration() {
        // Test animal tags
        val animalWords = mapOf(
            "CAT" to "animal", "DOG" to "animal", "PIG" to "animal", 
            "COW" to "animal", "FOX" to "animal", "BAT" to "animal"
        )
        
        animalWords.forEach { (word, expectedTag) ->
            val tags = PhoneticAnalyzer.generateSemanticTags(word)
            assertTrue("$word should have $expectedTag tag", tags.contains(expectedTag))
        }
        
        // Test action tags
        val actionWords = mapOf(
            "RUN" to "action", "DIG" to "action", "HIT" to "action", 
            "CUT" to "action", "SIT" to "action", "HOP" to "action"
        )
        
        actionWords.forEach { (word, expectedTag) ->
            val tags = PhoneticAnalyzer.generateSemanticTags(word)
            assertTrue("$word should have $expectedTag tag", tags.contains(expectedTag))
        }
        
        // Test object tags
        val objectWords = mapOf(
            "HAT" to "object", "CUP" to "object", "BOX" to "object", 
            "CAR" to "object", "BAG" to "object", "JET" to "object"
        )
        
        objectWords.forEach { (word, expectedTag) ->
            val tags = PhoneticAnalyzer.generateSemanticTags(word)
            assertTrue("$word should have $expectedTag tag", tags.contains(expectedTag))
        }
        
        // Test food tags
        val foodWords = mapOf(
            "HAM" to "food", "JAM" to "food", "BUN" to "food", 
            "NUT" to "food", "YAM" to "food"
        )
        
        foodWords.forEach { (word, expectedTag) ->
            val tags = PhoneticAnalyzer.generateSemanticTags(word)
            assertTrue("$word should have $expectedTag tag", tags.contains(expectedTag))
        }
        
        // Test body part tags
        val bodyWords = mapOf(
            "LEG" to "body", "HIP" to "body", "LIP" to "body", "RIB" to "body"
        )
        
        bodyWords.forEach { (word, expectedTag) ->
            val tags = PhoneticAnalyzer.generateSemanticTags(word)
            assertTrue("$word should have $expectedTag tag", tags.contains(expectedTag))
        }
        
        // Test nature tags
        val natureWords = mapOf(
            "SUN" to "nature", "FOG" to "nature", "LOG" to "nature", 
            "BUG" to "nature", "WEB" to "nature"
        )
        
        natureWords.forEach { (word, expectedTag) ->
            val tags = PhoneticAnalyzer.generateSemanticTags(word)
            assertTrue("$word should have $expectedTag tag", tags.contains(expectedTag))
        }
    }
    
    @Test
    fun testLevelAvailabilityConsistency() {
        // Test that level availability matches complexity
        val testCases = mapOf(
            1 to listOf(1, 2, 3, 4, 5), // Complexity 1 available at all levels
            2 to listOf(2, 3, 4, 5),    // Complexity 2 from level 2+
            3 to listOf(3, 4, 5),       // Complexity 3 from level 3+
            4 to listOf(4, 5),          // Complexity 4 from level 4+
            5 to listOf(5)              // Complexity 5 only at level 5
        )
        
        testCases.forEach { (complexity, expectedLevels) ->
            val actualLevels = PhoneticAnalyzer.determineLevelAvailability("TEST", complexity)
            assertEquals("Complexity $complexity should have correct level availability", 
                expectedLevels, actualLevels)
        }
    }
    
    @Test
    fun testSoundTypeClassificationConsistency() {
        // Test that sound type classification is consistent
        val testWords = listOf("CAT", "DOG", "BOY", "HAY")
        
        testWords.forEach { word ->
            val soundType = PhoneticAnalyzer.analyzeSoundType(word)
            
            // All test words should be CVC pattern
            assertEquals("$word should have CVC pattern", "CVC", soundType.pattern)
            
            // Verify consonant types are valid
            assertTrue("$word consonant1 should be valid type", 
                soundType.consonant1 in setOf("simple", "blend", "digraph", "none"))
            assertTrue("$word consonant2 should be valid type", 
                soundType.consonant2 in setOf("simple", "blend", "digraph", "none"))
            
            // Verify vowel types are valid
            assertTrue("$word vowel should be valid type", 
                soundType.vowel in setOf("short", "long", "digraph", "diphthong", "silent"))
        }
    }
    
    @Test
    fun testPhoneticComplexityRange() {
        // Test that complexity ratings are within valid range (1-5)
        val testWords = listOf("CAT", "BAT", "BOY", "COW", "BED", "HAY", "MIX", "SUN")
        
        testWords.forEach { word ->
            val complexity = PhoneticAnalyzer.analyzePhoneticComplexity(word)
            assertTrue("$word complexity should be between 1-5", complexity in 1..5)
        }
    }
    
    @Test
    fun testCaseInsensitivity() {
        // Test that analysis works consistently regardless of case
        val testCases = listOf(
            Pair("CAT", "cat"),
            Pair("DOG", "dog"), 
            Pair("BOY", "boy")
        )
        
        testCases.forEach { (uppercase, lowercase) ->
            val upperComplexity = PhoneticAnalyzer.analyzePhoneticComplexity(uppercase)
            val lowerComplexity = PhoneticAnalyzer.analyzePhoneticComplexity(lowercase)
            assertEquals("$uppercase and $lowercase should have same complexity", 
                upperComplexity, lowerComplexity)
            
            val upperTags = PhoneticAnalyzer.generateSemanticTags(uppercase)
            val lowerTags = PhoneticAnalyzer.generateSemanticTags(lowercase)
            assertEquals("$uppercase and $lowercase should have same tags", upperTags, lowerTags)
        }
    }
    
    @Test
    fun testComplexityFactorsAccuracy() {
        // Test that complexity factors are correctly identified
        val testCases = mapOf(
            "CAT" to emptyList<String>(), // Simple word should have no complexity factors
            "BOY" to listOf("complex_vowel"), // Should have complex vowel factor
            "BED" to emptyList<String>() // Should have no complexity factors (morphological complexity disabled)
        )
        
        testCases.forEach { (word, expectedFactors) ->
            val soundType = PhoneticAnalyzer.analyzeSoundType(word)
            assertEquals("$word should have correct complexity factors", 
                expectedFactors.sorted(), soundType.complexityFactors.sorted())
        }
    }
}