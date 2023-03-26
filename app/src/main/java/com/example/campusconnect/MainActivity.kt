package com.example.campusconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        binding=ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_CampusConnect)
        setContentView(binding.root)
        replaceFragment(homeFragment)
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


