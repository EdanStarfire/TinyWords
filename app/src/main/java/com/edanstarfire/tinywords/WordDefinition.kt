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
)