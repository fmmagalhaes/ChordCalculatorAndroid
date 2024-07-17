package com.fmmagalhaes.chordcalculator

import com.darkempire78.opencalculator.Calculator
import com.darkempire78.opencalculator.Expression
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.text.DecimalFormatSymbols

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExpressionUnitTest {

    @Test
    fun addition_isCorrect() {
        var result = calculate("C+1")
        assertEquals("C#", result)

        result = calculate("C+12")
        assertEquals("C", result)

        result = calculate("C#DbGBm7+2")
        assertEquals("Eb Eb A C#m7", result)
    }

    @Test
    fun subtraction_isCorrect() {
        var result = calculate("C-1")
        assertEquals("B", result)

        result = calculate("C-12")
        assertEquals("C", result)

        result = calculate("C#DbGBm7-2")
        assertEquals("B B F Am7", result)
    }

    @Test
    fun chordFromNotes_isCorrect() {
        var result = calculate("CEG")
        assertEquals("C", result)

        result = calculate("CEGBb")
        assertEquals("C7", result)

        result = calculate("CEGBbA")
        assertEquals("Am7b9/C", result)
    }

    @Test
    fun chordComposition_isCorrect() {
        var result = calculate("C")
        assertEquals("C E G", result)

        result = calculate("C7")
        assertEquals("C E G Bb", result)

        result = calculate("Am7b9/C")
        assertEquals("C A E G Bb", result)
    }

    private fun calculate(input: String) = calculator.evaluateChordsExpression(input)

    companion object {
        private lateinit var expression: Expression
        private lateinit var calculator: Calculator

        @BeforeClass
        @JvmStatic fun setup() {
            expression = Expression()
            calculator = Calculator(10)
        }
    }
}
