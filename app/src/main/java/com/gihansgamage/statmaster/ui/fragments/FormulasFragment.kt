package com.gihansgamage.statmaster.ui.fragments

import androidx.fragment.app.Fragment // Ensure this is the import
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gihansgamage.statmaster.R

class FormulasFragment : Fragment() { // Ensure it extends Fragment()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_formulas, container, false)
    }
}