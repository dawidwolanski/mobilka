package com.example.apka

import android.R.attr.text
import android.R.attr.value
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.release.gfg1.DBHandler
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


class MainActivity : AppCompatActivity() {
    private val db: DBHandler = DBHandler(this, null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("dbLoaded", false)
        editor.apply()
        initDb()

        Handler(Looper.getMainLooper()).postDelayed({
            setContentView(R.layout.activity_main)
            init()
        }, 1000)
    }

    private fun init() {
        setSentence()
        val startBtn: MaterialButton = findViewById(R.id.startButton)
        val progressBtn: Button = findViewById(R.id.progressBtn)

        startBtn.setOnClickListener() {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        progressBtn.setOnClickListener() {
            val intent = Intent(this, ProgressActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateSentenceTextView(word: String, response: String) {
        val textView = findViewById<TextView>(R.id.sentence)
        textView.text = "$word - $response"
    }

    private fun initDb() {
        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val isLoaded = sharedPreferences.getBoolean("dbLoaded", false)

        if (!isLoaded) {
            val editor = sharedPreferences.edit()
            editor.putBoolean("dbLoaded", true)
            editor.apply()

            dbLoadDefault()
        }
    }

    private fun setSentence() {
        val word = db.getRandomWord()
        val apiUrl = getString(R.string.dictionary_api, word)

        try {
            Thread {
                val url : URL = URI.create(apiUrl).toURL()
                val connection : HttpURLConnection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"

                val responseCode: Int = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read and print the response data
                    val reader : BufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                    var line: String?
                    val response = StringBuilder()

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    reader.close()

                    val jsonResponse = JSONArray(response.toString())
                    if (jsonResponse.length() > 0) {
                        val firstObject = jsonResponse.getJSONObject(0)
                        val meanings = firstObject.getJSONArray("meanings")
                        if (meanings.length() > 0) {
                            val firstMeaning = meanings.getJSONObject(0)
                            val definitions = firstMeaning.getJSONArray("definitions")
                            if (definitions.length() > 0) {
                                val firstDefinition = definitions.getJSONObject(0).getString("definition")
                                runOnUiThread { updateSentenceTextView(word, firstDefinition) }
                            }
                        }
                    }
                }

                connection.disconnect()
            }.start()

        } catch (e: Exception) {
            Log.e("blad", "blad requeasta")
            e.printStackTrace()
        }
    }

    private fun readCSVFromResources(context: Context, resId: Int): List<String> {
        val inputStream = context.resources.openRawResource(resId)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val lines = mutableListOf<String>()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            line?.let { lines.add(it) }
        }
        bufferedReader.close()
        return lines
    }

    private fun dbLoadDefault() {
        val csv = readCSVFromResources(this, R.raw.data)

        csv.forEach { line ->
            val tokens = line.split(",")
            db.insert(tokens[1], tokens[2], tokens[3].toInt())
        }
    }


}