package com.gihansgamage.statmaster.ui.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.gihansgamage.statmaster.R
import com.gihansgamage.statmaster.models.DistributionType
import com.gihansgamage.statmaster.utils.DistributionTableGenerator
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class TableFragment : Fragment() {

    private lateinit var distributionChips: ChipGroup
    private lateinit var tableInfo: TextView
    private lateinit var distributionTable: android.widget.TableLayout

    private var selectedDistribution = DistributionType.NORMAL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_table, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
        loadTable(DistributionType.NORMAL)
    }

    private fun initViews(view: View) {
        distributionChips = view.findViewById(R.id.distribution_chips)
        tableInfo = view.findViewById(R.id.table_info)
        distributionTable = view.findViewById(R.id.distribution_table)
    }

    private fun setupListeners() {
        distributionChips.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedChipId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val distribution = when (selectedChipId) {
                R.id.chip_normal -> DistributionType.NORMAL
                R.id.chip_t -> DistributionType.T
                R.id.chip_chisquare -> DistributionType.CHISQUARE
                R.id.chip_f -> DistributionType.F
                else -> DistributionType.NORMAL
            }
            selectedDistribution = distribution
            loadTable(distribution)
        }
    }

    private fun loadTable(distribution: DistributionType) {
        // Update table info
        tableInfo.text = "${distribution.tableName} - ${distribution.useCase}"

        // Clear existing table
        distributionTable.removeAllViews()

        // Generate table data
        val tableData = DistributionTableGenerator.getTable(distribution)

        // Populate table
        tableData.forEachIndexed { rowIndex, rowData ->
            val tableRow = TableRow(requireContext()).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
            }

            rowData.forEachIndexed { colIndex, cellText ->
                val cellView = TextView(requireContext()).apply {
                    text = cellText
                    textSize = if (rowIndex == 0) 12f else 11f
                    setPadding(12, 8, 12, 8)

                    // Style header row
                    if (rowIndex == 0) {
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setBackgroundColor(resources.getColor(R.color.primary, null))
                        setTextColor(resources.getColor(R.color.white, null))
                    } else {
                        // Alternate row colors
                        setBackgroundColor(
                            if (rowIndex % 2 == 0)
                                resources.getColor(R.color.white, null)
                            else
                                resources.getColor(R.color.background, null)
                        )
                        setTextColor(resources.getColor(R.color.text_primary, null))
                    }

                    // Center align
                    gravity = Gravity.CENTER

                    // Add cell border
                    setOnTouchListener { _, _ ->
                        false
                    }
                }

                tableRow.addView(cellView)
            }

            distributionTable.addView(tableRow)
        }
    }
}