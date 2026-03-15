package com.gihansgamage.statmaster.utils

import com.gihansgamage.statmaster.models.DistributionType

object DistributionTableGenerator {

    /**
     * Generate Z-table for standard normal distribution
     */
    fun generateZTable(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        val header = mutableListOf("Z\\P")

        // Add column headers (0.00 to 0.09)
        for (i in 0 until 10) {
            header.add(String.format("%.2f", i / 100.0))
        }
        table.add(header)

        // Add rows (-3.9 to 3.9)
        for (zInt in -39..39) {
            val row = mutableListOf<String>()
            val zValue = zInt / 10.0
            row.add(String.format("%.1f", zValue))

            for (i in 0 until 10) {
                val z = zValue + i / 100.0
                val probability = NormalDistribution.cdf(z)
                row.add(String.format("%.4f", probability))
            }

            table.add(row)
        }

        return table
    }

    /**
     * Generate t-table for Student's t-distribution
     */
    fun generateTTable(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        val alphas = listOf(0.10, 0.05, 0.025, 0.01, 0.005)
        val dfs = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30, 40, 50, 60, 80, 100, 1000)

        // Header
        val header = mutableListOf("df")
        for (alpha in alphas) {
            header.add(String.format("%.3f", alpha))
        }
        table.add(header)

        // Rows
        for (df in dfs) {
            val row = mutableListOf<String>()
            row.add(df.toString())

            for (alpha in alphas) {
                val tValue = TDistribution.inverseCDF(1.0 - alpha, df.toDouble())
                row.add(String.format("%.4f", tValue))
            }

            table.add(row)
        }

        return table
    }

    /**
     * Generate Chi-square table
     */
    fun generateChiSquareTable(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        val alphas = listOf(0.995, 0.99, 0.975, 0.95, 0.90, 0.10, 0.05, 0.025, 0.01, 0.005)
        val dfs = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30, 40, 50, 60, 80, 100)

        // Header
        val header = mutableListOf("df")
        for (alpha in alphas) {
            header.add(String.format("%.3f", alpha))
        }
        table.add(header)

        // Rows
        for (df in dfs) {
            val row = mutableListOf<String>()
            row.add(df.toString())

            for (alpha in alphas) {
                val chiSquareValue = ChiSquareDistribution.inverseCDF(alpha, df.toDouble())
                row.add(String.format("%.4f", chiSquareValue))
            }

            table.add(row)
        }

        return table
    }

    /**
     * Generate F-table (for df1 = 1-10, df2 = 1-30, alpha = 0.05)
     */
    fun generateFTable(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        val alpha = 0.05
        val df2Values = (1..30 step 2).toList() + listOf(40, 60, 120)
        val df1Values = (1..10).toList()

        // Header row
        val header = mutableListOf("df2\\df1")
        for (df1 in df1Values) {
            header.add(df1.toString())
        }
        table.add(header)

        // Data rows
        for (df2 in df2Values) {
            val row = mutableListOf<String>()
            row.add(df2.toString())

            for (df1 in df1Values) {
                val fValue = FDistribution.inverseCDF(1.0 - alpha, df1.toDouble(), df2.toDouble())
                row.add(String.format("%.3f", fValue))
            }

            table.add(row)
        }

        return table
    }

    /**
     * Get table based on distribution type
     */
    fun getTable(distributionType: DistributionType): List<List<String>> {
        return when (distributionType) {
            DistributionType.NORMAL -> generateZTable()
            DistributionType.T -> generateTTable()
            DistributionType.CHISQUARE -> generateChiSquareTable()
            DistributionType.F -> generateFTable()
        }
    }
}