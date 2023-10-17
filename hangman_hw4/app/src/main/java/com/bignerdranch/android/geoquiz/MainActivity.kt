package com.bignerdranch.android.geoquiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize New Game button
        val newGameButton: Button = findViewById(R.id.buttonNewGame)
        newGameButton.setOnClickListener {
            startNewGame()
        }
    }

    private fun startNewGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }
}