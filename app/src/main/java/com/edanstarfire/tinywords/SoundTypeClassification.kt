package com.edanstarfire.tinywords

import kotlinx.serialization.Serializable

/**
 * Represents the phonetic classification of sound components within a word.
 * Used for level-appropriate challenge generation and phonetic matching algorithms.
 */
@Serializable
data class SoundTypeClassification(
    /**
     * Type of first consonant sound: "simple" (single consonant), "blend" (br, cl, st), 
     * "digraph" (ch, th, sh), or "silent" (silent letters)
     */
    val consonant1: String = "simple",
    
    /**
     * Type of vowel sound: "short" (cat, bet), "long" (cake, beet), "digraph" (boat, meat),
     * "diphthong" (boy, cow), or "silent" (silent e)
     */
    val vowel: String = "short",
    
    /**
     * Type of final consonant sound: "simple" (single consonant), "blend" (nd, st, mp),
     * "digraph" (ch, th, ng), or "silent" (silent letters)
     */
    val consonant2: String = "simple",
    
    /**
     * Overall phonetic pattern: "CVC" (consonant-vowel-consonant), "CCVC" (blend-vowel-consonant),
     * "CVCC" (consonant-vowel-blend), "CVCe" (silent e pattern), etc.
     */
    val pattern: String = "CVC",
    
    /**
     * Indicates if this word contains any silent letters that affect pronunciation
     */
    val hasSilentLetters: Boolean = false,
    
    /**
     * Phonetic complexity factors present: e.g., ["consonant_blend", "long_vowel", "silent_e"]
     */
    val complexityFactors: List<String> = emptyList()
)