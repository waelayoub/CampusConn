package com.example.campusconnect

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

import org.w3c.dom.Text
import java.text.SimpleDateFormat


class EventModelAdapter(val context: Context, var eventlist:ArrayList<EventModel>,val specifier:Boolean):
    RecyclerView.Adapter<EventModelAdapter.MyViewHolder>(){

    var isShimmer=true
    val shimmerNumber=7



    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val eventName:TextView=itemView.findViewById(R.id.eventNameID)
        val eventTime:TextView=itemView.findViewById(R.id.eventTimeID)
        val eventImg:ImageView=itemView.findViewById(R.id.imageID)

        val eventTempImg:ImageView=itemView.findViewById(R.id.thermometer)
        val eventTemp:TextView=itemView.findViewById(R.id.evenTempID)

        val eventHuImg:ImageView=itemView.findViewById(R.id.humidity)
        val eventHumidity:TextView=itemView.findViewById(R.id.evenHumID)

        val eventWar:TextView=itemView.findViewById(R.id.eventWarningID)

        val linearTemp:LinearLayout=itemView.findViewById(R.id.linear1)
        val linearHumidity:LinearLayout=itemView.findViewById(R.id.linear2)
        val linearWarning:LinearLayout=itemView.findViewById(R.id.linear3)

        val deleteBTN:ImageView=itemView.findViewById(R.id.deletebtn)

        val shimmerViewContainer:ShimmerFrameLayout=itemView.findViewById(R.id.shimmer_view_container)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        eventlist.sortBy { SimpleDateFormat("dd/MM/yyyy HH:mm").parse(it.eventDate+" "+it.eventTime).time}

        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.events_cardview,parent,false)

        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return if(isShimmer) shimmerNumber else eventlist.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        if(isShimmer){
            holder.shimmerViewContainer.startShimmer()
            if (specifier == true ) {
                    holder.eventTemp.background = null
                    holder.eventHumidity.background = null
                    holder.deleteBTN.visibility = View.VISIBLE

            } else {
                holder.linearTemp.visibility = View.INVISIBLE
                holder.linearHumidity.visibility=View.INVISIBLE

                holder.deleteBTN.visibility = View.INVISIBLE
            }
            
        }else{
            holder.shimmerViewContainer.stopShimmer()
            holder.shimmerViewContainer.setShimmer(null)
            if (specifier == true) {
                val currentitem = eventlist[position]
                println("The Temp is :"+currentitem.eventTemp)

                if (currentitem.eventTemp.toString()!="-273.0" && currentitem.eventHum.toString()!="-1.0" ) {
                    holder.eventTemp.text = currentitem.eventTemp.toString() + " °C"
                    holder.eventHumidity.text = currentitem.eventHum.toString() + " %"
                }else{
                    holder.linearTemp.visibility=View.INVISIBLE
                    holder.linearHumidity.visibility=View.INVISIBLE
                }

                if (currentitem.eventWarning!="0"){
                    holder.eventWar.background=null
                    holder.linearWarning.visibility=View.VISIBLE
                }else{
                    holder.linearWarning.visibility=View.INVISIBLE
                }

                holder.eventTemp.background=null
                holder.eventHumidity.background=null
                holder.deleteBTN.visibility = View.VISIBLE
            } else {
                holder.linearTemp.visibility = View.INVISIBLE
                holder.linearHumidity.visibility=View.INVISIBLE

                holder.deleteBTN.visibility = View.INVISIBLE
            }


            val currentitem = eventlist[position]
            println("The Temp is :"+currentitem.eventTemp)

            holder.eventName.text = currentitem.eventName
            holder.eventName.background=null
            holder.eventTime.text =
                currentitem.eventDate.toString() + " " + currentitem.eventTime.toString()

            holder.eventTime.background=null
            holder.eventImg.background=null


            holder.itemView.setOnClickListener {
                val activity = it!!.context as AppCompatActivity
                activity.supportFragmentManager.beginTransaction().apply {
                    val popUp = EventInfo(
                        currentitem.eventName.toString(),
                        currentitem.eventDate.toString(),
                        currentitem.eventTime.toString(),
                        currentitem.eventLocation.toString(),
                        currentitem.eventOrganizer.toString(),
                        currentitem.eventType.toString(),
                        currentitem.eventCapacity.toString(),
                        currentitem.eventDescription.toString(),
                        currentitem.eventFlyer.toString(),
                        currentitem.eventIcon.toString(),
                        currentitem.eventId.toString()
                    )
                    replace(R.id.frame_layout, popUp).commit()
                }
            }
            holder.deleteBTN.setOnClickListener {
                val auth = Firebase.auth
                val dbRefReg = FirebaseDatabase.getInstance().getReference("registrations")
                    .child(currentitem.eventId!!).child(auth.currentUser!!.uid)
                dbRefReg.removeValue()


                FirebaseMessaging.getInstance().unsubscribeFromTopic(currentitem.eventId!!)
                    .addOnCompleteListener { task ->
                    var msg = "Done"
                    if (!task.isSuccessful) {
                        msg = "Failed"
                    }

                    println(msg)
                }.addOnFailureListener { println("Action failed") }
            }
            Glide.with(context).load(currentitem.eventIcon).into(holder.eventImg)

        }


    }

    }

