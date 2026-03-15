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

    // Normal — Z value only
    private lateinit var zScoreLayout: TextInputLayout
    private lateinit var zScoreInput: TextInputEditText

    // t — df + α
    private lateinit var dfLayout: TextInputLayout
    private lateinit var dfInput: TextInputEditText
    private lateinit var alphaLayout: TextInputLayout
    private lateinit var alphaInput: TextInputEditText

    // χ² — df + α   (reuses dfLayout/dfInput/alphaLayout/alphaInput + chiValueLayout)
    private lateinit var chiValueLayout: TextInputLayout
    private lateinit var chiValueInput: TextInputEditText

    // t observed stat (shared for t & chi)
    private lateinit var testStatLayout: TextInputLayout
    private lateinit var testStatInput: TextInputEditText

    // F — df1 + df2 + α
    private lateinit var df1Layout: TextInputLayout
    private lateinit var df1Input: TextInputEditText
    private lateinit var df2Layout: TextInputLayout
    private lateinit var df2Input: TextInputEditText
    private lateinit var fValueLayout: TextInputLayout
    private lateinit var fValueInput: TextInputEditText
    private lateinit var fAlphaLayout: TextInputLayout
    private lateinit var fAlphaInput: TextInputEditText

    // Log — base + value
    private lateinit var logBaseLayout: TextInputLayout
    private lateinit var logBaseInput: TextInputEditText
    private lateinit var logValueLayout: TextInputLayout
    private lateinit var logValueInput: TextInputEditText

    private enum class Mode { NORMAL, T, CHISQUARE, F, LOG }
    private var mode = Mode.NORMAL

    private val allLayouts get() = listOf(
        zScoreLayout,
        dfLayout, testStatLayout, alphaLayout,
        chiValueLayout,
        df1Layout, df2Layout, fValueLayout, fAlphaLayout,
        logBaseLayout, logValueLayout
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

        dfLayout       = view.findViewById(R.id.df_layout)
        dfInput        = view.findViewById(R.id.df_input)
        testStatLayout = view.findViewById(R.id.test_stat_layout)
        testStatInput  = view.findViewById(R.id.test_stat_input)
        alphaLayout    = view.findViewById(R.id.alpha_layout)
        alphaInput     = view.findViewById(R.id.alpha_input)

        chiValueLayout = view.findViewById(R.id.chi_value_layout)
        chiValueInput  = view.findViewById(R.id.chi_value_input)

        df1Layout    = view.findViewById(R.id.df1_layout)
        df1Input     = view.findViewById(R.id.df1_input)
        df2Layout    = view.findViewById(R.id.df2_layout)
        df2Input     = view.findViewById(R.id.df2_input)
        fValueLayout = view.findViewById(R.id.f_value_layout)
        fValueInput  = view.findViewById(R.id.f_value_input)
        fAlphaLayout = view.findViewById(R.id.f_alpha_layout)
        fAlphaInput  = view.findViewById(R.id.f_alpha_input)

        logBaseLayout  = view.findViewById(R.id.log_base_layout)
        logBaseInput   = view.findViewById(R.id.log_base_input)
        logValueLayout = view.findViewById(R.id.log_value_layout)
        logValueInput  = view.findViewById(R.id.log_value_input)
    }

    private fun setupListeners() {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            mode = when (checkedId) {
                R.id.radio_normal    -> Mode.NORMAL
                R.id.radio_t         -> Mode.T
                R.id.radio_chisquare -> Mode.CHISQUARE
                R.id.radio_f         -> Mode.F
                R.id.radio_log       -> Mode.LOG
                else                 -> Mode.NORMAL
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

        when (mode) {
            Mode.NORMAL -> {
                paramsHint.text = "Enter a Z-score to find cumulative probability."
                zScoreLayout.hint = "Z-Score"
                show(zScoreLayout)
            }
            Mode.T -> {
                paramsHint.text = "Enter df and α to get the critical value and decision."
                show(dfLayout, alphaLayout)
            }
            Mode.CHISQUARE -> {
                paramsHint.text = "Enter df and α to get the critical χ² value."
                show(dfLayout, alphaLayout)
            }
            Mode.F -> {
                paramsHint.text = "Enter df₁, df₂ and α to get the critical F value."
                show(df1Layout, df2Layout, fAlphaLayout)
            }
            Mode.LOG -> {
                paramsHint.text = "Enter base and value to compute logBase(value). E.g. base=3, value=25 → log₃(25)."
                show(logBaseLayout, logValueLayout)
            }
        }
    }

    private fun show(vararg layouts: TextInputLayout) =
        layouts.forEach { it.visibility = View.VISIBLE }

    // ── Calculations ──────────────────────────────────────────────────────────

    private fun performCalculation() {
        try {
            val result = when (mode) {
                Mode.NORMAL    -> calcNormal()
                Mode.T         -> calcT()
                Mode.CHISQUARE -> calcChiSquare()
                Mode.F         -> calcF()
                Mode.LOG       -> calcLog()
            }
            resultText.text = result.text
            resultsCard.visibility = View.VISIBLE
            if (mode != Mode.LOG) drawGraph(result.graphData) else { graphCard.visibility = View.GONE }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(requireContext(), e.message ?: "Invalid input", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid input — please check your values.", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Normal ────────────────────────────────────────────────────────────────

    private fun calcNormal(): CalcResult {
        val z = zScoreInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Z-score is required.")

        val cdf     = NormalDistribution.cdf(z)
        val pdf     = NormalDistribution.pdf(z)
        val upper   = 1.0 - cdf
        val twoTail = NormalDistribution.twoTailedProbability(z)

        val text = buildString {
            appendLine("── Input ────────────────────────")
            appendLine("  Z = $z")
            appendLine()
            appendLine("── Probabilities ───────────────")
            appendLine("  P(Z ≤ ${fmt(z)})  =  ${fmt(cdf)}")
            appendLine("  P(Z > ${fmt(z)})  =  ${fmt(upper)}")
            appendLine("  P(|Z| > ${fmt(kotlin.math.abs(z))}) = ${fmt(twoTail)}  (two-tail)")
            appendLine()
            appendLine("── Density ─────────────────────")
            appendLine("  f(${fmt(z)}) = ${fmt(pdf)}")
        }

        val graphData = GraphDrawer.generateGraphData(
            DistributionType.NORMAL,
            mapOf("z" to z, "mean" to 0.0, "stdDev" to 1.0),
            z
        )
        return CalcResult(text, graphData)
    }

    // ── Student's t ───────────────────────────────────────────────────────────

    private fun calcT(): CalcResult {
        val df    = dfInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Degrees of freedom (df) is required.")
        val alpha = alphaInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Significance level α is required.")
        if (df <= 0)             throw IllegalArgumentException("df must be > 0.")
        if (alpha !in 0.0..1.0) throw IllegalArgumentException("α must be between 0 and 1.")

        val criticalOneTail  = TDistribution.inverseCDF(1.0 - alpha, df)
        val criticalTwoTail  = TDistribution.inverseCDF(1.0 - alpha / 2.0, df)

        val text = buildString {
            appendLine("── Inputs ──────────────────────")
            appendLine("  df = ${df.toInt()},  α = $alpha")
            appendLine()
            appendLine("── Critical Values ─────────────")
            appendLine("  One-tail  t* (α=$alpha)   = ${fmt(criticalOneTail)}")
            appendLine("  Two-tail  t* (α/2=${alpha/2}) = ±${fmt(criticalTwoTail)}")
            appendLine()
            appendLine("── Decision Rule ───────────────")
            appendLine("  Reject H₀ (one-tail) if  t > ${fmt(criticalOneTail)}")
            appendLine("  Reject H₀ (two-tail) if |t| > ${fmt(criticalTwoTail)}")
        }

        val graphData = GraphDrawer.generateGraphData(
            DistributionType.T, mapOf("t" to criticalOneTail, "df" to df), criticalOneTail
        )
        return CalcResult(text, graphData)
    }

    // ── Chi-square ────────────────────────────────────────────────────────────

    private fun calcChiSquare(): CalcResult {
        val df    = dfInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Degrees of freedom (df) is required.")
        val alpha = alphaInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Significance level α is required.")
        if (df <= 0)             throw IllegalArgumentException("df must be > 0.")
        if (alpha !in 0.0..1.0) throw IllegalArgumentException("α must be between 0 and 1.")

        val critical = ChiSquareDistribution.inverseCDF(1.0 - alpha, df)

        val text = buildString {
            appendLine("── Inputs ──────────────────────")
            appendLine("  df = ${df.toInt()},  α = $alpha")
            appendLine()
            appendLine("── Critical Value ───────────────")
            appendLine("  χ²* (α=$alpha, df=${df.toInt()}) = ${fmt(critical)}")
            appendLine()
            appendLine("── Decision Rule ───────────────")
            appendLine("  Reject H₀ if  χ² > ${fmt(critical)}")
        }

        val graphData = GraphDrawer.generateGraphData(
            DistributionType.CHISQUARE, mapOf("chi" to critical, "df" to df), critical
        )
        return CalcResult(text, graphData)
    }

    // ── F distribution ────────────────────────────────────────────────────────

    private fun calcF(): CalcResult {
        val df1   = df1Input.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("df₁ is required.")
        val df2   = df2Input.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("df₂ is required.")
        val alpha = fAlphaInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Significance level α is required.")
        if (df1 <= 0 || df2 <= 0) throw IllegalArgumentException("df₁ and df₂ must be > 0.")
        if (alpha !in 0.0..1.0)   throw IllegalArgumentException("α must be between 0 and 1.")

        val critical = FDistribution.inverseCDF(1.0 - alpha, df1, df2)

        val text = buildString {
            appendLine("── Inputs ──────────────────────")
            appendLine("  df₁ = ${df1.toInt()},  df₂ = ${df2.toInt()},  α = $alpha")
            appendLine()
            appendLine("── Critical Value ───────────────")
            appendLine("  F* (α=$alpha, df₁=${df1.toInt()}, df₂=${df2.toInt()}) = ${fmt(critical)}")
            appendLine()
            appendLine("── Decision Rule ───────────────")
            appendLine("  Reject H₀ if  F > ${fmt(critical)}")
        }

        val graphData = GraphDrawer.generateGraphData(
            DistributionType.F, mapOf("f" to critical, "df1" to df1, "df2" to df2), critical
        )
        return CalcResult(text, graphData)
    }

    // ── Log ───────────────────────────────────────────────────────────────────

    private fun calcLog(): CalcResult {
        val base  = logBaseInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Base is required.")
        val value = logValueInput.text.toString().toDoubleOrNull()
            ?: throw IllegalArgumentException("Value is required.")
        if (base <= 0 || base == 1.0) throw IllegalArgumentException("Base must be > 0 and ≠ 1.")
        if (value <= 0)               throw IllegalArgumentException("Value must be > 0.")

        val result = LogarithmCalculator.logBase(value, base)

        val text = buildString {
            appendLine("── Input ────────────────────────")
            appendLine("  base = $base,  value = $value")
            appendLine()
            appendLine("── Result ───────────────────────")
            appendLine("  log${subscript(base)}($value) = ${fmt(result)}")
            appendLine()
            appendLine("── Verification ────────────────")
            appendLine("  ${fmt(base)} ^ ${fmt(result)} = ${fmt(LogarithmCalculator.antilogBase(result, base))}")
            appendLine()
            appendLine("── Same value in common bases ──")
            appendLine("  log₂($value)   = ${fmt(LogarithmCalculator.log2(value))}")
            appendLine("  log₁₀($value)  = ${fmt(LogarithmCalculator.log10(value))}")
            appendLine("  ln($value)     = ${fmt(LogarithmCalculator.ln(value))}")
        }
        return CalcResult(text, null)
    }

    private fun subscript(base: Double): String {
        val s = if (base == base.toLong().toDouble()) base.toLong().toString() else base.toString()
        return s.map { c ->
            when (c) {
                '0' -> '₀'; '1' -> '₁'; '2' -> '₂'; '3' -> '₃'; '4' -> '₄'
                '5' -> '₅'; '6' -> '₆'; '7' -> '₇'; '8' -> '₈'; '9' -> '₉'
                else -> c
            }
        }.joinToString("")
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

        val distType = when (mode) {
            Mode.NORMAL    -> DistributionType.NORMAL
            Mode.T         -> DistributionType.T
            Mode.CHISQUARE -> DistributionType.CHISQUARE
            Mode.F         -> DistributionType.F
            else           -> DistributionType.NORMAL
        }

        val color = when (distType) {
            DistributionType.NORMAL    -> R.color.normal_color
            DistributionType.T         -> R.color.t_color
            DistributionType.CHISQUARE -> R.color.chisquare_color
            DistributionType.F         -> R.color.f_color
        }.let { resources.getColor(it, null) }

        val dataSet = LineDataSet(entries, distType.displayName).apply {
            this.color = color
            setDrawCircles(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillAlpha = 40
            fillColor = color
        }

        distributionChart.setBackgroundColor(resources.getColor(R.color.white, null))
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