package com.example.apka

import android.widget.Button

data class GameState (
    var pairsRevealed: Int = 0,
    var isEnded: Boolean = false,
    var firstTile: Button? = null,
    var secondTile: Button? = null,
    var isFirstTileBeingClicked: Boolean = true,
    var canClickBePerformed: Boolean = true
)