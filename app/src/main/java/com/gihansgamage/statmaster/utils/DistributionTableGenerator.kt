package com.gihansgamage.statmaster.utils

import com.gihansgamage.statmaster.models.DistributionType
import kotlin.math.log10
import kotlin.math.ln

object DistributionTableGenerator {

    fun generateZTable(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        val header = mutableListOf("Z")
        for (j in 0..9) header.add("+0.0$j")
        table.add(header)
        for (zTen in -34..34) {
            val row = mutableListOf<String>()
            val zBase = zTen / 10.0
            row.add(String.format("%.1f", zBase))
            for (j in 0..9) {
                val z = zBase + j / 100.0
                row.add(String.format("%.4f", NormalDistribution.cdf(z)))
            }
            table.add(row)
        }
        return table
    }

    fun generateTTable(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        val alphas = listOf(0.10, 0.05, 0.025, 0.01, 0.005)
        table.add(listOf("df", "α=0.10", "α=0.05", "α=0.025", "α=0.01", "α=0.005"))
        table.add(listOf("", "2-tail 0.20", "2-tail 0.10", "2-tail 0.05", "2-tail 0.02", "2-tail 0.01"))
        val dfs = (1..30).toList() + listOf(40, 60, 120)
        for (df in dfs) {
            val row = mutableListOf(df.toString())
            for (alpha in alphas) {
                val value = try {
                    TDistribution.inverseCDF(1.0 - alpha, df.toDouble())
                } catch (e: Exception) { Double.NaN }
                row.add(if (value.isNaN() || value.isInfinite()) "—" else String.format("%.4f", value))
            }
            table.add(row)
        }
        val infRow = mutableListOf("∞")
        for (alpha in alphas) infRow.add(String.format("%.4f", NormalDistribution.inverseCDF(1.0 - alpha)))
        table.add(infRow)
        return table
    }

    fun generateChiSquareTable(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        // alpha = upper-tail probability (area to the right)
        val alphas = listOf(0.995, 0.99, 0.975, 0.95, 0.90, 0.10, 0.05, 0.025, 0.01, 0.005)
        val header = mutableListOf("df")
        for (a in alphas) header.add("α=${String.format("%.3f", a)}")
        table.add(header)

        val dfs = (1..30).toList() + listOf(40, 50, 60, 80, 100)
        for (df in dfs) {
            val row = mutableListOf(df.toString())
            for (alpha in alphas) {
                // inverseCDF takes cumulative probability = 1 - upper-tail alpha
                val p = 1.0 - alpha
                val value = try {
                    val v = ChiSquareDistribution.inverseCDF(p, df.toDouble())
                    if (v.isNaN() || v.isInfinite() || v < 0) null else v
                } catch (e: Exception) { null }
                row.add(if (value == null) "—" else String.format("%.3f", value))
            }
            table.add(row)
        }
        return table
    }

    fun generateFTable(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        val df1Values = (1..10).toList()
        val df2Values = (1..30).toList() + listOf(40, 60, 120)
        table.add(listOf("F-table α=0.05") + List(df1Values.size - 1) { "" })
        val header = mutableListOf("df₂ \\ df₁")
        for (df1 in df1Values) header.add(df1.toString())
        table.add(header)
        for (df2 in df2Values) {
            val row = mutableListOf(df2.toString())
            for (df1 in df1Values) {
                val value = try {
                    val v = FDistribution.inverseCDF(0.95, df1.toDouble(), df2.toDouble())
                    if (v.isNaN() || v.isInfinite() || v < 0) null else v
                } catch (e: Exception) { null }
                row.add(if (value == null) "—" else String.format("%.3f", value))
            }
            table.add(row)
        }
        return table
    }

    // ── Log tables ────────────────────────────────────────────────────────────

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
                row.add(String.format("%.4f", log10(x)))
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
                row.add(String.format("%.4f", ln(x)))
            }
            table.add(row)
        }
        return table
    }

    fun getTable(distributionType: DistributionType): List<List<String>> = when (distributionType) {
        DistributionType.NORMAL    -> generateZTable()
        DistributionType.T         -> generateTTable()
        DistributionType.CHISQUARE -> generateChiSquareTable()
        DistributionType.F         -> generateFTable()
    }
}