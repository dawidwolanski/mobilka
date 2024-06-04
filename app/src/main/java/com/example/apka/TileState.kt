package com.example.apka

data class TileState (
    val word: String,
    val wordId: Int,
    var isReversed: Boolean,
    var isClickable: Boolean,
    val isWordPl: Boolean
)