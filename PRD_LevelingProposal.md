# PRD: Multi-Level Complexity Progression System for TinyWords

**Document Version**: 1.0  
**Date**: 2025-08-20  
**Status**: Proposal  

## **Executive Summary**

This PRD proposes implementing a progressive difficulty system for TinyWords that moves beyond random challenge generation to structured phonetic learning levels. The system will support 5 distinct complexity levels, from simple letter variations to complex vowel patterns and multi-word concepts.

## **Problem Statement**

The current TinyWords implementation generates challenges randomly, which:
- Lacks educational progression and scaffolding
- May become non-challenging to a learner who has played regularly

## **Objectives**

### **Primary Goals**
1. Implement structured phonetic progression aligned with educational best practices
2. Provide appropriate challenge difficulty based on chosen level
3. Maintain current game performance and simplicity
4. Support future vocabulary expansion without manual challenge creation

### **Success Metrics**
- Support for 5+ distinct difficulty levels
- Scalable to 1000+ words without manual intervention

## **User Stories**

**As a young learner**, I want challenges that gradually increase in difficulty so I can build confidence and skills progressively.

**As an educator**, I want the game to follow established phonics sequences so it aligns with classroom instruction.

**As a parent**, I want to see my child's progress through different skill levels. (Not supporting this user story at this time)

## **Detailed Requirements**

### **Progression Level Definitions**

#### **Level 1: Single Letter Variation**
- **Description**: Vary one letter in the same position, maintaining phonetic simplicity
- **Example Challenge**: MOW → [MOM, MOP] (vary final sound only)
- **Phonetic Pattern**: Simple consonant-vowel-consonant (CVC) with single letter changes
- **Educational Focus**: Basic phoneme awareness

#### **Level 2: Consonant Blends & Digraphs**  
- **Description**: Introduce blended consonants (bl, cr, st) and digraphs (ch, th, sh)
- **Example Challenge**: MOW → [BLOW, SNOW] + [COW] (target vs. consonant blends)
- **Phonetic Pattern**: CCVC or CVC with consonant clusters
- **Educational Focus**: Consonant blend recognition

#### **Level 3: Complex Vowel Patterns**
- **Description**: Include vowel digraphs (ea, oa), silent letters, and multiple sound variations
- **Example Challenge**: MOW → [BOAT, COAT] + [MEAT] (multiple vowel patterns)  
- **Phonetic Pattern**: Complex vowel combinations, silent letters
- **Educational Focus**: Advanced vowel patterns

#### **Level 4: Plural Forms** (Optional)
- **Description**: Introduce simple plural transformations
- **Example Challenge**: COW → [COWS, BOWS] + [CAR] (plural vs. singular)
- **Educational Focus**: Morphological awareness

#### **Level 5: Multi-Word Concepts** (Optional)
- **Description**: Simple compound concepts or adjective-noun pairs
- **Example Challenge**: RED CAR → [RED BALL, BLUE CAR] + [GREEN DOG]
- **Educational Focus**: Semantic relationships

### **Technical Architecture**

#### **Data Schema Changes**

**Enhanced WordDefinition Structure**:
```json
{
  "targetWord": "MOW",
  "imageResName": "mow_image",
  "part1Sound": "M",
  "part2Sound": "O", 
  "part3Sound": "W",
  "phonicComplexity": 1,
  "soundType": {
    "consonant1": "simple",
    "vowel": "short",
    "consonant2": "simple"
  },
  "levelAvailability": [1, 2, 3],
  "tags": ["action", "outdoor"]
}
```

**New Fields Explained**:
- `phonicComplexity`: 1-5 rating of inherent word difficulty
- `soundType`: Detailed phonetic categorization for matching algorithms
- `levelAvailability`: Array of levels where this word can appear
- `tags`: Semantic grouping for thematic challenges

#### **WordChallengeGenerator Enhancements**

**New Core Methods**:
```kotlin
fun generateLevelChallenge(targetWord: String, difficultyLevel: Int): WordChallenge?
fun findLevelAppropriateDistractors(target: WordDefinition, level: Int): List<WordDefinition>
fun validateChallengeAppropriateForLevel(challenge: WordChallenge, level: Int): Boolean
```

**Level-Specific Algorithms**:
- Level 1: Enhanced single-letter variation with phonetic matching
- Level 2: Consonant blend vs. simple consonant variations  
- Level 3: Complex vowel pattern matching and silent letter awareness
- Level 4: Plural/singular morphological variations
- Level 5: Multi-word semantic relationship matching

### **Implementation Approach: Dynamic vs. Pre-Generated**

#### **Recommendation: Dynamic Runtime Generation**

**Rationale**:
- **Maintainability**: New words don't require manual challenge creation
- **Scalability**: Supports vocabulary growth without exponential data increase
- **Storage Efficiency**: Reduces data size by ~80% vs. pre-generated approach
- **Algorithmic Consistency**: Ensures uniform difficulty patterns
- **Adaptability**: Allows real-time difficulty adjustment

**Alternative Considered**: Pre-generated challenge lists were evaluated but rejected due to maintenance overhead and storage requirements (estimated 2500+ challenges per level for current vocabulary).

### **User Experience Changes**

#### **Challenge Selection Logic**
- Start new players at Level 1
- Allow manual level selection for learners
- Maintain current random word selection within appropriate level pool

### **Data Migration Plan**

#### **Phase 1: Schema Enhancement**
1. Update `WordDefinition` data class with new fields
2. Create migration script for existing word_definitions.json
3. Add phonetic classification utility functions
4. Implement backward compatibility for existing data

#### **Phase 2: Algorithm Implementation**  
1. Implement level-aware challenge generation
2. Add phonetic pattern matching algorithms
3. Create level-specific distractor selection logic
4. Add comprehensive unit tests for all levels

#### **Phase 3: UI Integration**
1. Add level selection and display components
2. Update settings to include level preferences

### **Testing Strategy**

#### **Automated Testing Requirements**
- Unit tests for each level's challenge generation algorithm
- Data validation tests for all phonetic classifications
- Integration tests for level progression logic

#### **Manual Testing Focus Areas**
- Smooth difficulty progression across levels
- Edge cases with limited vocabulary subsets
- User experience flow for level changes

### **Risk Assessment**

#### **High Priority Risks**
- **Data Migration**: Existing word definitions must be accurately classified

#### **Mitigation Strategies**
- Create comprehensive challenge validation tests

### **Future Considerations**

#### **Post-MVP Improvements**
- Add common problem words tracking, viewing and logic

#### **Scalability Planning**
- Architecture supports expansion to 1000+ words
- Algorithm framework can accommodate additional levels (6+)
- Data schema extensible for additional phonetic properties
- Challenge generation optimizable through caching strategies

### **Success Criteria**

#### **MVP Requirements**
- [ ] 5 distinct difficulty levels implemented
- [ ] Dynamic challenge generation for all levels
- [ ] Smooth progression between levels
- [ ] Maintain current game performance
- [ ] All existing words classified and available

#### **Quality Gates**
- [ ] Zero regression in existing functionality

### **Timeline Estimate**

- **Phase 1 (Schema & Migration)**: 2-3 weeks
- **Phase 2 (Algorithm Implementation)**: 3-4 weeks  
- **Phase 3 (UI Integration)**: 2-3 weeks
- **Testing & Refinement**: 2-3 weeks
- **Total Estimated Duration**: 9-13 weeks

### **Dependencies**

- UI/UX design for level progression interface
- Comprehensive word phonetic classification dataset

---

This proposal provides a foundation for transforming TinyWords from a random challenge generator into a structured educational progression system while maintaining the game's core simplicity and performance characteristics.