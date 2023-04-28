package com.example.campusconnect

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*


class Home : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var dbref: DatabaseReference
    private lateinit var dbrefEvent:DatabaseReference
    private lateinit var dbrefReg:DatabaseReference

    private lateinit var adapter: EventModelAdapter
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var con: Context

    private val auth: FirebaseAuth = Firebase.auth


    private var eventlist= arrayListOf<EventModel>()
    private lateinit var searchView:androidx.appcompat.widget.SearchView
    private var searchList= arrayListOf<EventModel>()



    private fun getEventData(){

        dbref= FirebaseDatabase.getInstance().getReference("Active")
        dbrefEvent= FirebaseDatabase.getInstance().getReference("Events")
        dbrefReg= FirebaseDatabase.getInstance().getReference("registrations")



        dbref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val eventRef = dbrefEvent.child(snapshot.key!!)
                eventRef.get().addOnSuccessListener {
                    eventSnapshot ->
                    if (eventSnapshot.exists()){
                            val event = eventSnapshot.getValue(EventModel::class.java)
                            event!!.eventId = eventSnapshot.key
                            eventlist.add(event!!)
                            eventlist.sortBy {
                                SimpleDateFormat("dd/MM/yyyy HH:mm").parse(it.eventDate+" "+it.eventTime).time
                            }
                            adapter.isShimmer=false

                                //

                            val searchText=searchView.query.toString()!!.toLowerCase(Locale.getDefault())
                            if (searchText.isNotEmpty()){

                                if (event.eventName?.toLowerCase(Locale.getDefault())?.contains(searchText) == true){
                                        searchList.add(event)
                                }

                                adapter = EventModelAdapter(con,searchList,false)
                                adapter.isShimmer=false
                                eventRecyclerView.adapter=adapter

                            }else{

                                searchList.clear()
                                searchList.addAll(eventlist)
                                adapter.notifyDataSetChanged()

                            }

                        if (event.eventWarning==1 && !FireAlarmWarning.triggered ){
                            val registeredToEvent = dbrefReg.child(event.eventId!!).child(auth.currentUser!!.uid)
                            registeredToEvent.get().addOnSuccessListener {
                                task ->
                                if (task.exists()){
                                    FireAlarmWarning.triggered=true
                                    val builder = AlertDialog.Builder(this@Home.context)
                                    builder.setMessage("Warning: In one event you registered, the fire alarm has been turned on")
                                        .setCancelable(false)
                                        .setPositiveButton("OK") { dialog, id ->
                                            // do something when the OK button is clicked
                                        }
                                    val alert = builder.create()
                                    alert.show()
                                }
                            }



                        }


                            //
                            //adapter.notifyDataSetChanged()


                    }

                }





            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val event = snapshot.key

                for (i in eventlist.indices) {
                    if (eventlist[i].eventId == event) {
                        eventlist.removeAt(i)
                        adapter.isShimmer=false


                        searchList.clear()
                        val searchText=searchView.query.toString()!!.toLowerCase(Locale.getDefault())
                        if (searchText.isNotEmpty()){
                            eventlist.forEach{
                                if (it.eventName?.toLowerCase(Locale.getDefault())?.contains(searchText) == true){
                                    searchList.add(it)
                                }
                            }
                            adapter = EventModelAdapter(con,searchList,false)
                            adapter.isShimmer=false
                            eventRecyclerView.adapter=adapter

                        }else{
                            searchList.clear()
                            adapter.notifyDataSetChanged()

                        }

                        break
                    }
                }

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        dbrefEvent.addChildEventListener(object : ChildEventListener {
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
                                    val builder = AlertDialog.Builder(con)
                                    builder.setMessage("Warning: In one event you registered, the fire alarm has been turned on")
                                        .setCancelable(false)
                                        .setPositiveButton("OK") { dialog, id ->
                                            // do something when the OK button is clicked
                                        }
                                    val alert = builder.create()
                                    alert.show()
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
                for (i in searchList.indices) {
                    if (searchList[i].eventId == event.eventId) {
                        searchList[i] = event

                        //adapter = EventModelAdapter(con,eventlist,false)
                        adapter.isShimmer=false
                        //eventRecyclerView.adapter=adapter

                        break
                    }
                }

                adapter.notifyDataSetChanged()

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val event = snapshot.getValue(EventModel::class.java)
                event!!.eventId = snapshot.key

                for (i in eventlist.indices) {
                    if (eventlist[i].eventId == event.eventId) {
                        eventlist.removeAt(i)
                        //adapter = EventModelAdapter(con,eventlist,false)
                        adapter.isShimmer=false
                        adapter.notifyDataSetChanged()
                        //eventRecyclerView.adapter=adapter
                        break
                    }
                }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentHomeBinding.inflate(inflater, container, false)
        eventlist.clear()
        searchView=binding.searchview
        adapter = EventModelAdapter(requireContext(),eventlist,false)
        eventRecyclerView = binding.eventlist
        eventRecyclerView.layoutManager=LinearLayoutManager(context)
        eventRecyclerView.setHasFixedSize(true)
        eventRecyclerView.adapter=adapter
        con=requireContext()
        val activityMain = con as AppCompatActivity
        activityMain.findViewById<FloatingActionButton>(R.id.add_fab).visibility=View.INVISIBLE
        activityMain.findViewById<FloatingActionButton>(R.id.add_alarm_fab).visibility=View.INVISIBLE
        activityMain.findViewById<FloatingActionButton>(R.id.add_person_fab).visibility=View.INVISIBLE
        getEventData()
        searchListener()
        return binding.root
    }
    private fun searchListener(){
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
                }else{
                    searchList.clear()
                    searchList.addAll(eventlist)
                }
                adapter = EventModelAdapter(con,searchList,false)
                adapter.isShimmer=false
                eventRecyclerView.adapter=adapter
                return false
            }
        })
    }
}