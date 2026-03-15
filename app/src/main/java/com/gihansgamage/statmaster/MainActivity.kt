package com.gihansgamage.statmaster

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.gihansgamage.statmaster.ui.fragments.CalculatorFragment
import com.gihansgamage.statmaster.ui.fragments.FormulasFragment
import com.gihansgamage.statmaster.ui.fragments.TableFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupBottomNavigation()

        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(CalculatorFragment())
        }
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calculator -> {
                    loadFragment(CalculatorFragment())
                    true
                }
                R.id.nav_tables -> {
                    loadFragment(TableFragment())
                    true
                }
                R.id.nav_formulas -> {
                    loadFragment(FormulasFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}