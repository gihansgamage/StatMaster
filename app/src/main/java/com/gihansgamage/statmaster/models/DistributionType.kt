package com.gihansgamage.statmaster.models

enum class DistributionType(val displayName: String, val tableName: String, val useCase: String) {
    NORMAL(
        displayName = "Normal Distribution",
        tableName = "Z-table",
        useCase = "Probability, hypothesis testing"
    ),
    T(
        displayName = "Student's t Distribution",
        tableName = "t-table",
        useCase = "Small sample tests"
    ),
    CHISQUARE(
        displayName = "Chi-square Distribution",
        tableName = "χ²-table",
        useCase = "Goodness of fit, independence"
    ),
    F(
        displayName = "F Distribution",
        tableName = "F-table",
        useCase = "ANOVA, variance comparison"
    )
}