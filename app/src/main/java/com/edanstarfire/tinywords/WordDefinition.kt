package com.edanstarfire.tinywords

import kotlinx.serialization.Serializable

@Serializable
data class WordDefinition(
    val targetWord: String,
    val imageResName: String, // e.g., "cat_image"
    val part1Sound: String,
    val part2Sound: String,
    val part3Sound: String,
    // You could add part4_sound, part5_sound for longer words,
    // or make 'parts' a list: val parts: List<String>
    // For CVC words, 3 parts are fine.
    
    // Phase 6.1: Enhanced fields for multi-level complexity progression
    /**
     * Phonetic complexity rating from 1-5:
     * 1 = Simple CVC single letter variations
     * 2 = Consonant blends & digraphs  
     * 3 = Complex vowel patterns
     * 4 = Plural forms & morphology
     * 5 = Multi-word concepts
     */
    val phonicComplexity: Int = 1,
    
    /**
     * Detailed phonetic classification for algorithmic matching
     */
    val soundType: SoundTypeClassification = SoundTypeClassification(),
    
    /**
     * List of difficulty levels where this word can appear (1-5)
     * Empty list means available at all levels
     */
    val levelAvailability: List<Int> = listOf(1, 2, 3, 4, 5),
    
    /**
     * Semantic tags for thematic grouping: e.g., ["animal", "outdoor", "action"]
     */
    val tags: List<String> = emptyList()
)