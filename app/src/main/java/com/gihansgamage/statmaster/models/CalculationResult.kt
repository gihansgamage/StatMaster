package com.gihansgamage.statmaster.models

data class CalculationResult(
    val distributionType: DistributionType,
    val inputParameters: Map<String, Double>,
    val results: Map<String, Double>,
    val graphData: GraphData? = null
)

data class GraphData(
    val xValues: List<Double>,
    val yValues: List<Double>,
    val criticalX: Double? = null,
    val areaX: List<Double>? = null,
    val areaY: List<Double>? = null
)