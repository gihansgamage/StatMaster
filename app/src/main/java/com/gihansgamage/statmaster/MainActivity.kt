package com.gihansgamage.statmaster

import android.app.AlertDialog
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

        // Hide default ActionBar (prevents duplicate toolbar)
        supportActionBar?.hide()

        initViews()
        setupBottomNavigation()

        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(CalculatorFragment())
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_info -> {
                showAppInfoDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Set up toolbar with info icon
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable back button if needed
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = ""
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

    private fun showAppInfoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_app_info, null)

        AlertDialog.Builder(this, R.style.AppInfoDialog)
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}