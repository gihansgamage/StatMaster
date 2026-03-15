package com.gihansgamage.statmaster.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gihansgamage.statmaster.R
import com.gihansgamage.statmaster.databinding.FragmentDistributionSelectionBinding
import com.gihansgamage.statmaster.models.DistributionType

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
        binding.cardNormal.setOnClickListener {
            navigateToCalculator(DistributionType.NORMAL)
        }

        binding.cardT.setOnClickListener {
            navigateToCalculator(DistributionType.T)
        }

        // Note: XML id is card_chi_square → binding name is cardChiSquare
        binding.cardChiSquare.setOnClickListener {
            navigateToCalculator(DistributionType.CHISQUARE)
        }

        binding.cardF.setOnClickListener {
            navigateToCalculator(DistributionType.F)
        }
    }

    private fun navigateToCalculator(distribution: DistributionType) {
        val fragment = CalculatorFragment().apply {
            arguments = Bundle().apply {
                putString("DISTRIBUTION_TYPE", distribution.name)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}