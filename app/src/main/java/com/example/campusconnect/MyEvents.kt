package com.example.campusconnect

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.databinding.FragmentMyEventsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase


class MyEvents : Fragment() {

    private lateinit var binding: FragmentMyEventsBinding

    private lateinit var adapter: EventModelAdapter
    private lateinit var eventRecyclerView: RecyclerView
    private val auth = Firebase.auth

    private var eventlist= arrayListOf<EventModel>()
    private val dbrefReg = FirebaseDatabase.getInstance().getReference("registrations")
    private val dbrefEvent= FirebaseDatabase.getInstance().getReference("Events")
    private val dbrefActive= FirebaseDatabase.getInstance().getReference("Active")

    private lateinit var con:Context

    private lateinit var activeListener: ChildEventListener
    private lateinit var eventListener: ChildEventListener
    private lateinit var registrationListener: ChildEventListener



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMyEventsBinding.inflate(inflater, container, false)

        con= requireContext()
        val activityMain = con as AppCompatActivity
        activityMain.findViewById<FloatingActionButton>(R.id.add_fab).visibility=View.INVISIBLE
        activityMain.findViewById<FloatingActionButton>(R.id.add_alarm_fab).visibility=View.INVISIBLE
        activityMain.findViewById<FloatingActionButton>(R.id.add_person_fab).visibility=View.INVISIBLE

        eventlist.clear()

        eventRecyclerView = binding.myEventsScroll
        adapter = EventModelAdapter(requireContext(),eventlist,true)

        if(eventlist.size==0){
            adapter.isShimmer=false
        }
        eventRecyclerView.layoutManager= LinearLayoutManager(con)
        eventRecyclerView.setHasFixedSize(true)
        eventRecyclerView.adapter=adapter

        getEventData1()


        return binding.root
    }


    private fun getEventData1() {
        val uid: String = auth.currentUser!!.uid

        registrationListener = object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                dbrefActive.child(snapshot.key!!).get().addOnSuccessListener { task ->
                    if (task.exists()) {
                        println("I am in child added")
                        if (snapshot.child(uid).exists()) {
                            val eventRef = dbrefEvent.child(snapshot.key!!)
                            eventRef.get().addOnSuccessListener { eventSnapshot ->
                                if (eventSnapshot.exists()) {
                                    val eventData = eventSnapshot.getValue(EventModel::class.java)
                                    eventData!!.eventId = eventSnapshot.key
                                    println("event id is: " + eventSnapshot.key)
                                    eventlist.add(eventData!!)
                                    adapter = EventModelAdapter(con, eventlist, true)
                                    eventRecyclerView.adapter = adapter

                                    adapter.isShimmer = false

                                }
                            }
                        }
                    }

                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                println("I am in child changed")

                val removedKey = snapshot.key
                for ((index, event) in eventlist.withIndex()) {
                    if (event.eventId == removedKey) {
                        eventlist.removeAt(index)
                        adapter = EventModelAdapter(con, eventlist, true)
                        eventRecyclerView.adapter = adapter
                        break
                    }
                }

                adapter.isShimmer = false
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                println("I am in child removed")
                val removedKey = snapshot.key
                for ((index, event) in eventlist.withIndex()) {
                    if (event.eventId == removedKey) {
                        eventlist.removeAt(index)
                        adapter = EventModelAdapter(con, eventlist, true)
                        eventRecyclerView.adapter = adapter
                        break
                    }
                }
                adapter.isShimmer = false

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Implement code to handle movement of data
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors that occur
            }

        }

        activeListener=object :ChildEventListener{

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                println("added in active")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedKey = snapshot.key
                for ((index, event) in eventlist.withIndex()) {
                    if (event.eventId == removedKey) {
                        eventlist.removeAt(index)
                        adapter = EventModelAdapter(con, eventlist, true)
                        eventRecyclerView.adapter = adapter
                        break
                    }
                }
                adapter.isShimmer = false
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }

        eventListener=object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            }

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
                                    FireAlarmWarning.triggered=false


//                                    val notification=
//                                        FcmNotificationsSender("/topics/"+event.eventId, "Fire Detected", "Fire alarm went on", con, act)
//                                    notification.SendNotifications()
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
                        //adapter = EventModelAdapter(con,eventlist,false)
                        adapter.isShimmer=false
                        //eventRecyclerView.adapter=adapter

                        break
                    }
                }
//                adapter= EventModelAdapter(con,eventlist,true)
//                adapter.isShimmer=false
//                eventRecyclerView.adapter=adapter

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        }


        dbrefReg.addChildEventListener(registrationListener)

        dbrefActive.addChildEventListener(activeListener)

        dbrefEvent.addChildEventListener(eventListener)


    }

    override fun onDestroy() {
        super.onDestroy()
        println("Destroy in my events")
            dbrefActive.removeEventListener(activeListener);

            dbrefEvent.removeEventListener(eventListener)

            dbrefReg.removeEventListener(registrationListener)

    }


}