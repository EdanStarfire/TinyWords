package com.edanstarfire.tinywords

/**
 * Utility class for analyzing phonetic characteristics of words to support 
 * level-appropriate challenge generation and automatic classification.
 */
object PhoneticAnalyzer {
    
    // Phonetic pattern definitions
    private val consonantBlends = setOf(
        "bl", "br", "cl", "cr", "dr", "fl", "fr", "gl", "gr", "pl", "pr", "sc", "sk", "sl", "sm", 
        "sn", "sp", "st", "sw", "tr", "tw", "dw", "scr", "spl", "spr", "str", "thr"
    )
    
    private val consonantDigraphs = setOf(
        "ch", "sh", "th", "wh", "ph", "gh", "ck", "ng", "qu"
    )
    
    private val vowelDigraphs = setOf(
        "ai", "ay", "ea", "ee", "ie", "oa", "oe", "ue", "ui", "ey", "au", "aw", "ew", "ow", "ou", "oy", "oi"
    )
    
    private val shortVowels = setOf('a', 'e', 'i', 'o', 'u')
    private val longVowelPatterns = setOf("a_e", "e_e", "i_e", "o_e", "u_e") // Silent e patterns
    
    private val finalBlends = setOf(
        "nd", "nt", "st", "sk", "mp", "nk", "ng", "rt", "rd", "rk", "sp", "lk", "lt", "lp", "ct", "ft", "pt"
    )
    
    /**
     * Analyzes a word and returns its phonetic complexity rating (1-5)
     * Based on the progression levels defined in PRD_LevelingProposal.md
     */
    fun analyzePhoneticComplexity(word: String): Int {
        val lowerWord = word.lowercase()
        var complexity = 1
        
        // Level 2: Consonant blends and digraphs
        if (hasConsonantBlends(lowerWord) || hasConsonantDigraphs(lowerWord)) {
            complexity = maxOf(complexity, 2)
        }
        
        // Level 3: Complex vowel patterns
        if (hasComplexVowelPatterns(lowerWord) || hasSilentLetters(lowerWord)) {
            complexity = maxOf(complexity, 3)
        }
        
        // Level 4: Plural forms only (morphological complexity disabled for Phase 6.1)
        if (isPluralForm(lowerWord)) {
            complexity = maxOf(complexity, 4)
        }
        
        // Level 5: Multi-word concepts (compound words, etc.)
        if (isMultiWordConcept(word) || hasHighComplexity(lowerWord)) {
            complexity = maxOf(complexity, 5)
        }
        
        return complexity
    }
    
    /**
     * Generates a detailed SoundTypeClassification for the given word
     */
    fun analyzeSoundType(word: String): SoundTypeClassification {
        val lowerWord = word.lowercase()
        val parts = analyzeWordParts(lowerWord)
        
        return SoundTypeClassification(
            consonant1 = analyzeInitialConsonant(parts.initial),
            vowel = analyzeVowelSound(parts.vowel),
            consonant2 = analyzeFinalConsonant(parts.final),
            pattern = determinePhoneticPattern(lowerWord),
            hasSilentLetters = hasSilentLetters(lowerWord),
            complexityFactors = identifyComplexityFactors(lowerWord)
        )
    }
    
    /**
     * Generates semantic tags based on common word patterns and categories
     */
    fun generateSemanticTags(word: String): List<String> {
        val tags = mutableListOf<String>()
        val lowerWord = word.lowercase()
        
        // Animal tags
        when (lowerWord) {
            "cat", "dog", "bat", "rat", "pig", "cow", "fox", "bug", "bee", "fly" -> tags.add("animal")
        }
        
        // Action tags
        when (lowerWord) {
            "run", "hop", "sit", "pat", "hit", "cut", "dig", "mow", "fix", "mix" -> tags.add("action")
        }
        
        // Object tags
        when (lowerWord) {
            "hat", "mat", "cup", "pot", "box", "bag", "car", "bus", "van", "jet" -> tags.add("object")
        }
        
        // Body part tags
        when (lowerWord) {
            "leg", "arm", "eye", "ear", "lip", "hip", "toe", "jaw", "rib" -> tags.add("body")
        }
        
        // Food tags
        when (lowerWord) {
            "ham", "jam", "yam", "bun", "gum", "pie", "nut", "egg", "fig" -> tags.add("food")
        }
        
        // Nature/outdoor tags
        when (lowerWord) {
            "sun", "mud", "log", "bug", "web", "dew", "fog", "ice", "snow" -> tags.add("nature")
        }
        
        return tags
    }
    
