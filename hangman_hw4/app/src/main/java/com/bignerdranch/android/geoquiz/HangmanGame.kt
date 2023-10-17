package com.bignerdranch.android.geoquiz

import androidx.annotation.StringRes
import java.util.*

class HangmanGame(private val words: Array<String>) {

    private var wordToGuess: String = ""
    private var guessedLetters: MutableList<Char> = mutableListOf()
    private var incorrectAttempts: Int = 0
    private var gameState: GameState = GameState.IN_PROGRESS


    fun startNewGame() {
        wordToGuess = getRandomWord()
        guessedLetters.clear()
        incorrectAttempts = 0
        gameState = GameState.IN_PROGRESS
    }

    private fun getRandomWord(): String {
        return words[Random().nextInt(words.size)]
    }

    fun getWordToDisplay(): String {
        return wordToGuess.map { if (guessedLetters.contains(it)) it else '_' }.joinToString(" ")
    }

    private fun isWordGuessed(): Boolean {
        return wordToGuess.all { guessedLetters.contains(it) }
    }

    fun guessLetter(letter: Char): Boolean {
        if (guessedLetters.contains(letter)) return false

        guessedLetters.add(letter)

        if (!wordToGuess.contains(letter)) {
            incorrectAttempts++
            if (incorrectAttempts >= MAX_INCORRECT_ATTEMPTS) {
                gameState = GameState.LOST
            }
            return false
        } else if (isWordGuessed()) {
            gameState = GameState.WON
        }

        return true
    }

    fun getGameState(): GameState {
        return gameState
    }

    fun getIncorrectAttempts(): Int {
        return incorrectAttempts
    }

    fun isGameOver(): Boolean {
        return incorrectAttempts >= MAX_INCORRECT_ATTEMPTS || wordToGuess.all { guessedLetters.contains(it) }
    }

    fun getHangmanImageResource(): Int {
        val incorrectAttempts = getIncorrectAttempts()
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

    fun getRemainingLetters(): List<Char> {
        val remainingLetters = mutableListOf<Char>()
        for (letter in 'A'..'Z') {
            if (!guessedLetters.contains(letter)) {
                remainingLetters.add(letter)
            }
        }
        return remainingLetters
    }

    // Function to deduct a turn (increment incorrectAttempts)
    fun deductTurn() {
        incorrectAttempts++
    }

    companion object {
        const val MAX_INCORRECT_ATTEMPTS = 6
    }
}

enum class GameState {
    IN_PROGRESS,
    WON,
    LOST
}