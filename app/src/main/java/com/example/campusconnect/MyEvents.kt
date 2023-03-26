package com.example.campusconnect

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.databinding.FragmentMyEventsBinding
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



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMyEventsBinding.inflate(inflater, container, false)

        eventlist.clear()

        eventRecyclerView = binding.myEventsScroll
        adapter = EventModelAdapter(requireContext(),eventlist)

        eventRecyclerView.layoutManager= LinearLayoutManager(context)
        eventRecyclerView.setHasFixedSize(true)
        eventRecyclerView.adapter=adapter

        getEventData()

        return binding.root
    }


    private fun getEventData() {

        val uid:String=auth.currentUser!!.uid

        dbrefReg.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                println("I am in child Added")

                if (snapshot.child(uid).exists()) {
                    val eventRef = dbrefEvent.child(snapshot.key!!)
                    eventRef.get().addOnSuccessListener { eventSnapshot ->
                        if (eventSnapshot.exists()) {
                            val eventData = eventSnapshot.getValue(EventModel::class.java)
                            eventData!!.eventId=eventSnapshot.key
                            eventlist.add(eventData!!)
                            eventRecyclerView.adapter?.notifyDataSetChanged()
                            // Process the data as needed
                        }
                    }.addOnFailureListener { exception ->
                        // Handle any errors that occur
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                println("I am in child changed")
                val removedKey = snapshot.key
                for ((index, event) in eventlist.withIndex()) {
                    if (event.eventId == removedKey) {
                        eventlist.removeAt(index)
                        adapter=EventModelAdapter(requireContext(),eventlist)
                        eventRecyclerView.adapter=adapter
                        eventRecyclerView.adapter?.notifyDataSetChanged()
                        break
                    }
                }

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                println("I am in child removed")
                val removedKey = snapshot.key
                for ((index, event) in eventlist.withIndex()) {
                    if (event.eventId == removedKey) {
                        eventlist.removeAt(index)
                        adapter=EventModelAdapter(requireContext(),eventlist)
                        eventRecyclerView.adapter=adapter
                        eventRecyclerView.adapter?.notifyDataSetChanged()
                        break
                    }
                }

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Implement code to handle movement of data
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors that occur
            }
        })

    }


}