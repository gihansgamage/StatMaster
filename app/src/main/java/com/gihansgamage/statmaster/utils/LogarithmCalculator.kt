package com.gihansgamage.statmaster.utils

import kotlin.math.ln as mathLn
import kotlin.math.log10 as mathLog10
import kotlin.math.log2 as mathLog2

object LogarithmCalculator {

    /** log base 2 */
    fun log2(x: Double): Double {
        require(x > 0) { "x must be > 0" }
        return mathLog2(x)
    }

    /** log base 10 (common log) */
    fun log10(x: Double): Double {
        require(x > 0) { "x must be > 0" }
        return mathLog10(x)
    }

    /** natural log (base e) */
    fun ln(x: Double): Double {
        require(x > 0) { "x must be > 0" }
        return mathLn(x)
    }

    /** log of any custom base */
    fun logBase(x: Double, base: Double): Double {
        require(x > 0)       { "x must be > 0" }
        require(base > 0)    { "base must be > 0" }
        require(base != 1.0) { "base cannot be 1" }
        return mathLn(x) / mathLn(base)
    }

    /** antilog base 10: 10^x */
    fun antilog10(x: Double) = Math.pow(10.0, x)

    /** antilog base 2: 2^x */
    fun antilog2(x: Double) = Math.pow(2.0, x)

    /** antilog natural: e^x */
    fun antilogE(x: Double) = Math.pow(Math.E, x)

    /** antilog custom base: base^x */
    fun antilogBase(x: Double, base: Double): Double {
        require(base > 0)    { "base must be > 0" }
        require(base != 1.0) { "base cannot be 1" }
        return Math.pow(base, x)
    }

    // ── Table generators ────────────────────────────────────────────────────

    fun generateLog10Table(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        val header = mutableListOf("x")
        for (j in 0..9) header.add("+0.0$j")
        table.add(header)
        for (iTen in 10..99) {
            val row = mutableListOf<String>()
            val xBase = iTen / 10.0
            row.add(String.format("%.1f", xBase))
            for (j in 0..9) {
                val x = xBase + j / 100.0
                row.add(String.format("%.4f", mathLog10(x)))
            }
            table.add(row)
        }
        return table
    }

    fun generateLnTable(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        val header = mutableListOf("x")
        for (j in 0..9) header.add("+0.0$j")
        table.add(header)
        for (iTen in 10..99) {
            val row = mutableListOf<String>()
            val xBase = iTen / 10.0
            row.add(String.format("%.1f", xBase))
            for (j in 0..9) {
                val x = xBase + j / 100.0
                row.add(String.format("%.4f", mathLn(x)))
            }
            table.add(row)
        }
        return table
    }

    fun generateLog2Table(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        table.add(listOf("x", "log₂(x)", "x", "log₂(x)", "x", "log₂(x)", "x", "log₂(x)"))
        val xValues = (1..64).toList()
        for (row in xValues.chunked(4)) {
            val tableRow = mutableListOf<String>()
            for (x in row) {
                tableRow.add(x.toString())
                tableRow.add(String.format("%.4f", mathLog2(x.toDouble())))
            }
            while (tableRow.size < 8) tableRow.add("")
            table.add(tableRow)
        }
        return table
    }
}