package com.gihansgamage.statmaster.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gihansgamage.statmaster.R
import com.gihansgamage.statmaster.models.DistributionType
import com.gihansgamage.statmaster.models.GraphData
import com.gihansgamage.statmaster.utils.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CalculatorFragment : Fragment() {

    // --- Views ---
    private lateinit var radioGroup: RadioGroup
    private lateinit var paramsHint: TextView
    private lateinit var calculateButton: MaterialButton
    private lateinit var resultsCard: MaterialCardView
    private lateinit var resultText: TextView
    private lateinit var graphCard: MaterialCardView
    private lateinit var distributionChart: LineChart

    // Normal
    private lateinit var zScoreLayout: TextInputLayout
    private lateinit var zScoreInput: TextInputEditText
    private lateinit var meanLayout: TextInputLayout
    private lateinit var meanInput: TextInputEditText
    private lateinit var stdDevLayout: TextInputLayout
    private lateinit var stdDevInput: TextInputEditText

    // t / chi-square shared
    private lateinit var dfLayout: TextInputLayout
    private lateinit var dfInput: TextInputEditText
    private lateinit var testStatLayout: TextInputLayout
    private lateinit var testStatInput: TextInputEditText
    private lateinit var alphaLayout: TextInputLayout
    private lateinit var alphaInput: TextInputEditText

    // F
    private lateinit var df1Layout: TextInputLayout
    private lateinit var df1Input: TextInputEditText
    private lateinit var df2Layout: TextInputLayout
    private lateinit var df2Input: TextInputEditText
    private lateinit var fValueLayout: TextInputLayout
    private lateinit var fValueInput: TextInputEditText
    private lateinit var fAlphaLayout: TextInputLayout
    private lateinit var fAlphaInput: TextInputEditText

    private var selectedDistribution = DistributionType.NORMAL

    private val allLayouts get() = listOf(
        zScoreLayout, meanLayout, stdDevLayout,
        dfLayout, testStatLayout, alphaLayout,
        df1Layout, df2Layout, fValueLayout, fAlphaLayout
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_calculator, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        updateInputFields()
    }

    private fun initViews(view: View) {
        radioGroup        = view.findViewById(R.id.distribution_radio_group)
        paramsHint        = view.findViewById(R.id.params_hint)
        calculateButton   = view.findViewById(R.id.calculate_button)
        resultsCard       = view.findViewById(R.id.results_card)
        resultText        = view.findViewById(R.id.result_text)
        graphCard         = view.findViewById(R.id.graph_card)
        distributionChart = view.findViewById(R.id.distribution_chart)

        zScoreLayout  = view.findViewById(R.id.z_score_layout)
        zScoreInput   = view.findViewById(R.id.z_score_input)
        meanLayout    = view.findViewById(R.id.mean_layout)
        meanInput     = view.findViewById(R.id.mean_input)
        stdDevLayout  = view.findViewById(R.id.std_dev_layout)
        stdDevInput   = view.findViewById(R.id.std_dev_input)

        dfLayout       = view.findViewById(R.id.df_layout)
        dfInput        = view.findViewById(R.id.df_input)
        testStatLayout = view.findViewById(R.id.test_stat_layout)
        testStatInput  = view.findViewById(R.id.test_stat_input)
        alphaLayout    = view.findViewById(R.id.alpha_layout)
        alphaInput     = view.findViewById(R.id.alpha_input)

        df1Layout    = view.findViewById(R.id.df1_layout)
        df1Input     = view.findViewById(R.id.df1_input)
        df2Layout    = view.findViewById(R.id.df2_layout)
        df2Input     = view.findViewById(R.id.df2_input)
        fValueLayout = view.findViewById(R.id.f_value_layout)
        fValueInput  = view.findViewById(R.id.f_value_input)
        fAlphaLayout = view.findViewById(R.id.f_alpha_layout)
        fAlphaInput  = view.findViewById(R.id.f_alpha_input)
    }

    private fun setupListeners() {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedDistribution = when (checkedId) {
                R.id.radio_normal    -> DistributionType.NORMAL
                R.id.radio_t         -> DistributionType.T
                R.id.radio_chisquare -> DistributionType.CHISQUARE
                R.id.radio_f         -> DistributionType.F
                else                 -> DistributionType.NORMAL
            }
            updateInputFields()
        }
        calculateButton.setOnClickListener { performCalculation() }
    }

    // ── Show/hide the right fields ────────────────────────────────────────────

    private fun updateInputFields() {
        allLayouts.forEach { it.visibility = View.GONE }
        resultsCard.visibility = View.GONE
        graphCard.visibility   = View.GONE

        when (selectedDistribution) {
            DistributionType.NORMAL -> {
                paramsHint.text = "Enter a Z-score (or any x value with μ and σ) to find cumulative probability."
                zScoreLayout.hint = "Z-Score (or x value)"
                show(zScoreLayout, meanLayout, stdDevLayout)
            }
            DistributionType.T -> {
                paramsHint.text = "Enter df, the observed t-value, and α to get the critical value and decision."
                testStatLayout.hint = "Observed t-value"
                show(dfLayout, testStatLayout, alphaLayout)
            }
            DistributionType.CHISQUARE -> {
                paramsHint.text = "Enter df, the observed χ² value, and α to get the critical value and decision."
                testStatLayout.hint = "Observed χ² value"
                show(dfLayout, testStatLayout, alphaLayout)
            }
            DistributionType.F -> {
                paramsHint.text = "Enter df₁ (numerator), df₂ (denominator), the observed F-value, and α."
                show(df1Layout, df2Layout, fValueLayout, fAlphaLayout)
            }
        }
    }

    private fun show(vararg layouts: TextInputLayout) =
        layouts.forEach { it.visibility = View.VISIBLE }

    // ── Calculations ──────────────────────────────────────────────────────────

    private fun performCalculation() {
        try {
            val result = when (selectedDistribution) {
                DistributionType.NORMAL    -> calcNormal()
                DistributionType.T         -> calcT()
                DistributionType.CHISQUARE -> calcChiSquare()
                DistributionType.F         -> calcF()
            }
            resultText.text = result.text
            resultsCard.visibility = View.VISIBLE
            drawGraph(result.graphData)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(requireContext(), e.message ?: "Invalid input", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid input — please check your values.", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Normal ────────────────────────────────────────────────────────────────

    private fun calcNormal(): CalcResult {
        val x      = zScoreInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Z-score / x value is required.")
        val mean   = meanInput.text.toString().toDoubleOrNull() ?: 0.0
        val stdDev = stdDevInput.text.toString().toDoubleOrNull() ?: 1.0
        if (stdDev <= 0) throw IllegalArgumentException("Standard deviation must be > 0.")

        val z       = (x - mean) / stdDev
        val cdf     = NormalDistribution.cdf(z)
        val pdf     = NormalDistribution.pdf(x, mean, stdDev)
        val upper   = 1.0 - cdf
        val twoTail = NormalDistribution.twoTailedProbability(z)

        val text = buildString {
            appendLine("── Inputs ──────────────────────")
            appendLine("  x = $x,  μ = $mean,  σ = $stdDev")
            appendLine("  Z-score = ${fmt(z)}")
            appendLine()
            appendLine("── Probabilities ───────────────")
            appendLine("  P(X ≤ $x)  =  ${fmt(cdf)}")
            appendLine("  P(X > $x)  =  ${fmt(upper)}")
            appendLine("  P(|Z| > ${fmt(kotlin.math.abs(z))}) = ${fmt(twoTail)}  (two-tail)")
            appendLine()
            appendLine("── Density ─────────────────────")
            appendLine("  f($x) = ${fmt(pdf)}")
        }

        val graphData = GraphDrawer.generateGraphData(
            DistributionType.NORMAL,
            mapOf("z" to z, "mean" to mean, "stdDev" to stdDev),
            z
        )
        return CalcResult(text, graphData)
    }

    // ── Student's t ───────────────────────────────────────────────────────────

    private fun calcT(): CalcResult {
        val df    = dfInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Degrees of freedom (df) is required.")
        val tObs  = testStatInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Observed t-value is required.")
        val alpha = alphaInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Significance level α is required.")
        if (df <= 0)             throw IllegalArgumentException("df must be > 0.")
        if (alpha !in 0.0..1.0) throw IllegalArgumentException("α must be between 0 and 1.")

        val cdf              = TDistribution.cdf(tObs, df)
        val upperP           = 1.0 - cdf
        val twoTailP         = 2.0 * minOf(cdf, upperP)
        val criticalOneTail  = TDistribution.inverseCDF(1.0 - alpha, df)
        val criticalTwoTail  = TDistribution.inverseCDF(1.0 - alpha / 2.0, df)
        val rejectOneTail    = tObs > criticalOneTail
        val rejectTwoTail    = kotlin.math.abs(tObs) > criticalTwoTail

        val text = buildString {
            appendLine("── Inputs ──────────────────────")
            appendLine("  df = ${df.toInt()},  t = $tObs,  α = $alpha")
            appendLine()
            appendLine("── Probabilities ───────────────")
            appendLine("  P(T ≤ $tObs) = ${fmt(cdf)}")
            appendLine("  P(T > $tObs) = ${fmt(upperP)}   (one-tail p-value)")
            appendLine("  Two-tail p-value = ${fmt(twoTailP)}")
            appendLine()
            appendLine("── Critical Values ─────────────")
            appendLine("  One-tail  t* (α=$alpha)   = ${fmt(criticalOneTail)}")
            appendLine("  Two-tail  t* (α/2=${alpha/2}) = ±${fmt(criticalTwoTail)}")
            appendLine()
            appendLine("── Decision (α = $alpha) ────────")
            appendLine("  One-tail: ${if (rejectOneTail) "REJECT H₀  (t > t*)" else "Fail to reject H₀"}")
            appendLine("  Two-tail: ${if (rejectTwoTail) "REJECT H₀  (|t| > t*)" else "Fail to reject H₀"}")
        }

        val graphData = GraphDrawer.generateGraphData(
            DistributionType.T, mapOf("t" to tObs, "df" to df), tObs
        )
        return CalcResult(text, graphData)
    }

    // ── Chi-square ────────────────────────────────────────────────────────────

    private fun calcChiSquare(): CalcResult {
        val df     = dfInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Degrees of freedom (df) is required.")
        val chiObs = testStatInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Observed χ² value is required.")
        val alpha  = alphaInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Significance level α is required.")
        if (df <= 0)             throw IllegalArgumentException("df must be > 0.")
        if (chiObs < 0)         throw IllegalArgumentException("χ² value must be ≥ 0.")
        if (alpha !in 0.0..1.0) throw IllegalArgumentException("α must be between 0 and 1.")

        val cdf      = ChiSquareDistribution.cdf(chiObs, df)
        val pValue   = 1.0 - cdf
        val critical = ChiSquareDistribution.inverseCDF(1.0 - alpha, df)
        val reject   = chiObs > critical

        val text = buildString {
            appendLine("── Inputs ──────────────────────")
            appendLine("  df = ${df.toInt()},  χ² = $chiObs,  α = $alpha")
            appendLine()
            appendLine("── Probabilities ───────────────")
            appendLine("  P(χ² ≤ $chiObs) = ${fmt(cdf)}")
            appendLine("  P(χ² > $chiObs) = ${fmt(pValue)}   (p-value)")
            appendLine()
            appendLine("── Critical Value ───────────────")
            appendLine("  χ²* (α=$alpha, df=${df.toInt()}) = ${fmt(critical)}")
            appendLine()
            appendLine("── Decision (α = $alpha) ────────")
            appendLine("  ${if (reject) "REJECT H₀  (χ² > χ²*)" else "Fail to reject H₀"}")
        }

        val graphData = GraphDrawer.generateGraphData(
            DistributionType.CHISQUARE, mapOf("chi" to chiObs, "df" to df), chiObs
        )
        return CalcResult(text, graphData)
    }

    // ── F distribution ────────────────────────────────────────────────────────

    private fun calcF(): CalcResult {
        val df1   = df1Input.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("df₁ is required.")
        val df2   = df2Input.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("df₂ is required.")
        val fObs  = fValueInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Observed F-value is required.")
        val alpha = fAlphaInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Significance level α is required.")
        if (df1 <= 0 || df2 <= 0) throw IllegalArgumentException("df₁ and df₂ must be > 0.")
        if (fObs < 0)             throw IllegalArgumentException("F-value must be ≥ 0.")
        if (alpha !in 0.0..1.0)  throw IllegalArgumentException("α must be between 0 and 1.")

        val cdf      = FDistribution.cdf(fObs, df1, df2)
        val pValue   = 1.0 - cdf
        val critical = FDistribution.inverseCDF(1.0 - alpha, df1, df2)
        val reject   = fObs > critical

        val text = buildString {
            appendLine("── Inputs ──────────────────────")
            appendLine("  df₁ = ${df1.toInt()},  df₂ = ${df2.toInt()}")
            appendLine("  F = $fObs,  α = $alpha")
            appendLine()
            appendLine("── Probabilities ───────────────")
            appendLine("  P(F ≤ $fObs) = ${fmt(cdf)}")
            appendLine("  P(F > $fObs) = ${fmt(pValue)}   (p-value)")
            appendLine()
            appendLine("── Critical Value ───────────────")
            appendLine("  F* (α=$alpha, df₁=${df1.toInt()}, df₂=${df2.toInt()}) = ${fmt(critical)}")
            appendLine()
            appendLine("── Decision (α = $alpha) ────────")
            appendLine("  ${if (reject) "REJECT H₀  (F > F*)" else "Fail to reject H₀"}")
        }

        val graphData = GraphDrawer.generateGraphData(
            DistributionType.F, mapOf("f" to fObs, "df1" to df1, "df2" to df2), fObs
        )
        return CalcResult(text, graphData)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun fmt(v: Double) = String.format("%.6f", v)

    private data class CalcResult(val text: String, val graphData: GraphData?)

    // ── Graph ─────────────────────────────────────────────────────────────────

    private fun drawGraph(graphData: GraphData?) {
        if (graphData == null) { graphCard.visibility = View.GONE; return }
        graphCard.visibility = View.VISIBLE

        val entries = graphData.xValues.zip(graphData.yValues).map { (x, y) ->
            Entry(x.toFloat(), y.toFloat())
        }

        val color = when (selectedDistribution) {
            DistributionType.NORMAL    -> R.color.normal_color
            DistributionType.T         -> R.color.t_color
            DistributionType.CHISQUARE -> R.color.chisquare_color
            DistributionType.F         -> R.color.f_color
        }.let { resources.getColor(it, null) }

        val dataSet = LineDataSet(entries, selectedDistribution.displayName).apply {
            this.color = color
            setDrawCircles(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillAlpha = 40
            fillColor = color
        }

        distributionChart.apply {
            data = LineData(dataSet)
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
                    override fun getFormattedValue(value: Float) =
                        String.format("%.2f", value)
                }
            }
            axisLeft.apply { setDrawGridLines(true); axisMinimum = 0f }
            axisRight.isEnabled = false
            legend.isEnabled = true
            invalidate()
        }
    }
}