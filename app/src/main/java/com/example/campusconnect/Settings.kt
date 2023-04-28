package com.example.campusconnect


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.campusconnect.databinding.FragmentSettingsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Settings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var activityMain:AppCompatActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentSettingsBinding.inflate(inflater, container, false)
        activityMain = context as AppCompatActivity
        if (activityMain.intent.getStringExtra("user")=="admin"){
            activityMain.findViewById<FloatingActionButton>(R.id.add_fab).visibility=View.VISIBLE

        }


//        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
//        val isSystemInDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES


        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("is_dark_mode_enabled", false)

        // Update the switch state
        binding.themeSwitch.isChecked = isDarkModeEnabled

        // Set the app theme based on the saved preference or system default
//        if (isDarkModeEnabled) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        }

        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateTheme(isChecked)

        }

        binding.tvUserName.text= activity?.intent!!.getStringExtra("name")
        binding.tvUserMail.text =auth.currentUser!!.email


        println("I am in Settings the email is: "+ auth.currentUser!!.email )

        binding.SignOutButton.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            GlobalScope.launch(Dispatchers.IO) {
                MS_Account_Object.mSingleAccountApp!!.signOut()
            }

            val intent = Intent(requireContext(), Splash_Screen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
           }
        return binding.root
    }

    private fun updateTheme(isDarkModeEnabled: Boolean) {
        println("im in updatetheme")
        theme_object.themebool=true
        // Save the user's preference for theme mode in shared preferences
        val sharedPreferences =
            requireActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("is_dark_mode_enabled", isDarkModeEnabled).apply()

        // Set the app theme based on the saved preference
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        activityMain.findViewById<FloatingActionButton>(R.id.add_fab).visibility=View.INVISIBLE

    }





}