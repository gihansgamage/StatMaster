package com.gihansgamage.statmaster.ui.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gihansgamage.statmaster.R
import com.gihansgamage.statmaster.utils.LogarithmCalculator
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.E

class LogFragment : Fragment() {

    // Tabs
    private lateinit var logTabs: TabLayout
    private lateinit var calcScroll: View
    private lateinit var tableContainer: View

    // Calculator views
    private lateinit var logXInput: TextInputEditText
    private lateinit var baseChips: ChipGroup
    private lateinit var customBaseLayout: TextInputLayout
    private lateinit var customBaseInput: TextInputEditText
    private lateinit var calcButton: MaterialButton
    private lateinit var resultCard: MaterialCardView
    private lateinit var resultText: TextView

    // Table views
    private lateinit var logTableChips: ChipGroup
    private lateinit var logTableInfo: TextView
    private lateinit var logTable: TableLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_log, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupTabs()
        setupCalculator()
        setupTableChips()
        loadLogTable("log10")
    }

    private fun initViews(view: View) {
        logTabs          = view.findViewById(R.id.log_tabs)
        calcScroll       = view.findViewById(R.id.calc_scroll)
        tableContainer   = view.findViewById(R.id.table_container)

        logXInput        = view.findViewById(R.id.log_x_input)
        baseChips        = view.findViewById(R.id.base_chips)
        customBaseLayout = view.findViewById(R.id.custom_base_layout)
        customBaseInput  = view.findViewById(R.id.custom_base_input)
        calcButton       = view.findViewById(R.id.log_calc_button)
        resultCard       = view.findViewById(R.id.log_result_card)
        resultText       = view.findViewById(R.id.log_result_text)

        logTableChips    = view.findViewById(R.id.log_table_chips)
        logTableInfo     = view.findViewById(R.id.log_table_info)
        logTable         = view.findViewById(R.id.log_table)

        // Add tabs
        logTabs.addTab(logTabs.newTab().setText("Calculator"))
        logTabs.addTab(logTabs.newTab().setText("Tables"))
    }

    // ── Tabs ─────────────────────────────────────────────────────────────────

    private fun setupTabs() {
        logTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> { calcScroll.visibility = View.VISIBLE; tableContainer.visibility = View.GONE }
                    1 -> { calcScroll.visibility = View.GONE;    tableContainer.visibility = View.VISIBLE }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    // ── Calculator ────────────────────────────────────────────────────────────

    private fun setupCalculator() {
        // Show/hide custom base input
        baseChips.setOnCheckedStateChangeListener { _, checkedIds ->
            customBaseLayout.visibility =
                if (checkedIds.firstOrNull() == R.id.chip_base_custom) View.VISIBLE else View.GONE
        }

        calcButton.setOnClickListener { calculate() }
    }

    private fun calculate() {
        try {
            val x = logXInput.text.toString().toDoubleOrNull()
                ?: throw IllegalArgumentException("Please enter a valid value for x.")
            if (x <= 0) throw IllegalArgumentException("x must be greater than 0.")

            val checkedId = baseChips.checkedChipId
            val result = buildString {
                when (checkedId) {
                    R.id.chip_base2 -> {
                        val v = LogarithmCalculator.log2(x)
                        appendLine("log₂($x) = ${fmt(v)}")
                        appendLine()
                        appendLine("Verification:  2^${fmt(v)} = ${fmt(LogarithmCalculator.antilog2(v))}")
                    }
                    R.id.chip_base10 -> {
                        val v = LogarithmCalculator.log10(x)
                        appendLine("log₁₀($x) = ${fmt(v)}")
                        appendLine()
                        appendLine("Verification:  10^${fmt(v)} = ${fmt(LogarithmCalculator.antilog10(v))}")
                    }
                    R.id.chip_base_e -> {
                        val v = LogarithmCalculator.ln(x)
                        appendLine("ln($x) = ${fmt(v)}")
                        appendLine()
                        appendLine("Verification:  e^${fmt(v)} = ${fmt(LogarithmCalculator.antilogE(v))}")
                    }
                    R.id.chip_base_custom -> {
                        val base = customBaseInput.text.toString().toDoubleOrNull()
                            ?: throw IllegalArgumentException("Please enter a valid custom base.")
                        if (base <= 0 || base == 1.0)
                            throw IllegalArgumentException("Base must be > 0 and ≠ 1.")
                        val v = LogarithmCalculator.logBase(x, base)
                        appendLine("log_${fmt(base)}($x) = ${fmt(v)}")
                        appendLine()
                        appendLine("Verification:  ${fmt(base)}^${fmt(v)} = ${fmt(LogarithmCalculator.antilogBase(v, base))}")
                    }
                    else -> throw IllegalArgumentException("Please select a base.")
                }

                // Always show all bases for comparison
                appendLine()
                appendLine("── All bases for x = $x ────────")
                appendLine("  log₂($x)   = ${fmt(LogarithmCalculator.log2(x))}")
                appendLine("  log₁₀($x)  = ${fmt(LogarithmCalculator.log10(x))}")
                appendLine("  ln($x)     = ${fmt(LogarithmCalculator.ln(x))}")

                appendLine()
                appendLine("── Change-of-base reminder ─────")
                appendLine("  logₙ(x) = ln(x) / ln(n)")
                appendLine("          = log₁₀(x) / log₁₀(n)")
            }

            resultText.text = result
            resultCard.visibility = View.VISIBLE

        } catch (e: IllegalArgumentException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid input.", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Tables ────────────────────────────────────────────────────────────────

    private fun setupTableChips() {
        logTableChips.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chip_log10_table -> loadLogTable("log10")
                R.id.chip_ln_table    -> loadLogTable("ln")
                R.id.chip_log2_table  -> loadLogTable("log2")
            }
        }
    }

    private fun loadLogTable(type: String) {
        val tableData: List<List<String>>
        val infoText: String

        when (type) {
            "log10" -> {
                tableData = LogarithmCalculator.generateLog10Table()
                infoText  = "log₁₀(x)  ·  Rows = integer part, Cols = +second decimal digit"
            }
            "ln" -> {
                tableData = LogarithmCalculator.generateLnTable()
                infoText  = "ln(x) = logₑ(x)  ·  Rows = integer part, Cols = +second decimal digit"
            }
            else -> {
                tableData = LogarithmCalculator.generateLog2Table()
                infoText  = "log₂(x)  ·  x from 1 to 64"
            }
        }

        logTableInfo.text = infoText
        logTable.removeAllViews()

        tableData.forEachIndexed { rowIndex, rowData ->
            val tableRow = TableRow(requireContext())
            rowData.forEachIndexed { colIndex, cellText ->
                val cell = TextView(requireContext()).apply {
                    text = cellText
                    textSize = if (rowIndex == 0) 11f else 10f
                    setPadding(10, 6, 10, 6)
                    gravity = Gravity.CENTER
                    when {
                        rowIndex == 0 -> {
                            setTypeface(null, android.graphics.Typeface.BOLD)
                            setBackgroundColor(resources.getColor(R.color.primary, null))
                            setTextColor(resources.getColor(R.color.white, null))
                        }
                        colIndex == 0 -> {
                            setTypeface(null, android.graphics.Typeface.BOLD)
                            setBackgroundColor(resources.getColor(R.color.primary_light, null))
                            setTextColor(resources.getColor(R.color.primary_dark, null))
                        }
                        else -> {
                            setBackgroundColor(
                                if (rowIndex % 2 == 0) resources.getColor(R.color.white, null)
                                else resources.getColor(R.color.background, null)
                            )
                            setTextColor(resources.getColor(R.color.text_primary, null))
                        }
                    }
                }
                tableRow.addView(cell)
            }
            logTable.addView(tableRow)
        }
    }

    private fun fmt(v: Double) = String.format("%.6f", v)
}