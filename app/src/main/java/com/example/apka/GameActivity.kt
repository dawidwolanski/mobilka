package com.example.apka

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentContainerView
import com.release.gfg1.DBHandler
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.max

class GameActivity : AppCompatActivity() {
    private val tileHeight = 80
    private val tileWidth = 130
    private var level = 1
    private var columns = 3
    private var rows = 6
    private val wordPairs = columns * rows / 2
    private val db: DBHandler = DBHandler(this, null)
    private var assignedWords: LinkedHashMap<Int, Triple<Int, String, Boolean>> = LinkedHashMap()
    private val tileStates: LinkedHashMap<Int, TileState> = LinkedHashMap()
    private var tiles = ArrayList<Button>()
    private var gameState = GameState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val orientation = this.resources.configuration.orientation
        level = getLevel()


        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columns = rows.apply { rows = columns }
        }

        launchGame(columns, rows, level)

        findViewById<Button>(R.id.continueButton).setOnClickListener{
            gameWonContinue()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        rearrangeGrid()
    }

    private fun rearrangeGrid() {
        val grid = findViewById<GridLayout>(R.id.gameGrid)
        grid.removeAllViews()
        val originalColumnCount = grid.columnCount
        val originalRowCount = grid.rowCount

        grid.rowCount = originalColumnCount
        grid.columnCount = originalRowCount
        Log.d("REARRANGATION", "${grid.columnCount} | ${grid.rowCount}")

        tiles.forEachIndexed { index, tile ->
            tile.layoutParams = GridLayout.LayoutParams().apply {
                width = tileWidth.dpToPx()
                height = tileHeight.dpToPx()
            }
        }

        tiles.forEach { tile ->
            grid.addView(tile)
        }

    }

    private fun createTiles(rows: Int, columns: Int): ArrayList<Button> {
        val tiles = ArrayList<Button>()


        for (i in 0 until rows*columns) {
            val button = Button(this).apply {
                id = View.generateViewId()
                isAllCaps = false
                text = getString(R.string.tileFrontText)
                textSize = 6.dpToPx().toFloat()
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.ITALIC)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = tileWidth.dpToPx()
                    height = tileHeight.dpToPx()
                }
                backgroundTintList = getColorStateList(R.color.theme_main)
            }

            button.setOnClickListener {
                tileClicked(button.id, button)
            }

            tiles.add(button)
        }
        return tiles
    }

    private fun launchGame(columns: Int, rows: Int, level: Int) {
        val grid = findViewById<GridLayout>(R.id.gameGrid)
        grid.rowCount = rows
        grid.columnCount = columns

        tiles = createTiles(rows, columns)
        insertTiles(tiles)

        val words = db.getWords(level, wordPairs)
        assignedWords = assingBtnIdToWords(words)

        for (tile in tiles) {
            val wordId = assignedWords[tile.id]!!.first
            val word = assignedWords[tile.id]!!.second
            val isWordPl = assignedWords[tile.id]!!.third
            tileStates[tile.id] = TileState(word, wordId, false,true, isWordPl)
        }
    }

    private fun tileClicked(tileId: Int, tile: Button) {
        val tileState = tileStates[tileId]

        if (!tileState!!.isClickable || !gameState.canClickBePerformed || gameState.isEnded) return

        reverseTile(tile)

        if (gameState.isFirstTileBeingClicked) {
            tileState.isClickable = false
            gameState.firstTile = tile
            gameState.isFirstTileBeingClicked = false
        } else {
            gameState.secondTile = tile
            gameState.isFirstTileBeingClicked = true
            gameState.canClickBePerformed = false

            val firstState = tileStates[gameState.firstTile!!.id]
            val secondState = tileStates[gameState.secondTile!!.id]

            if (firstState!!.wordId == secondState!!.wordId) {
                gameState.isEnded = ++gameState.pairsRevealed >= wordPairs
                firstState.isClickable = false
                secondState.isClickable = false
                gameState.canClickBePerformed = true

                if (gameState.isEnded) endGame()

            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    reverseTile(gameState.firstTile!!)
                    reverseTile(tile)
                    firstState.isClickable = true
                    secondState.isClickable = true
                    gameState.canClickBePerformed = true
                }, 1000)
            }
        }
    }

    private fun endGame() {
        val newLvl = if (level + 1 > 10) 10 else level + 1
        setLevel(newLvl)
        showEndGameScreen()
    }

    private fun reverseTile(tile: Button) {
        val tileState = tileStates[tile.id]
        tileState!!.isReversed = !tileState.isReversed

        if (!tileState!!.isReversed) {
            tile.text = getString(R.string.tileFrontText)
            tile.setTypeface(null, Typeface.ITALIC)
            tile.backgroundTintList = getColorStateList(R.color.theme_main)
            return
        }

        tile.text = tileState.word
        tile.setTypeface(null, Typeface.NORMAL)

        if (tileState.isWordPl)
            tile.backgroundTintList = getColorStateList(R.color.tile_back_pl)
        else
            tile.backgroundTintList = getColorStateList(R.color.tile_back_en)
    }

    private fun insertTiles(tiles: ArrayList<Button>) {
        val gridLayout = findViewById<GridLayout>(R.id.gameGrid)
        for (tile in  tiles) {
            gridLayout.addView(tile)
        }
    }

    private fun assingBtnIdToWords(words: ArrayList<Word>): LinkedHashMap<Int, Triple<Int, String, Boolean>> {
        val ret = LinkedHashMap<Int, Triple<Int, String, Boolean>>()
        val s = ArrayList<Triple<Int, String, Boolean>>()

        for (word in words) {
            s.add(Triple(word.id, word.wordEn, false))
            s.add(Triple(word.id, word.wordPl, true))
        }

        s.shuffle()

        var i = 0
        for (tile in tiles) {
            ret[tile.id] = s[i++]
        }

        return ret
    }

    private fun showEndGameScreen() {
        findViewById<ConstraintLayout>(R.id.endingScreen).visibility = View.VISIBLE
    }

    private fun gameWonContinue() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun getLevel(): Int {
        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val v = sharedPreferences.getInt("level", 1)
        return v
    }

    private fun setLevel(lvl: Int) {
        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("level", lvl)
        editor.apply()
    }
}
