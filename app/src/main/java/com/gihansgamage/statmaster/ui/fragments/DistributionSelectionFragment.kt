package com.gihansgamage.statmaster.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gihansgamage.statmaster.R
import com.gihansgamage.statmaster.databinding.FragmentDistributionSelectionBinding
import com.gihansgamage.statmaster.models.DistributionType
import com.google.android.material.card.MaterialCardView

/**
 * Fragment for selecting a statistical distribution type.
 * Displays cards for each distribution type with icons and descriptions.
 */
class DistributionSelectionFragment : Fragment() {

    private var _binding: FragmentDistributionSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDistributionSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDistributionCards()
    }

    private fun setupDistributionCards() {
        // Normal Distribution Card
        binding.cardNormal.setOnClickListener {
            navigateToCalculator(DistributionType.NORMAL)
        }

        // t-Distribution Card
        binding.cardT.setOnClickListener {
            navigateToCalculator(DistributionType.T)
        }

        // Chi-square Distribution Card
        binding.cardChiSquare.setOnClickListener {
            navigateToCalculator(DistributionType.CHISQUARE)
        }

        // F Distribution Card
        binding.cardF.setOnClickListener {
            navigateToCalculator(DistributionType.F)
        }
    }

    private fun navigateToCalculator(distribution: DistributionType) {
        // Pass the selected distribution to CalculatorFragment
        val bundle = Bundle().apply {
            putString("DISTRIBUTION_TYPE", distribution.name)
        }

        // Navigate to CalculatorFragment with arguments
        findNavController().navigate(
            R.id.action_distributionSelection_to_calculator,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}