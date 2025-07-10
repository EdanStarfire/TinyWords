package com.edanstarfire.tinywords

import androidx.annotation.DrawableRes

data class WordChallenge(
    val targetWord: String,
    val correctImageWord: String, // This will be the same as targetWord in this model
    @DrawableRes val correctImageRes: Int,
    val incorrectImageWord1: String,
    @DrawableRes val incorrectImageRes1: Int,
    val incorrectImageWord2: String,
    @DrawableRes val incorrectImageRes2: Int
)