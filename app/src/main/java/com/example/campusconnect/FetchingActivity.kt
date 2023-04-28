package com.example.campusconnect

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class FetchingActivity : AppCompatActivity() {

    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var tvLoadingData: TextView
    private lateinit var eventList : ArrayList<EventModel>
    private lateinit var dbRef : DatabaseReference
    private lateinit var userName:String

    private fun getThis() : Context?{
        return this.applicationContext
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetching)


//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        eventRecyclerView = findViewById(R.id.rvEvent)
        tvLoadingData = findViewById(R.id.tvLoadingData)
        userName=intent.getStringExtra("name").toString()

        eventRecyclerView.layoutManager = LinearLayoutManager(this)
        eventRecyclerView.setHasFixedSize(true)

        eventList = arrayListOf()

        getEventsData()
    }
    override fun onBackPressed() {
        super.onBackPressed()

        val parentActivityIntent = Intent(this, MainActivity::class.java)
        parentActivityIntent.putExtra("user","admin")
        println("the name received: "+intent.getStringExtra("name"))
        parentActivityIntent.putExtra("name", intent.getStringExtra("name"))
        finish()
        startActivity(parentActivityIntent)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Navigate back to the parent activity
                onBackPressed()
                true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getEventsData() {
        eventRecyclerView.visibility = View.GONE
        tvLoadingData.visibility = View.VISIBLE

        dbRef = FirebaseDatabase.getInstance().getReference("Events")
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                eventList.clear()
                // println("The size of the list : " + eventList.size)
                if(snapshot.exists()){
                    for(eventSnap in snapshot.children){
                        println(" event key : " + eventSnap.key)
                        val eventData = eventSnap.getValue(EventModel :: class.java)
                        eventData!!.eventId =  eventSnap.key
                        println("evendata ::  " + eventData!!)
                        eventList.add(eventData!!)
                    }
                    val mAdapter = EventAdapter(eventList)
                    eventRecyclerView.adapter = mAdapter

                    mAdapter.setOnItemClickListener(object : EventAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val intent = Intent(getThis(), EventDetailsActivity::class.java)
                            intent.putExtra("name", userName)
                            intent.putExtra("eventId",eventList[position].eventId)
                            intent.putExtra("eventName",eventList[position].eventName)
                            intent.putExtra("eventDate",eventList[position].eventDate)
                            intent.putExtra("eventTime",eventList[position].eventTime)
                            intent.putExtra("eventLocation",eventList[position].eventLocation)
                            intent.putExtra("eventOrganizer",eventList[position].eventOrganizer)
                            intent.putExtra("eventType",eventList[position].eventType)
                            intent.putExtra("eventCapacity",eventList[position].eventCapacity)
                            intent.putExtra("eventDescription",eventList[position].eventDescription)
                            intent.putExtra("eventFlyer",eventList[position].eventFlyer)
                            intent.putExtra("eventIcon",eventList[position].eventIcon)
                            intent.putExtra("eventTemp",eventList[position].eventTemp)
                            intent.putExtra("eventHum",eventList[position].eventHum)
                            intent.putExtra("eventWarning",eventList[position].eventWarning)

                            startActivity(intent)
                            finish()
                        }
                    })
                    eventRecyclerView.visibility = View.VISIBLE
                    tvLoadingData.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


}