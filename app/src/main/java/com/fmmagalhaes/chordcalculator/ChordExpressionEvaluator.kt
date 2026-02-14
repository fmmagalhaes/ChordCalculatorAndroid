package com.fmmagalhaes.chordcalculator

import ChordCalculator.ChordCalculator

class ChordExpressionEvaluator {
    fun evaluateExpression(equation: String): String {
        val calc = ChordCalculator()
        val noteMatch = "[CDEFGAB][#b]?" // e.g. Eb
        val chordMatch = "$noteMatch[/a-zM#0-9]*(/$noteMatch)*( *)"  // e.g. Ebmaj7b9/C#, C7/9
        val chordsMatch = "($chordMatch)+" // multiple chords

        return when {
            equation.matches(Regex("$chordsMatch\\+( *)[0-9]+")) -> handleChordSum(equation, calc)
            equation.matches(Regex("$chordsMatch-( *)[0-9]+")) -> handleChordSubtraction(equation, calc)
            equation.matches(Regex("($noteMatch( *)){2,}")) -> handleChordFromNotes(equation, calc)
            equation.matches(Regex(chordMatch)) -> handleChordComposition(equation, calc)
            else -> equation
        }
    }

    private fun handleChordSum(equation: String, calc: ChordCalculator): String {
        val parts = equation.split("\\+".toRegex()).map { it.trim() }
        val chords = splitChords(parts[0])
        val n = parts[1].toInt()

        println("[Chords] Handling sum $chords + $n")

        return chords.joinToString(" ") {
            chord -> calc.addSemitones(chord.replace(" ", ""), n)
        }
    }

    private fun handleChordSubtraction(equation: String, calc: ChordCalculator): String {
        val parts = equation.split("-".toRegex()).map { it.trim() }
        val chords = splitChords(parts[0])
        val n = parts[1].toInt()

        println("[Chords] Handling subtraction $chords - $n")

        return chords.joinToString(" ") {
            chord -> calc.subtractSemitones(chord.replace(" ", ""), n)
        }
    }

    private fun handleChordFromNotes(equation: String, calc: ChordCalculator): String {
        val notes = splitChords(equation)

        println("[Chords] Handling chord from notes $notes")

        val chords = calc.getChord(notes)
        return if (chords.isNotEmpty()) chords[0] else equation
    }

    private fun handleChordComposition(equation: String, calc: ChordCalculator): String {
        val chord = equation.trim()

        println("[Chords] Handling notes from chord $chord")

        val notes = calc.getNotes(chord)
        return notes.joinToString(" ")
    }

    private fun splitChords(chords: String): List<String> {
        // Split C#m/GDAm7 in [C#m/G, D, Am7]
        return Regex("(?<!/)(?=[CDEFGAB])").split(chords).filter { it.isNotEmpty() }
    }
}