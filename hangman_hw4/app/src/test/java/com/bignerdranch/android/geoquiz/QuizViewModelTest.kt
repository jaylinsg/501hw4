package com.bignerdranch.android.geoquiz

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Test

class QuizViewModelTest {
    @Test
    fun initiallyProvidesFirstQuestionText() {
        val savedStateHandle = SavedStateHandle()
        val gameOverActivity = GameOverActivity(savedStateHandle)
        assertEquals(R.string.question_australia, gameOverActivity.currentQuestionText)
    }

    @Test
    fun wrapsAroundQuestionBank() {
        val savedStateHandle = SavedStateHandle(mapOf(CURRENT_INDEX_KEY to 5))
        val gameOverActivity = GameOverActivity(savedStateHandle)
        assertEquals(R.string.question_asia, gameOverActivity.currentQuestionText)
        gameOverActivity.moveToNext()
        assertEquals(R.string.question_australia, gameOverActivity.currentQuestionText)
    }
}
