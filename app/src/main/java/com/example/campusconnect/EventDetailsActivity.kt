package com.example.campusconnect

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class EventDetailsActivity : AppCompatActivity() {
    private lateinit var tvEventId : TextView
    private lateinit var tvEventName : TextView
    private lateinit var tvEventDate : TextView
    private lateinit var tvEventTime : TextView
    private lateinit var tvEventLocation : TextView
    private lateinit var tvEventOrganizer : TextView
    private lateinit var tvEventType : TextView
    private lateinit var tvEventCapacity : TextView
    private lateinit var tvEventDescription : TextView
    private lateinit var tvEventFlyer : TextView
    private lateinit var tvEventIcon : TextView
    private lateinit var tvEventTemp : TextView
    private lateinit var tvEventHum : TextView
    private lateinit var tvEventWarning : TextView

    private lateinit var btnUpdate : Button
    private lateinit var btnDelete : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

//         supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initView()
        setValuesToViews()

        btnUpdate.setOnClickListener{
            openUpdateDialog(
                intent.getStringExtra("eventId").toString(),
                intent.getStringExtra("eventName").toString())
        }

        btnDelete.setOnClickListener {
            deleteRecord(
                intent.getStringExtra("eventId").toString()
            )
        }
    }

    private fun deleteRecord(id: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete this event?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                val dbRef = FirebaseDatabase.getInstance().getReference("Events").child(id)
                val mTask = dbRef.removeValue()
                mTask.addOnSuccessListener {
                    Toast.makeText(this, "Event Data Deleted", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, FetchingActivity::class.java)
                    finish()
                    startActivity(intent)
                }.addOnFailureListener { error ->
                    Toast.makeText(this, "Deleting error ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
    }


    @SuppressLint("MissingInflatedId")
    private fun openUpdateDialog(
        eventId : String,
        eventName: String
    ) {
        val mDialog = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val mDialogView = inflater.inflate(R.layout.update_dialog,null)

        mDialog.setView(mDialogView)

        val etEvtId = mDialogView.findViewById<EditText>(R.id.etEvtId)
        val etEvtName = mDialogView.findViewById<EditText>(R.id.etEvtName)
        val etEvtDate = mDialogView.findViewById<EditText>(R.id.etEvtDate)
        val etEvtTime = mDialogView.findViewById<EditText>(R.id.etEvtTime)
        val etEvtLocation = mDialogView.findViewById<EditText>(R.id.etEvtLocation)
        val etEvtOrganizer = mDialogView.findViewById<EditText>(R.id.etEvtOrganizer)
        val etEvtType = mDialogView.findViewById<EditText>(R.id.etEvtType)
        val etEvtCapacity = mDialogView.findViewById<EditText>(R.id.etEvtCapacity)
        val etEvtDescription = mDialogView.findViewById<EditText>(R.id.etEvtDescription)
        val etEvtFlyer = mDialogView.findViewById<EditText>(R.id.etEvtFlyer)
        val etEvtIcon = mDialogView.findViewById<EditText>(R.id.etEvtIcon)

        val btnUpdateData = mDialogView.findViewById<Button>(R.id.btnUpdateData)


        etEvtDate.isFocusable = false
        etEvtDate.isFocusableInTouchMode = false
        etEvtDate.setOnClickListener {
            showDatePicker(etEvtDate)
        }


        etEvtTime.isFocusableInTouchMode = false
        etEvtTime.isFocusable = false
        etEvtTime.setOnClickListener {
            showTimePicker(etEvtTime)
        }

        etEvtId.setText(intent.getStringExtra("eventId").toString())
        etEvtName.setText(intent.getStringExtra("eventName").toString())
        etEvtDate.setText(intent.getStringExtra("eventDate").toString())
        etEvtTime.setText(intent.getStringExtra("eventTime").toString())
        etEvtLocation.setText(intent.getStringExtra("eventLocation").toString())
        etEvtOrganizer.setText(intent.getStringExtra("eventOrganizer").toString())
        etEvtType.setText(intent.getStringExtra("eventType").toString())
        etEvtCapacity.setText(intent.getStringExtra("eventCapacity").toString())
        etEvtDescription.setText(intent.getStringExtra("eventDescription").toString())
        etEvtFlyer.setText(intent.getStringExtra("eventFlyer").toString())
        etEvtIcon.setText(intent.getStringExtra("eventIcon").toString())


        mDialog.setTitle("Updating $eventName data")

        val alertDialog = mDialog.create()
        alertDialog.show()

        btnUpdateData.setOnClickListener{
            updateEventData(
                etEvtName.text.toString(),
                etEvtDate.text.toString(),
                etEvtTime.text.toString(),
                etEvtLocation.text.toString(),
                etEvtOrganizer.text.toString(),
                etEvtType.text.toString(),
                etEvtCapacity.text.toString(),
                etEvtDescription.text.toString(),
                etEvtFlyer.text.toString(),
                etEvtIcon.text.toString(),
                tvEventTemp.text.toString().toInt(),
                tvEventHum.text.toString().toInt(),
                tvEventWarning.text.toString().toInt(),
                eventId
            )
            Toast.makeText(applicationContext, "Event Data Updated", Toast.LENGTH_LONG).show()

            tvEventId.text = etEvtId.text.toString()
            tvEventName.text = etEvtName.text.toString()
            tvEventDate.text = etEvtDate.text.toString()
            tvEventTime.text =  etEvtTime.text.toString()
            tvEventLocation.text = etEvtLocation.text.toString()
            tvEventOrganizer.text = etEvtOrganizer.text.toString()
            tvEventType.text =etEvtType.text.toString()
            tvEventCapacity.text = etEvtCapacity.text.toString()
            tvEventDescription.text = etEvtDescription.text.toString()
            tvEventFlyer.text = etEvtFlyer.text.toString()
            tvEventIcon.text = etEvtIcon.text.toString()

            alertDialog.dismiss()
        }
    }

    private fun showDatePicker(dateEditText: EditText) {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                dateEditText.setText(formattedDate)
            }, year, month, day)

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24
        datePickerDialog.show()
    }

    private fun showTimePicker(timeEditText: EditText) {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this,
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                timeEditText.setText(selectedTime)
            }, hour, minute, true)

        timePickerDialog.show()
    }

    private fun updateEventData(
        name: String,
        date: String,
        time: String,
        location: String,
        organizer : String,
        type: String,
        capacity: String,
        description: String,
        flyer: String,
        icon: String,
        temp: Int,
        Hum: Int,
        Warning: Int,
        id : String
    ) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Events").child(id)
        val eventInfo = EventModel(name,date,time,location,organizer,type,capacity,description,flyer,icon, temp.toFloat(),Hum.toFloat(), Warning)
        println("event id : " + id)
        dbRef.setValue(eventInfo)
    }

    private fun initView() {
        tvEventId = findViewById(R.id.tvEventId)
        tvEventName = findViewById(R.id.tvEventName)
        tvEventDate = findViewById(R.id.tvEventDate)
        tvEventTime = findViewById(R.id.tvEventTime)
        tvEventType = findViewById(R.id.tvEventType)
        tvEventLocation = findViewById(R.id.tvEventLocation)
        tvEventOrganizer = findViewById(R.id.tvEventOrganizer)
        tvEventCapacity = findViewById(R.id.tvEventCapacity)
        tvEventDescription = findViewById(R.id.tvEventDescription)
        tvEventFlyer = findViewById(R.id.tvEventFlyer)
        tvEventIcon = findViewById(R.id.tvEventIcon)
        tvEventTemp = findViewById(R.id.tvEventTemp)
        tvEventHum = findViewById(R.id.tvEventHum)
        tvEventWarning = findViewById(R.id.tvEventWarning)

        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
    }
    private fun setValuesToViews() {
        tvEventId.text = intent.getStringExtra("eventId")
        tvEventName.text = intent.getStringExtra("eventName")
        tvEventDate.text = intent.getStringExtra("eventDate")
        tvEventTime.text = intent.getStringExtra("eventTime")
        tvEventLocation.text = intent.getStringExtra("eventLocation")
        tvEventOrganizer.text = intent.getStringExtra("eventOrganizer")
        tvEventType.text = intent.getStringExtra("eventType")
        tvEventCapacity.text = intent.getStringExtra("eventCapacity")
        tvEventDescription.text = intent.getStringExtra("eventDescription")
        tvEventFlyer.text = intent.getStringExtra("eventFlyer")
        tvEventIcon.text = intent.getStringExtra("eventIcon")
        //tvEventTemp.text = intent.getStringExtra("eventTemp").toString()

        tvEventTemp.text = intent.getIntExtra("eventTemp", 0).toString()
        tvEventHum.text = intent.getIntExtra("eventHum", 0).toString()
        tvEventWarning.text = intent.getIntExtra("eventWarning", 0).toString()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val parentActivityIntent = Intent(this, FetchingActivity::class.java)
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
}
