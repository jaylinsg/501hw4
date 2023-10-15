package com.bignerdranch.android.geoquiz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AlertDialog

class GameActivity : AppCompatActivity() {

    private lateinit var hangmanGame: HangmanGame
    private lateinit var wordToGuessTextView: TextView
    private lateinit var hangmanImageView: ImageView
    private lateinit var hangmanImageViewLand: ImageView


    private val words = arrayOf("HANGMAN", "COMPUTE", "ANDROID", "STUDIO", "IPHONE", "HOMEWORK", "COLLEGE")

    private lateinit var letterButtons: Array<Button>

    private val letterButtonIds = arrayOf(
        R.id.buttonA, R.id.buttonB, R.id.buttonC, R.id.buttonD, R.id.buttonE, R.id.buttonF, R.id.buttonG,
        R.id.buttonH, R.id.buttonI, R.id.buttonJ, R.id.buttonK, R.id.buttonL, R.id.buttonM, R.id.buttonN,
        R.id.buttonO, R.id.buttonP, R.id.buttonQ, R.id.buttonR, R.id.buttonS, R.id.buttonT, R.id.buttonU,
        R.id.buttonV, R.id.buttonW, R.id.buttonX, R.id.buttonY, R.id.buttonZ
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        hangmanGame = HangmanGame(words)
        hangmanGame.startNewGame()

        val gridLayoutLetters: GridLayout = findViewById(R.id.gridLayoutLetters)
        initLetterButtons(gridLayoutLetters)

        wordToGuessTextView = findViewById(R.id.textViewWordToGuess)
        hangmanImageView = findViewById(R.id.imageViewHangman)
        hangmanImageViewLand = findViewById(R.id.imageViewHangmanLand)


        val newGameButton: Button = findViewById(R.id.buttonNewGame)
        newGameButton.setOnClickListener {
            hangmanGame.startNewGame()
            for (button in letterButtons) {
                button.isEnabled = true
            }
            updateUI()
        }
    }

    private fun initLetterButtons(gridLayout: GridLayout) {
        letterButtons = Array(letterButtonIds.size) { index ->
            findViewById<Button>(letterButtonIds[index]).apply {
                setOnClickListener { onLetterButtonClick(this) }
            }
        }

        for (button in letterButtons) {
            // Remove the button from its parent if it has one
            (button.parent as? ViewGroup)?.removeView(button)
            gridLayout.addView(button)
        }
    }

    fun onLetterButtonClick(button: Button) {
        val letter = button.text[0]
        val isCorrect = hangmanGame.guessLetter(letter)
        button.isEnabled = false

        // Update the UI based on the game state
        updateUI()

        if (isCorrect) {
            // Correct guess, but the game is not over
            if (!hangmanGame.isGameOver()) {
                // Update hangman image
                hangmanImageView.setImageResource(hangmanGame.getHangmanImageResource())
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    hangmanImageViewLand.setImageResource(hangmanGame.getHangmanImageResource())
                }
            }
        }

        if (hangmanGame.isGameOver()) {
            handleGameOver()
        }
    }

    private fun handleGameOver() {
        for (button in letterButtons) {
            button?.isEnabled = false
        }

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)

        if (hangmanGame.isGameOver()) {
            builder.setTitle(getString(R.string.game_over))

            if (hangmanGame.getIncorrectAttempts() >= HangmanGame.MAX_INCORRECT_ATTEMPTS) {
                builder.setMessage(getString(R.string.lost))
            } else {
                builder.setMessage(getString(R.string.won))
            }
        }

        builder.setPositiveButton(getString(R.string.new_game)) { _, _ ->
            hangmanGame.startNewGame()
            for (button in letterButtons) {
                button?.isEnabled = true
            }
            updateUI()
        }

        val dialog = builder.create()
        dialog.show()

        // Show game over message and new game button
        val gameOverTextView: TextView = findViewById(R.id.textViewGameOver)
        val newGameButton: Button = findViewById(R.id.buttonNewGame)

        gameOverTextView.visibility = View.GONE
        newGameButton.visibility = View.GONE

        // Show the dialog
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setCancelable(false)

        builder.show()
    }

    private fun updateUI() {
        // Update word to guess display
        wordToGuessTextView.text = hangmanGame.getWordToDisplay()
        // wordToGuessTextViewLand.text = hangmanGame.getWordToDisplay()

        // Update hangman image
        hangmanImageView.setImageResource(hangmanGame.getHangmanImageResource())
        hangmanImageViewLand.setImageResource(hangmanGame.getHangmanImageResource())

        // Update hangman image for landscape mode
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hangmanImageViewLand.setImageResource(hangmanGame.getHangmanImageResource())
        }

        // Update game over message and new game button visibility
        if (hangmanGame.isGameOver()) {
            handleGameOver()
        }
    }

    private fun getHangmanImageResource(): Int {
        val incorrectAttempts = hangmanGame.getIncorrectAttempts()
        return when (incorrectAttempts) {
            1 -> R.drawable.hangman_1
            2 -> R.drawable.hangman_2
            3 -> R.drawable.hangman_3
            4 -> R.drawable.hangman_4
            5 -> R.drawable.hangman_5
            6 -> R.drawable.hangman_6
            else -> R.drawable.hangman_0
        }
    }
}

