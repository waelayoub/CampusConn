package com.example.campusconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.campusconnect.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private val homeFragment = Home()
    private val myEventsFragment=MyEvents()
    private val settingsFragment=Settings()

    private lateinit var mAddFab: FloatingActionButton
    private lateinit var mAddAlarmFab: FloatingActionButton
    private lateinit var mAddPersonFab: FloatingActionButton
    private lateinit var userName:String

    private var isAllFabsVisible: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("is_dark_mode_enabled", false)
        val mode = if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

        binding=ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_CampusConnect)
        setContentView(binding.root)

        userName=intent.getStringExtra("name").toString()
        mAddFab = binding.addFab

        // FAB button
        mAddAlarmFab = binding.addAlarmFab
        mAddPersonFab = binding.addPersonFab

        // Also register the action name text, of all the FABs.



//        mAddAlarmFab.visibility = View.GONE
//        mAddPersonFab.visibility = View.GONE
//        addAlarmActionText.visibility = View.GONE
//        addPersonActionText.visibility = View.GONE

        // make the boolean variable as false, as all the
        // action name texts and all the sub FABs are invisible
        isAllFabsVisible = false
        if(intent.getStringExtra("user")=="admin"){
            mAddFab.visibility=View.VISIBLE
        }


        mAddFab.setOnClickListener(View.OnClickListener {
            (if (!isAllFabsVisible!!) {
                // when isAllFabsVisible becomes true make all
                // the action name texts and FABs VISIBLE
                mAddAlarmFab.show()
                mAddPersonFab.show()


                // make the boolean variable true as we
                // have set the sub FABs visibility to GONE
                true
            } else {
                // when isAllFabsVisible becomes true make
                // all the action name texts and FABs GONE.
                mAddAlarmFab.hide()
                mAddPersonFab.hide()


                // make the boolean variable false as we
                // have set the sub FABs visibility to GONE
                false
            }).also { isAllFabsVisible = it }
        })
        // below is the sample action to handle add person FAB. Here it shows simple Toast msg.
        // The Toast will be shown only when they are visible and only when user clicks on them
        mAddPersonFab.setOnClickListener {
            val intent = Intent(this, InsertionActivity::class.java)
            intent.putExtra("name", userName)
            startActivity(intent)
            finish()
        }

        // below is the sample action to handle add alarm FAB. Here it shows simple Toast msg
        // The Toast will be shown only when they are visible and only when user clicks on them
        mAddAlarmFab.setOnClickListener {
            val intent = Intent(this, FetchingActivity::class.java)
            intent.putExtra("name", userName)
            startActivity(intent)
            finish()
        }




        window.enterTransition = null
        window.exitTransition = null

        if (theme_object.themebool){

            replaceFragment(settingsFragment)
            theme_object.themebool=false
        }else{
            replaceFragment(homeFragment)
        }

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


