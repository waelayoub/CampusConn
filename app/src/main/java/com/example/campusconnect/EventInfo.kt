package com.example.campusconnect

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.example.campusconnect.databinding.FragmentEventInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import java.text.SimpleDateFormat


class EventInfo(private var eventName: String,
                private var eventDate: String,
                private var eventTime: String,
                private var eventLocation: String,
                private val eventOrganizer: String,
                private val eventType: String,
                private val eventCapacity: String,
                private var eventDescription: String,
                private val eventFlyer: String,
                private val eventIcon: String,
                private val eventId: String,
                private val homeOrigin:Boolean) : Fragment() {

    private lateinit var binding: FragmentEventInfoBinding
    private val auth: FirebaseAuth = Firebase.auth
    private val dbref: DatabaseReference=FirebaseDatabase.getInstance().getReference("registrations")
    private val dbrefActive= FirebaseDatabase.getInstance().getReference("Active")
    private val dbrefEvent= FirebaseDatabase.getInstance().getReference("Events")
    private lateinit var eventListener: ChildEventListener
    private lateinit var activeListener: ChildEventListener

    private lateinit var con: Context
    private var eventlist= arrayListOf<EventModel>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentEventInfoBinding.inflate(inflater, container, false)
        dbref.child(eventId).get().addOnSuccessListener {
            if(it.child(auth.currentUser!!.uid).exists()){
                binding.registerButton.text="Subscribed"
                binding.registerButton.isEnabled=false

            }
        }
        con=requireContext()
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
                if (!homeOrigin) {
                    val popUp = Home()
                    replace(R.id.frame_layout, popUp).commit()
                }else{
                    val popUp = MyEvents()
                    replace(R.id.frame_layout, popUp).commit()
                }
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
                    binding.registerButton.text="Subscribed"
                    binding.registerButton.isEnabled=false

                }
                .addOnFailureListener {
                    println("Fail")
                }

            println(eventId)
            FirebaseMessaging.getInstance().subscribeToTopic(eventId)
                .addOnCompleteListener { task ->
                    var msg = "Done"
                    if (!task.isSuccessful) {
                        msg = "Failed"
                    }

                    println(msg)
                }.addOnFailureListener{
                    println("Action Failed")
                }
            //println("The current user is: "+auth.currentUser!!.uid)
            //println("The current event is : "+eventId)
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
                if (event.eventId == eventId){
                    eventName=event.eventName.toString()
                     eventDate=event.eventDate.toString()
                    eventTime=event.eventTime.toString()
                     eventLocation=event.eventLocation.toString()
                    eventDescription=event.eventDescription.toString()
                    binding.eventNameInfoID.setText(eventName)
                    binding.eventTimeInfoID.setText(eventTime)
                    binding.eventDescriptionInfoID.setText(eventDescription)
                    binding.eventLocationInfoID.setText(eventLocation)
                    binding.eventDateInfoID.setText(eventDate)
                }
                for (i in eventlist.indices) {
                    if (eventlist[i].eventId == event.eventId) {
                        if (eventlist[i].eventWarning!=event.eventWarning && event.eventWarning!=0){
                            val registeredToEvent = dbref.child(event.eventId!!).child(auth.currentUser!!.uid)

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

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        dbrefEvent.removeEventListener(eventListener)
        dbrefActive.removeEventListener(activeListener)
    }

}