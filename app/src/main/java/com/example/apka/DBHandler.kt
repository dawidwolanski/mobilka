package com.release.gfg1

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.apka.R
import com.example.apka.Word
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader

class DBHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                WORD_PL_COL + " TEXT," +
                WORD_EN_COL + " TEXT," +
                LEVEL_COL + " INTEGER  " + ")")

        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun insert(pl : String, en : String, level: Int ){
        val values = ContentValues()

        values.put(WORD_EN_COL, en)
        values.put(WORD_PL_COL, pl)
        values.put(LEVEL_COL, level)

        val db = this.writableDatabase

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    @SuppressLint("Recycle")
    fun getWords(lvl: Int, amount: Int): ArrayList<Word> {
        val db = this.readableDatabase
        val c = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY ABS($lvl - $LEVEL_COL) LIMIT $amount", null)

        val list = ArrayList<Word>()
        while (c.moveToNext()) {
            val id = c.getInt(c.getColumnIndexOrThrow(ID_COL));
            val pl = c.getString(c.getColumnIndexOrThrow(WORD_PL_COL));
            val en = c.getString(c.getColumnIndexOrThrow(WORD_EN_COL));
            val level = c.getInt(c.getColumnIndexOrThrow(LEVEL_COL));
            list.add(Word(id, pl, en, level))
        }
        return list
    }

    @SuppressLint("Recycle")
    fun getRandomWord(): String {
        val db = this.readableDatabase
        val c = db.rawQuery("SELECT $WORD_EN_COL FROM $TABLE_NAME ORDER BY RANDOM() LIMIT 1", null)
        c.moveToNext()
        return c.getString(c.getColumnIndexOrThrow((WORD_EN_COL)))
    }



    companion object{
        private val DATABASE_NAME = "LANGAPP"
        private val DATABASE_VERSION = 1

        val TABLE_NAME = "words"
        val ID_COL = "id"
        val WORD_PL_COL = "word_pl"
        val WORD_EN_COL = "word_en"
        val LEVEL_COL = "level"
    }
}
