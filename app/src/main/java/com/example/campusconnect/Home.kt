package com.example.campusconnect

import android.app.Activity
import android.app.Application
import android.os.Bundle


import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.databinding.FragmentHomeBinding
import com.google.firebase.database.*


class Home : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var dbref: DatabaseReference
    private lateinit var adapter: EventModelAdapter
    private lateinit var eventRecyclerView: RecyclerView

    private var eventlist= arrayListOf<EventModel>()



    private fun getEventData(){
        dbref= FirebaseDatabase.getInstance().getReference("Events")


        dbref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val event = snapshot.getValue(EventModel::class.java)
                event!!.eventId = snapshot.key
                eventlist.add(event!!)
                adapter.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val event = snapshot.getValue(EventModel::class.java)
                event!!.eventId = snapshot.key

                for (i in eventlist.indices) {
                    if (eventlist[i].eventId == event.eventId) {
                        eventlist[i] = event
                        adapter.notifyItemChanged(i)
                        break
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val event = snapshot.getValue(EventModel::class.java)
                event!!.eventId = snapshot.key

                for (i in eventlist.indices) {
                    if (eventlist[i].eventId == event.eventId) {
                        eventlist.removeAt(i)
                        adapter.notifyItemRemoved(i)
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

        eventRecyclerView = binding.eventlist
        adapter = EventModelAdapter(requireContext(),eventlist)

        eventRecyclerView.layoutManager=LinearLayoutManager(context)
        eventRecyclerView.setHasFixedSize(true)
        eventRecyclerView.adapter=adapter

        getEventData()

        return binding.root
    }

}