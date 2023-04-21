package com.example.campusconnect

import android.content.Intent
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.databinding.FragmentMyEventsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*


class MyEvents : Fragment() {

    private lateinit var binding: FragmentMyEventsBinding

    private lateinit var adapter: EventModelAdapter
    private lateinit var eventRecyclerView: RecyclerView
    private val auth = Firebase.auth

    private var eventlist= arrayListOf<EventModel>()
    private val dbrefReg = FirebaseDatabase.getInstance().getReference("registrations")
    private val dbrefEvent= FirebaseDatabase.getInstance().getReference("Events")
    private val dbrefActive= FirebaseDatabase.getInstance().getReference("Active")

    private lateinit var searchView:androidx.appcompat.widget.SearchView
    private var searchList= arrayListOf<EventModel>()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMyEventsBinding.inflate(inflater, container, false)

        eventlist.clear()

        eventRecyclerView = binding.myEventsScroll
        adapter = EventModelAdapter(requireContext(),eventlist,true)
        searchView=binding.Mysearchview

        eventRecyclerView.layoutManager= LinearLayoutManager(context)
        eventRecyclerView.setHasFixedSize(true)
        eventRecyclerView.adapter=adapter

        getEventData1()
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object:androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchList.clear()
                val searchText=newText!!.toLowerCase(Locale.getDefault())
                if (searchText.isNotEmpty()){
                    eventlist.forEach{
                        if (it.eventName?.toLowerCase(Locale.getDefault())?.contains(searchText) == true){
                            searchList.add(it)
                        }
                    }
                    eventRecyclerView.adapter!!.notifyDataSetChanged()
                }else{
                    searchList.clear()
                    searchList.addAll(eventlist)
                    eventRecyclerView.adapter!!.notifyDataSetChanged()
                }
                return false
            }


        })


        return binding.root
    }


    private fun getEventData1(){
        val uid:String=auth.currentUser!!.uid


        dbrefReg.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                dbrefActive.child(snapshot.key!!).get().addOnSuccessListener {
                    task ->
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

                                    searchList.add(eventData!!)
                                    adapter = EventModelAdapter(requireContext(), searchList, true)
                                    adapter.isShimmer = false
                                    eventRecyclerView.adapter = adapter



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
                        //adapter=EventModelAdapter(requireContext(),eventlist,true)
                        //eventRecyclerView.adapter=adapter
                        //break
                    }
                }
                for ((index, event) in searchList.withIndex()) {
                    if (event.eventId == removedKey) {
                        searchList.removeAt(index)
                        adapter=EventModelAdapter(requireContext(),searchList,true)
                        eventRecyclerView.adapter=adapter

                    }
                }

                //adapter.isShimmer=false
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                println("I am in child removed")
                val removedKey = snapshot.key
                for ((index, event) in eventlist.withIndex()) {
                    if (event.eventId == removedKey) {
                        eventlist.removeAt(index)
                        //adapter=EventModelAdapter(requireContext(),eventlist,true)
                        //eventRecyclerView.adapter=adapter
                        //break
                    }
                }
                for ((index, event) in searchList.withIndex()) {
                    if (event.eventId == removedKey) {
                        searchList.removeAt(index)
                        adapter=EventModelAdapter(requireContext(),searchList,true)
                        eventRecyclerView.adapter=adapter

                    }
                }
                //adapter.isShimmer=false

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Implement code to handle movement of data
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors that occur
            }
        })

        dbrefActive.
                addChildEventListener(object :ChildEventListener{
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
                                //adapter=EventModelAdapter(requireContext(),eventlist,true)
                                //eventRecyclerView.adapter=adapter
                                //break
                            }
                        }
                        for ((index, event) in searchList.withIndex()) {
                            if (event.eventId == removedKey) {
                                searchList.removeAt(index)
                                adapter=EventModelAdapter(requireContext(),searchList,true)
                                eventRecyclerView.adapter=adapter

                            }
                        }
                        //adapter.isShimmer=false
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })



