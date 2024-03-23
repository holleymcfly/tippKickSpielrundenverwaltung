package de.herrmann.tippkick.spielrundenverwaltung

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.herrmann.tippkick.spielrundenverwaltung.databinding.ActivityMainBinding
import de.herrmann.tippkick.spielrundenverwaltung.util.PrintFragment
import de.herrmann.tippkick.spielrundenverwaltung.util.VersionInfoFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_teams, R.id.navigation_competition, R.id.navigation_play
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.navigation_version_info -> {
                showInfo()
                true
            }
            R.id.navigation_print -> {
                showPrint()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showInfo() {
        val infoDialog = VersionInfoFragment()
        infoDialog.isCancelable = false
        infoDialog.show(supportFragmentManager, null)
    }

    private fun showPrint() {
        val printDialog = PrintFragment()
        printDialog.isCancelable = false
        printDialog.show(supportFragmentManager, null)
    }
}