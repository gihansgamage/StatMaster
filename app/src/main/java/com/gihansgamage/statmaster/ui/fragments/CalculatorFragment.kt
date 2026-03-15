package com.gihansgamage.statmaster.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gihansgamage.statmaster.R
import com.gihansgamage.statmaster.models.DistributionType
import com.gihansgamage.statmaster.models.GraphData
import com.gihansgamage.statmaster.utils.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

class CalculatorFragment : Fragment() {

    private lateinit var distributionRadioGroup: RadioGroup
    private lateinit var zScoreInput: TextInputEditText
    private lateinit var dfInput: TextInputEditText
    private lateinit var df1Input: TextInputEditText
    private lateinit var df2Input: TextInputEditText
    private lateinit var meanInput: TextInputEditText
    private lateinit var stdDevInput: TextInputEditText
    private lateinit var calculateButton: MaterialButton
    private lateinit var resultsCard: MaterialCardView
    private lateinit var resultText: android.widget.TextView
    private lateinit var graphCard: MaterialCardView
    private lateinit var distributionChart: LineChart

    private var selectedDistribution = DistributionType.NORMAL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calculator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        distributionRadioGroup = view.findViewById(R.id.distribution_radio_group)
        zScoreInput = view.findViewById(R.id.z_score_input)
        dfInput = view.findViewById(R.id.df_input)
        df1Input = view.findViewById(R.id.df1_input)
        df2Input = view.findViewById(R.id.df2_input)
        meanInput = view.findViewById(R.id.mean_input)
        stdDevInput = view.findViewById(R.id.std_dev_input)
        calculateButton = view.findViewById(R.id.calculate_button)
        resultsCard = view.findViewById(R.id.results_card)
        resultText = view.findViewById(R.id.result_text)
        graphCard = view.findViewById(R.id.graph_card)
        distributionChart = view.findViewById(R.id.distribution_chart)
    }

    private fun setupListeners() {
        distributionRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedDistribution = when (checkedId) {
                R.id.radio_normal -> DistributionType.NORMAL
                R.id.radio_t -> DistributionType.T
                R.id.radio_chisquare -> DistributionType.CHISQUARE
                R.id.radio_f -> DistributionType.F
                else -> DistributionType.NORMAL
            }
            updateInputFields()
        }

        calculateButton.setOnClickListener {
            performCalculation()
        }
    }

    private fun updateInputFields() {
        // Hide all input fields first
        view?.findViewById<TextInputLayout>(R.id.z_score_layout)?.visibility = View.GONE
        view?.findViewById<TextInputLayout>(R.id.df_layout)?.visibility = View.GONE
        view?.findViewById<TextInputLayout>(R.id.df1_layout)?.visibility = View.GONE
        view?.findViewById<TextInputLayout>(R.id.df2_layout)?.visibility = View.GONE
        view?.findViewById<TextInputLayout>(R.id.mean_layout)?.visibility = View.GONE
        view?.findViewById<TextInputLayout>(R.id.std_dev_layout)?.visibility = View.GONE

        // Show relevant input fields based on distribution
        when (selectedDistribution) {
            DistributionType.NORMAL -> {
                view?.findViewById<TextInputLayout>(R.id.z_score_layout)?.visibility = View.VISIBLE
                view?.findViewById<TextInputLayout>(R.id.mean_layout)?.visibility = View.VISIBLE
                view?.findViewById<TextInputLayout>(R.id.std_dev_layout)?.visibility = View.VISIBLE
            }
            DistributionType.T -> {
                view?.findViewById<TextInputLayout>(R.id.z_score_layout)?.visibility = View.VISIBLE
                view?.findViewById<TextInputLayout>(R.id.z_score_layout)?.hint = "t-value"
                view?.findViewById<TextInputLayout>(R.id.df_layout)?.visibility = View.VISIBLE
            }
            DistributionType.CHISQUARE -> {
                view?.findViewById<TextInputLayout>(R.id.z_score_layout)?.visibility = View.VISIBLE
                view?.findViewById<TextInputLayout>(R.id.z_score_layout)?.hint = "Chi-square value"
                view?.findViewById<TextInputLayout>(R.id.df_layout)?.visibility = View.VISIBLE
            }
            DistributionType.F -> {
                view?.findViewById<TextInputLayout>(R.id.z_score_layout)?.visibility = View.VISIBLE
                view?.findViewById<TextInputLayout>(R.id.z_score_layout)?.hint = "F-value"
                view?.findViewById<TextInputLayout>(R.id.df1_layout)?.visibility = View.VISIBLE
                view?.findViewById<TextInputLayout>(R.id.df2_layout)?.visibility = View.VISIBLE
            }
        }

        // Reset hints
        if (selectedDistribution == DistributionType.NORMAL) {
            view?.findViewById<TextInputLayout>(R.id.z_score_layout)?.hint = getString(R.string.z_score)
        }

        // Hide results
        resultsCard.visibility = View.GONE
        graphCard.visibility = View.GONE
    }

    private fun performCalculation() {
        try {
            val result = when (selectedDistribution) {
                DistributionType.NORMAL -> calculateNormal()
                DistributionType.T -> calculateT()
                DistributionType.CHISQUARE -> calculateChiSquare()
                DistributionType.F -> calculateF()
            }

            displayResults(result)
            drawGraph(result.graphData)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.error_invalid_input), Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateNormal(): Pair<String, GraphData?> {
        val z = zScoreInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid Z-score")
        val mean = meanInput.text.toString().toDoubleOrNull() ?: 0.0
        val stdDev = stdDevInput.text.toString().toDoubleOrNull() ?: 1.0

        if (stdDev <= 0) {
            throw IllegalArgumentException("Standard deviation must be positive")
        }

        val cdfValue = NormalDistribution.cdf(z)
        val pdfValue = NormalDistribution.pdf(z, mean, stdDev)
        val twoTailed = NormalDistribution.twoTailedProbability(z)

        val resultText = """
            Z-Score: $z
            Mean (μ): $mean
            Std Dev (σ): $stdDev
            
            Cumulative Probability (P(Z ≤ $z)): ${String.format("%.6f", cdfValue)}
            Probability Density: ${String.format("%.6f", pdfValue)}
            Two-tailed Probability: ${String.format("%.6f", twoTailed)}
        """.trimIndent()

        val graphData = GraphDrawer.generateGraphData(
            DistributionType.NORMAL,
            mapOf("z" to z, "mean" to mean, "stdDev" to stdDev),
            z
        )

        return resultText to graphData
    }

    private fun calculateT(): Pair<String, GraphData?> {
        val t = zScoreInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid t-value")
        val df = dfInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid degrees of freedom")

        if (df <= 0) {
            throw IllegalArgumentException("Degrees of freedom must be positive")
        }

        val cdfValue = TDistribution.cdf(t, df)
        val pdfValue = TDistribution.pdf(t, df)
        val twoTailed = 2.0 * (1.0 - cdfValue)

        val resultText = """
            t-value: $t
            Degrees of Freedom: $df
            
            Cumulative Probability (P(T ≤ $t)): ${String.format("%.6f", cdfValue)}
            Probability Density: ${String.format("%.6f", pdfValue)}
            Two-tailed P-value: ${String.format("%.6f", twoTailed)}
        """.trimIndent()

        val graphData = GraphDrawer.generateGraphData(
            DistributionType.T,
            mapOf("t" to t, "df" to df),
            t
        )

        return resultText to graphData
    }

    private fun calculateChiSquare(): Pair<String, GraphData?> {
        val chi = zScoreInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid Chi-square value")
        val df = dfInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid degrees of freedom")

        if (chi < 0 || df <= 0) {
            throw IllegalArgumentException("Invalid parameters")
        }

        val cdfValue = ChiSquareDistribution.cdf(chi, df)
        val pdfValue = ChiSquareDistribution.pdf(chi, df)
        val pValue = 1.0 - cdfValue

        val resultText = """
            Chi-square value: $chi
            Degrees of Freedom: $df
            
            Cumulative Probability (P(χ² ≤ $chi)): ${String.format("%.6f", cdfValue)}
            Probability Density: ${String.format("%.6f", pdfValue)}
            P-value (P(χ² > $chi)): ${String.format("%.6f", pValue)}
        """.trimIndent()

        val graphData = GraphDrawer.generateGraphData(
            DistributionType.CHISQUARE,
            mapOf("chi" to chi, "df" to df),
            chi
        )

        return resultText to graphData
    }

    private fun calculateF(): Pair<String, GraphData?> {
        val f = zScoreInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid F-value")
        val df1 = df1Input.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid df1")
        val df2 = df2Input.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid df2")

        if (f < 0 || df1 <= 0 || df2 <= 0) {
            throw IllegalArgumentException("Invalid parameters")
        }

        val cdfValue = FDistribution.cdf(f, df1, df2)
        val pdfValue = FDistribution.pdf(f, df1, df2)
        val pValue = 1.0 - cdfValue

        val resultText = """
            F-value: $f
            Degrees of Freedom 1: $df1
            Degrees of Freedom 2: $df2
            
            Cumulative Probability (P(F ≤ $f)): ${String.format("%.6f", cdfValue)}
            Probability Density: ${String.format("%.6f", pdfValue)}
            P-value (P(F > $f)): ${String.format("%.6f", pValue)}
        """.trimIndent()

        val graphData = GraphDrawer.generateGraphData(
            DistributionType.F,
            mapOf("f" to f, "df1" to df1, "df2" to df2),
            f
        )

        return resultText to graphData
    }

    private fun displayResults(resultText: String) {
        this.resultText.text = resultText
        resultsCard.visibility = View.VISIBLE
    }

    private fun drawGraph(graphData: GraphData?) {
        if (graphData == null) {
            graphCard.visibility = View.GONE
            return
        }

        graphCard.visibility = View.VISIBLE

        // Create entries
        val entries = graphData.xValues.zip(graphData.yValues).map { (x, y) ->
            Entry(x.toFloat(), y.toFloat())
        }

        // Create dataset
        val dataSet = LineDataSet(entries, "Distribution").apply {
            color = getColorForDistribution(selectedDistribution)
            setDrawCircles(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        // Create line data
        val lineData = LineData(dataSet)
        distributionChart.data = lineData

        // Configure chart
        distributionChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.2f", value)
                    }
                }
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            legend.isEnabled = true

            invalidate()
        }
    }

    private fun getColorForDistribution(distribution: DistributionType): Int {
        return when (distribution) {
            DistributionType.NORMAL -> R.color.normal_color
            DistributionType.T -> R.color.t_color
            DistributionType.CHISQUARE -> R.color.chisquare_color
            DistributionType.F -> R.color.f_color
        }.let { resources.getColor(it, null) }
    }
}