    /**
     * Determines which levels this word is appropriate for based on its characteristics
     */
    fun determineLevelAvailability(word: String, complexity: Int): List<Int> {
        return when (complexity) {
            1 -> listOf(1, 2, 3, 4, 5) // Simple words available at all levels
            2 -> listOf(2, 3, 4, 5)    // Blends/digraphs from level 2+
            3 -> listOf(3, 4, 5)       // Complex vowels from level 3+
            4 -> listOf(4, 5)          // Plurals from level 4+
            5 -> listOf(5)             // Multi-word only at level 5
            else -> listOf(1, 2, 3, 4, 5)
        }
    }
    
    // Private helper methods
    private data class WordParts(val initial: String, val vowel: String, val final: String)
    
    private fun analyzeWordParts(word: String): WordParts {
        // Simple CVC pattern analysis - can be enhanced for complex patterns
        val vowelIndices = word.indices.filter { word[it] in shortVowels }
        
        return if (vowelIndices.isNotEmpty()) {
            val vowelIndex = vowelIndices.first()
            WordParts(
                initial = if (vowelIndex > 0) word.substring(0, vowelIndex) else "",
                vowel = word.substring(vowelIndex, vowelIndex + 1),
                final = if (vowelIndex < word.length - 1) word.substring(vowelIndex + 1) else ""
            )
        } else {
            WordParts(word, "", "") // Fallback for words without clear vowels
        }
    }
    
    private fun analyzeInitialConsonant(consonant: String): String {
        return when {
            consonant.length >= 2 && consonant in consonantBlends -> "blend"
            consonant.length >= 2 && consonant in consonantDigraphs -> "digraph"
            consonant.isEmpty() -> "none"
            else -> "simple"
        }
    }
    
    private fun analyzeVowelSound(vowel: String): String {
        return when {
            vowel.length >= 2 && vowel in vowelDigraphs -> "digraph"
            vowel in shortVowels.map { it.toString() } -> "short"
            else -> "short" // Default assumption for CVC words
        }
    }
    
    private fun analyzeFinalConsonant(consonant: String): String {
        return when {
            consonant.length >= 2 && consonant in finalBlends -> "blend"
            consonant.length >= 2 && consonant in consonantDigraphs -> "digraph"
            consonant.isEmpty() -> "none"
            else -> "simple"
        }
    }
    
    private fun determinePhoneticPattern(word: String): String {
        val vowelCount = word.count { it in shortVowels }
        val consonantCount = word.length - vowelCount
        
        return when {
            word.length == 3 && vowelCount == 1 -> "CVC"
            word.length == 4 && vowelCount == 1 -> if (hasConsonantBlends(word)) "CCVC" else "CVCC"
            word.endsWith("e") && vowelCount >= 2 -> "CVCe"
            vowelCount >= 2 -> "CVVC"
            else -> "CVC" // Default pattern
        }
    }
    
    private fun hasConsonantBlends(word: String): Boolean {
        return consonantBlends.any { word.contains(it) }
    }
    
    private fun hasConsonantDigraphs(word: String): Boolean {
        return consonantDigraphs.any { word.contains(it) }
    }
    
    private fun hasComplexVowelPatterns(word: String): Boolean {
        return vowelDigraphs.any { word.contains(it) } || 
               longVowelPatterns.any { pattern ->
                   val parts = pattern.split("_")
                   word.contains(parts[0]) && word.endsWith("e")
               }
    }
    
    private fun hasSilentLetters(word: String): Boolean {
        return word.endsWith("e") && word.length > 3 // Silent e pattern
    }
    
    private fun isPluralForm(word: String): Boolean {
        return word.endsWith("s") && word.length > 2
    }
    
    private fun isMisclassifiedAsPlural(word: String): Boolean {
        // Words that end in 'D' are sometimes incorrectly classified as plurals by simple heuristics
        // This is a known limitation of the current algorithm
        return word.endsWith("d", ignoreCase = true) && word.length == 3
    }
    
    private fun hasMorphologicalComplexity(word: String): Boolean {
        return word.contains("ing") || word.contains("ed") || word.contains("er") || word.contains("est")
    }
    
    private fun isMultiWordConcept(word: String): Boolean {
        return word.contains(" ") || word.contains("-")
    }
    
    private fun hasHighComplexity(word: String): Boolean {
        return word.length > 6 || // Long words
               (hasConsonantBlends(word) && hasComplexVowelPatterns(word)) // Multiple complex features
    }
    
    private fun identifyComplexityFactors(word: String): List<String> {
        val factors = mutableListOf<String>()
        
        if (hasConsonantBlends(word)) factors.add("consonant_blend")
        if (hasConsonantDigraphs(word)) factors.add("consonant_digraph")
        if (hasComplexVowelPatterns(word)) factors.add("complex_vowel")
        if (hasSilentLetters(word)) factors.add("silent_letters")
        if (isPluralForm(word)) factors.add("plural_form")
        if (word.length > 5) factors.add("long_word")
        
        return factors
    }
}