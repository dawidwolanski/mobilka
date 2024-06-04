package com.example.apka

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProgressActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSentence()
    }

    private fun getLevel(): Int {
        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val v = sharedPreferences.getInt("level", 1)
        return v
    }

    private fun setSentence() {
        val t: TextView = findViewById(R.id.sentence)
        t.text = "Current level: ${getLevel()}, gg!"
    }
}