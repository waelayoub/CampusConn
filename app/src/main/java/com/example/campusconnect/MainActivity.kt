package com.example.campusconnect

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.campusconnect.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private val homeFragment = Home()
    private val myEventsFragment=MyEvents()
    private val settingsFragment=Settings()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("is_dark_mode_enabled", false)
        val mode = if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

        binding=ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_CampusConnect)
        setContentView(binding.root)

        //

        // Restore the current fragment tag from shared preferences
        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val currentFragmentTag = prefs.getString("currentFragmentTag", "")
        // Navigate to the corresponding fragment
        if (!currentFragmentTag.isNullOrEmpty()) {
            replaceFragment(settingsFragment)
            prefs.edit().putString("currentFragmentTag", "").apply()
        } else {
            replaceFragment(homeFragment)
        }


        //
        //replaceFragment(homeFragment)

        val bottomNav=findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.MainPage->replaceFragment(homeFragment)
                R.id.MyEventsPage->replaceFragment(myEventsFragment)
                R.id.SettingsPage->replaceFragment(settingsFragment)
            }
            true
        }
    }


    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {

            replace(R.id.frame_layout,fragment)
            commit()
        }

    }



}


