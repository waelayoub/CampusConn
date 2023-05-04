package com.example.campusconnect


import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.example.campusconnect.databinding.FragmentSettingsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class Settings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var activityMain:AppCompatActivity
    private val dbrefReg = FirebaseDatabase.getInstance().getReference("registrations")
    private val dbrefActive= FirebaseDatabase.getInstance().getReference("Active")
    private val dbrefEvent= FirebaseDatabase.getInstance().getReference("Events")
    private lateinit var eventListener: ChildEventListener
    private lateinit var activeListener: ChildEventListener

    private lateinit var con:Context
    private var eventlist= arrayListOf<EventModel>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentSettingsBinding.inflate(inflater, container, false)
        con=requireContext()
        activityMain = context as AppCompatActivity
        if (activityMain.intent.getStringExtra("user")=="admin"){
            activityMain.findViewById<FloatingActionButton>(R.id.add_fab).visibility=View.VISIBLE

        }

        activeListener=object :ChildEventListener{

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val eventRef = dbrefEvent.child(snapshot.key!!)
                eventRef.get().addOnSuccessListener { eventSnapshot ->
                    if (eventSnapshot.exists()) {
                        val event = eventSnapshot.getValue(EventModel::class.java)
                        event!!.eventId = eventSnapshot.key
                        eventlist.add(event!!)

                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedKey = snapshot.key
                for ((index, event) in eventlist.withIndex()) {
                    if (event.eventId == removedKey) {
                        eventlist.removeAt(index)
                        break
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }

        eventListener=object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val event = snapshot.getValue(EventModel::class.java)
                event!!.eventId = snapshot.key
                for (i in eventlist.indices) {
                    if (eventlist[i].eventId == event.eventId) {
                        if (eventlist[i].eventWarning!=event.eventWarning && event.eventWarning!=0){
                            val registeredToEvent = dbrefReg.child(event.eventId!!).child(auth.currentUser!!.uid)

                            registeredToEvent.get().addOnSuccessListener {
                                    task ->
                                if (task.exists()){

                                    val notificationId = 1
                                    val builder = NotificationCompat.Builder(con, "myFirebaseChannel")
                                        .setSmallIcon(R.drawable.baseline_notifications_active_24)
                                        .setContentTitle("Fire Detected")
                                        .setContentText("Fire alarm went on")
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    with(NotificationManagerCompat.from(con)) {
                                        if (ActivityCompat.checkSelfPermission(
                                                con,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            println("no permission")
                                            // TODO: Consider calling
                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.

                                        }
                                        notify(notificationId, builder.build())
                                    }

                                    try {
                                        val builder = AlertDialog.Builder(con)
                                        builder.setMessage("Warning: In one event you registered, the fire alarm has been turned on")
                                            .setCancelable(false)
                                            .setPositiveButton("OK") { dialog, id ->
                                                // do something when the OK button is clicked
                                            }
                                        val alert = builder.create()
                                        alert.show()
                                    }
                                    catch (e:Exception){
                                        println("Can't Display the dialog")
                                    }

                                }
                            }
                        }
                        eventlist[i] = event
                        break
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        }
        dbrefEvent.addChildEventListener(eventListener)
        dbrefActive.addChildEventListener(activeListener)


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
            FireAlarmWarning.triggered=false
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
        dbrefEvent.removeEventListener(eventListener)
        dbrefActive.removeEventListener(activeListener)
    }




}