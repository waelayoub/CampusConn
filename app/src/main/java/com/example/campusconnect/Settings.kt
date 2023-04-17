package com.example.campusconnect


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.campusconnect.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Settings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentSettingsBinding.inflate(inflater, container, false)

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemInDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("is_dark_mode_enabled", isSystemInDarkMode)

        // Update the switch state
        binding.themeSwitch.isChecked = isDarkModeEnabled

        // Set the app theme based on the saved preference or system default
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateTheme(isChecked)

        }

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
        // Save the user's preference for theme mode in shared preferences
        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("is_dark_mode_enabled", isDarkModeEnabled).apply()

        // Set the app theme based on the saved preference
        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val currentFragmentTag ="SETTINGS"

        println("current frag tag: "+currentFragmentTag)

        prefs.edit().putString("currentFragmentTag", currentFragmentTag).apply()
        // Recreate the current activity to apply the new theme
        requireActivity().recreate()

    }









}