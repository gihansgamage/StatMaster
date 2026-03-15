package com.gihansgamage.statmaster.utils

import com.gihansgamage.statmaster.models.DistributionType

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
            for (alpha in alphas) row.add(String.format("%.4f", TDistribution.inverseCDF(1.0 - alpha, df.toDouble())))
            table.add(row)
        }
        val infRow = mutableListOf("∞")
        for (alpha in alphas) infRow.add(String.format("%.4f", NormalDistribution.inverseCDF(1.0 - alpha)))
        table.add(infRow)
        return table
    }

    fun generateChiSquareTable(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        val alphas = listOf(0.995, 0.99, 0.975, 0.95, 0.90, 0.10, 0.05, 0.025, 0.01, 0.005)
        val header = mutableListOf("df")
        for (a in alphas) header.add("α=${String.format("%.3f", a)}")
        table.add(header)
        val dfs = (1..30).toList() + listOf(40, 50, 60, 80, 100)
        for (df in dfs) {
            val row = mutableListOf(df.toString())
            for (alpha in alphas) row.add(String.format("%.3f", ChiSquareDistribution.inverseCDF(1.0 - alpha, df.toDouble())))
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
            for (df1 in df1Values) row.add(String.format("%.3f", FDistribution.inverseCDF(0.95, df1.toDouble(), df2.toDouble())))
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