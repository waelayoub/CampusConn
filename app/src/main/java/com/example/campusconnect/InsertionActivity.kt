package com.example.campusconnect

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

class InsertionActivity : AppCompatActivity() {

    private lateinit var etEventID : EditText
    private lateinit var etName: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText


    private lateinit var timePickerDialog: TimePickerDialog

    private lateinit var etLocation: EditText
    private lateinit var etOrganizer: EditText
    private lateinit var etType: EditText
    private lateinit var etCapacity: EditText
    private lateinit var etDescription: EditText
    private lateinit var etTemp: EditText
    private lateinit var etHum: EditText
    private lateinit var etWarning: EditText

    private lateinit var imageView: ImageView
    private lateinit var imageViewIcon: ImageView
    private lateinit var btnUpload : Button

    private lateinit var calendar : Calendar


    private lateinit var btnSaveData: Button

    private lateinit var progressBar: ProgressBar

    private lateinit var dbRef: DatabaseReference
    private lateinit var strgRef : StorageReference
    private  var imageUri: Uri? = null
    private  var imageUriIcon: Uri? = null

    private lateinit var imageUrl  : String
    private lateinit var imageUrlIcon  : String


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insertion)

//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etEventID = findViewById(R.id.etEventId)
        etName = findViewById(R.id.etName)
        etDate = findViewById(R.id.etDate)
        calendar = Calendar.getInstance()


        val date = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            updateLabel()
        }

        etDate.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                    this@InsertionActivity,
                    date,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                // Set the minimum selectable date to today
                datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24

                datePickerDialog.show()
            }
        }

        etTime = findViewById(R.id.etTime)
        etTime.isFocusableInTouchMode = false
        etTime.isFocusable = false
        etTime.setOnClickListener { view ->
            timePickerDialog = TimePickerDialog(
                this, { timePicker, hourOfDay, minutes ->
                    etTime.setText(String.format("%02d:%02d", hourOfDay, minutes) )
                }, 0, 0, false
            )
            timePickerDialog.show()
        }


        etLocation = findViewById(R.id.etLocation)
        etOrganizer = findViewById(R.id.etOrganizer)
        etType = findViewById(R.id.etType)
        etCapacity = findViewById(R.id.etCapacity)
        etDescription = findViewById(R.id.etDescription)
        etTemp = findViewById(R.id.etTemp)
        etHum = findViewById(R.id.etHum)
        etWarning = findViewById(R.id.etWarning)

        btnSaveData = findViewById(R.id.btnSaveData)

        imageView = findViewById(R.id.imageView)
        imageViewIcon = findViewById(R.id.imageViewIcon)
        progressBar = findViewById(R.id.progressBar)
        btnUpload=findViewById(R.id.btnUploadImage)

        dbRef = FirebaseDatabase.getInstance().getReference("Events")
        strgRef = FirebaseStorage.getInstance().getReference()


        progressBar.visibility = View.INVISIBLE

        imageView.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent,2)
        }
        imageViewIcon.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent,3)
        }

        btnUpload.setOnClickListener(View.OnClickListener {
            if (imageUri == null ) {
                Toast.makeText(this, "Please Select Event's Flyer", Toast.LENGTH_SHORT).show()

            } else if (imageUriIcon == null) {
                Toast.makeText(this, "Please Select Event's Organizer Icon", Toast.LENGTH_SHORT).show()
            }
            else {
                uploadToFirebase(imageUri!!,false)
                uploadToFirebase(imageUriIcon!!,true)
            }
        })
        btnSaveData.isEnabled = false
        btnSaveData.setOnClickListener {
            saveEventData()
        }
    }


    private fun updateLabel() {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.getDefault())
        etDate.setText(dateFormat.format(calendar.time))

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
        }
        if (requestCode == 3 && resultCode == RESULT_OK && data != null) {
            imageUriIcon = data.data
            imageViewIcon.setImageURI(imageUriIcon)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun uploadToFirebase(uri: Uri, isIcon: Boolean) {
        val fileRef: StorageReference = strgRef.child("${System.currentTimeMillis()}.${getFileExtension(uri)}")
        fileRef.putFile(uri).addOnSuccessListener { taskSnapshot ->
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                if(isIcon) {
                    imageUrlIcon = uri.toString()
                } else{
                    imageUrl = uri.toString()
                }
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                println("the image url is :"+imageUrl)
                btnSaveData.isEnabled = true


            }
        }.addOnProgressListener{ snapshot ->
            progressBar.visibility = View.VISIBLE
        }.addOnFailureListener { e ->
            progressBar.visibility = View.INVISIBLE
            Toast.makeText(this, "Uploading Failed !!", Toast.LENGTH_SHORT).show()
        }

    }


    private fun getFileExtension(muri: Uri): String? {
        val cr: ContentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(muri))
    }


    private fun saveEventData() {
        //getting values

        val eventID = etEventID.text.toString()
        val eventName = etName.text.toString()
        val eventDate = etDate.text.toString()
        val eventTime = etTime.text.toString()
        val eventLocation = etLocation.text.toString()
        val eventOrganizer = etOrganizer.text.toString()
        val eventType = etType.text.toString()
        val eventCapacity = etCapacity.text.toString()
        val eventDescription = etDescription.text.toString()
        val eventTemp = etTemp.text.toString().toInt()
        val eventHum = etHum.text.toString().toInt()
        val eventWarning = etWarning.text.toString().toInt()



        if(eventID.isEmpty()){etEventID.error = "Please enter your event ID"}
        if (eventName.isEmpty()) {
            etName.error = "Please enter your event name"
            return
        }
        if (eventDate.isEmpty()) {
            etDate.error = "Please enter your event date"
            return
        }
        if (eventTime.isEmpty()) {
            etTime.error = "Please enter your event time"
            return
        }
        if (eventLocation.isEmpty()) {
            etLocation.error = "Please enter your event location"
            return
        }
        if (eventOrganizer.isEmpty()) {
            etOrganizer.error = "Please enter your event organizer"
            return
        }
        if (eventType.isEmpty()) {
            etType.error = "Please enter your event type"
            return
        }
        if (eventCapacity.isEmpty()) {
            etCapacity.error = "Please enter your event capacity"
            return
        }
        if (eventDescription.isEmpty()) {
            etDescription.error = "Please enter your event description"
            return
        }


        val event = EventModel(
            eventName, eventDate, eventTime, eventLocation,
            eventOrganizer, eventType, eventCapacity, eventDescription, imageUrl , imageUrlIcon, eventTemp.toFloat(), eventHum.toFloat(), eventWarning
        )

        dbRef.child(eventID).setValue(event)
            .addOnCompleteListener {
                Toast.makeText(this, "Event inserted successfully", Toast.LENGTH_LONG).show()
                val notification=
                    FcmNotificationsSender("/topics/all", "New Event", "Come check the new event recently added", applicationContext, this)
                notification.SendNotifications()
                etEventID.text.clear()
                etName.text.clear()
                etDate.text.clear()
                etTime.text.clear()
                etLocation.text.clear()
                etOrganizer.text.clear()
                etType.text.clear()
                etCapacity.text.clear()
                etDescription.text.clear()
                imageViewIcon.setImageResource(R.drawable.ic_icon)
                imageView.setImageResource(R.drawable.ic_image)



            }.addOnFailureListener { err ->
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
            }

        btnSaveData.isEnabled = false
        fun isFieldsValid(): Boolean {
            return (etEventID.text.isNotEmpty()
                    && etName.text.isNotEmpty()
                    && etDate.text.isNotEmpty()
                    && etTime.text.isNotEmpty()
                    && etLocation.text.isNotEmpty()
                    && etOrganizer.text.isNotEmpty()
                    && etType.text.isNotEmpty()
                    && etCapacity.text.isNotEmpty()
                    && etDescription.text.isNotEmpty())
        }
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                btnSaveData.isEnabled = isFieldsValid()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etEventID.addTextChangedListener(watcher)
        etName.addTextChangedListener(watcher)
        etDate.addTextChangedListener(watcher)
        etTime.addTextChangedListener(watcher)
        etLocation.addTextChangedListener(watcher)
        etOrganizer.addTextChangedListener(watcher)
        etType.addTextChangedListener(watcher)
        etCapacity.addTextChangedListener(watcher)
        etDescription.addTextChangedListener(watcher)
    }


    override fun onBackPressed() {
        super.onBackPressed()

        val parentActivityIntent = Intent(this, MainActivity::class.java)
        parentActivityIntent.putExtra("user","admin")
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
}



