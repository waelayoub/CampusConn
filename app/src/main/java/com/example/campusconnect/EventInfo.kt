package com.example.campusconnect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.campusconnect.databinding.FragmentEventInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*
import java.text.SimpleDateFormat


class EventInfo(private val eventName: String,
                private val eventDate: String,
                private val eventTime: String,
                private val eventLocation: String,
                private val eventOrganizer: String,
                private val eventType: String,
                private val eventCapacity: String,
                private val eventDescription: String,
                private val eventFlyer: String,
                private val eventIcon: String,
                private val eventId: String) : Fragment() {

    private lateinit var binding: FragmentEventInfoBinding
    private val auth: FirebaseAuth = Firebase.auth
    private val dbref: DatabaseReference=FirebaseDatabase.getInstance().getReference("registrations")



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentEventInfoBinding.inflate(inflater, container, false)
        dbref.child(eventId).get().addOnSuccessListener {
            if(it.child(auth.currentUser!!.uid).exists()){
                binding.registerButton.text="Registered"
                binding.registerButton.isEnabled=false
                // Here to add all modifications if event is registered (@Ralph)
            }
        }
        binding.eventNameInfoID.setText(eventName)
        binding.eventTimeInfoID.setText(eventTime)
        binding.eventDescriptionInfoID.setText(eventDescription)
        binding.eventLocationInfoID.setText(eventLocation)
        binding.eventDateInfoID.setText(eventDate)
        Glide.with(requireContext()).load(eventFlyer).into(binding.eventFlyerInfoID)
        Glide.with(requireContext()).load(eventIcon).into(binding.eventLogoInfoID)

        binding.closebtn.setOnClickListener{
            val activity=it!!.context as AppCompatActivity
            activity.supportFragmentManager.beginTransaction().apply{
                val popUp=Home()
                replace(R.id.frame_layout,popUp).commit()
            }
        }

        binding.registerButton.setOnClickListener {


            val userId = auth.currentUser!!.uid

            // adding time
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateString = dateFormat.format(Date())
            // end time

            val dbRef = FirebaseDatabase.getInstance().getReference("registrations").child(eventId)
            dbRef.child(userId).setValue(dateString)
                .addOnSuccessListener {
                    println("Success")
                    binding.registerButton.text="Registered"
                    binding.registerButton.isEnabled=false
                    // (@Ralph) change also ui for button if newly registered
                }
                .addOnFailureListener {
                    println("Fail")
                }


            println("The current user is: "+auth.currentUser!!.uid)
            println("The current event is : "+eventId)
        }

        return binding.root
    }

}