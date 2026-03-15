package com.gihansgamage.statmaster.ui.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gihansgamage.statmaster.R
import com.gihansgamage.statmaster.utils.DistributionTableGenerator
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TableFragment : Fragment() {

    private lateinit var distributionChips: ChipGroup
    private lateinit var tableInfo: TextView
    private lateinit var distributionTable: android.widget.TableLayout
    private lateinit var progressBar: ProgressBar

    private enum class TableType { NORMAL, T, CHISQUARE, F, LOG10, LN }
    private var selectedTable = TableType.NORMAL
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_table, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        loadTable(TableType.NORMAL)
    }

    private fun initViews(view: View) {
        distributionChips = view.findViewById(R.id.distribution_chips)
        tableInfo         = view.findViewById(R.id.table_info)
        distributionTable = view.findViewById(R.id.distribution_table)
        progressBar       = view.findViewById(R.id.table_progress)
    }

    private fun setupListeners() {
        distributionChips.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isLoading) return@setOnCheckedStateChangeListener
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            selectedTable = when (id) {
                R.id.chip_normal    -> TableType.NORMAL
                R.id.chip_t         -> TableType.T
                R.id.chip_chisquare -> TableType.CHISQUARE
                R.id.chip_f         -> TableType.F
                R.id.chip_log10     -> TableType.LOG10
                R.id.chip_ln        -> TableType.LN
                else                -> TableType.NORMAL
            }
            loadTable(selectedTable)
        }
    }

    private fun loadTable(type: TableType) {
        if (isLoading) return
        isLoading = true

        progressBar.visibility       = View.VISIBLE
        distributionTable.visibility = View.GONE
        distributionTable.removeAllViews()

        tableInfo.text = when (type) {
            TableType.NORMAL    -> "Z-table — Cumulative probability P(Z ≤ z) for standard normal"
            TableType.T         -> "t-table — Critical values for Student's t distribution"
            TableType.CHISQUARE -> "χ²-table — Critical values for Chi-square distribution"
            TableType.F         -> "F-table (α=0.05) — Critical values for F distribution"
            TableType.LOG10     -> "log₁₀(x)  ·  Rows = x integer part, Cols = +second decimal"
            TableType.LN        -> "ln(x) = logₑ(x)  ·  Rows = x integer part, Cols = +second decimal"
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val tableData = withContext(Dispatchers.Default) {
                when (type) {
                    TableType.NORMAL    -> DistributionTableGenerator.generateZTable()
                    TableType.T         -> DistributionTableGenerator.generateTTable()
                    TableType.CHISQUARE -> DistributionTableGenerator.generateChiSquareTable()
                    TableType.F         -> DistributionTableGenerator.generateFTable()
                    TableType.LOG10     -> DistributionTableGenerator.generateLog10Table()
                    TableType.LN        -> DistributionTableGenerator.generateLnTable()
                }
            }
            if (!isAdded) return@launch
            renderTable(tableData)
        }
    }

    private fun renderTable(tableData: List<List<String>>) {
        val colorPrimary      = resources.getColor(R.color.primary, null)
        val colorPrimaryLight = resources.getColor(R.color.primary_light, null)
        val colorPrimaryDark  = resources.getColor(R.color.primary_dark, null)
        val colorWhite        = resources.getColor(R.color.white, null)
        val colorBg           = resources.getColor(R.color.background, null)
        val colorText         = resources.getColor(R.color.text_primary, null)

        tableData.forEachIndexed { rowIndex, rowData ->
            val tableRow = TableRow(requireContext())
            rowData.forEachIndexed { colIndex, cellText ->
                val cell = TextView(requireContext()).apply {
                    text = cellText
                    textSize = if (rowIndex == 0) 12f else 11f
                    setPadding(12, 8, 12, 8)
                    gravity = Gravity.CENTER
                    when {
                        rowIndex == 0 -> {
                            setTypeface(null, android.graphics.Typeface.BOLD)
                            setBackgroundColor(colorPrimary)
                            setTextColor(colorWhite)
                        }
                        colIndex == 0 -> {
                            setTypeface(null, android.graphics.Typeface.BOLD)
                            setBackgroundColor(colorPrimaryLight)
                            setTextColor(colorPrimaryDark)
                        }
                        else -> {
                            setBackgroundColor(if (rowIndex % 2 == 0) colorWhite else colorBg)
                            setTextColor(colorText)
                        }
                    }
                }
                tableRow.addView(cell)
            }
            distributionTable.addView(tableRow)
        }

        progressBar.visibility       = View.GONE
        distributionTable.visibility = View.VISIBLE
        isLoading = false
    }
}