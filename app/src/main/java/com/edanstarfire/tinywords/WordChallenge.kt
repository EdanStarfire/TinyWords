package com.edanstarfire.tinywords

import androidx.annotation.DrawableRes

data class WordChallenge(
    val targetWord: String,
    val correctImageWord: String,
    @DrawableRes val correctImageRes: Int,
    val incorrectImageWord1: String,
    @DrawableRes val incorrectImageRes1: Int,
    val incorrectImageWord2: String,
    @DrawableRes val incorrectImageRes2: Int,
    val choiceOrder: List<Int> // order of [0,1,2] for which (correct, incorrect1, incorrect2)
)