//        dbrefReg.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//
//
//                dbrefActive.child(snapshot.key!!).get().addOnSuccessListener {
//
//                    if (snapshot.child(uid).exists()) {
//                        val eventRef = dbrefEvent.child(snapshot.key!!)
//                        eventRef.get().addOnSuccessListener { eventSnapshot ->
//                            if (eventSnapshot.exists()) {
//                                val eventData = eventSnapshot.getValue(EventModel::class.java)
//                                eventData!!.eventId=eventSnapshot.key
//                                eventlist.add(eventData!!)
//                                eventRecyclerView.adapter?.notifyDataSetChanged()
//                                // Process the data as needed
//                            }
//                        }.addOnFailureListener { exception ->
//                            // Handle any errors that occur
//                        }
//                    }
//                    adapter.isShimmer=false
//                }
//
//
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                println("I am in child changed")
//
//                val removedKey = snapshot.key
//                for ((index, event) in eventlist.withIndex()) {
//                    if (event.eventId == removedKey) {
//                        eventlist.removeAt(index)
//                        adapter=EventModelAdapter(requireContext(),eventlist,true)
//                        eventRecyclerView.adapter=adapter
//                        break
//                    }
//                }
//
//                adapter.isShimmer=false
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {
//                println("I am in child removed")
//                val removedKey = snapshot.key
//                for ((index, event) in eventlist.withIndex()) {
//                    if (event.eventId == removedKey) {
//                        eventlist.removeAt(index)
//                        adapter=EventModelAdapter(requireContext(),eventlist,true)
//                        eventRecyclerView.adapter=adapter
//                        eventRecyclerView.adapter?.notifyDataSetChanged()
//                        break
//                    }
//                }
//                adapter.isShimmer=false
//
//            }
//
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//                // Implement code to handle movement of data
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle any errors that occur
//            }
//        })
//
//        dbrefActive.addChildEventListener(object : ChildEventListener{
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                println("I am in child added 2nd listener")
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                println("I am in child changed 2nd listener")
//
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {
//                println("I am in child removed 2nd listener")
//                val removedKey = snapshot.key
//                for ((index, event) in eventlist.withIndex()) {
//                    if (event.eventId == removedKey) {
//                        eventlist.removeAt(index)
//                        adapter=EventModelAdapter(requireContext(),eventlist,true)
//                        eventRecyclerView.adapter=adapter
//                        eventRecyclerView.adapter?.notifyDataSetChanged()
//                        break
//                    }
//                }
//                adapter.isShimmer=false
//            }
//
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//        })

    }





//    private fun getEventData() {
//
//        val uid:String=auth.currentUser!!.uid
//
//        dbrefReg.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
////                println("I am in child Added")
//                if (snapshot.child(uid).exists()) {
//                    val eventRef = dbrefEvent.child(snapshot.key!!)
//                    eventRef.get().addOnSuccessListener { eventSnapshot ->
//                        if (eventSnapshot.exists()) {
//                            val eventData = eventSnapshot.getValue(EventModel::class.java)
//                            eventData!!.eventId=eventSnapshot.key
//                            eventlist.add(eventData!!)
//                            eventRecyclerView.adapter?.notifyDataSetChanged()
//                            // Process the data as needed
//                        }
//                    }.addOnFailureListener { exception ->
//                        // Handle any errors that occur
//                    }
//                }
//                adapter.isShimmer=false
//
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                println("I am in child changed")
//
//                val removedKey = snapshot.key
//                for ((index, event) in eventlist.withIndex()) {
//                    if (event.eventId == removedKey) {
//                        eventlist.removeAt(index)
//                        adapter=EventModelAdapter(requireContext(),eventlist,true)
//                        eventRecyclerView.adapter=adapter
//                        eventRecyclerView.adapter?.notifyDataSetChanged()
//                        break
//                    }
//                }
//
//                adapter.isShimmer=false
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {
//                println("I am in child removed")
//                val removedKey = snapshot.key
//                for ((index, event) in eventlist.withIndex()) {
//                    if (event.eventId == removedKey) {
//                        eventlist.removeAt(index)
//                        adapter=EventModelAdapter(requireContext(),eventlist,true)
//                        eventRecyclerView.adapter=adapter
//                        eventRecyclerView.adapter?.notifyDataSetChanged()
//                        break
//                    }
//                }
//                adapter.isShimmer=false
//
//            }
//
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//                // Implement code to handle movement of data
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle any errors that occur
//            }
//        })
//
//    }




}