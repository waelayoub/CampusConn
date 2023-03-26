package com.example.campusconnect

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.w3c.dom.Text


class EventModelAdapter(val context: Context, val eventlist:ArrayList<EventModel>):
    RecyclerView.Adapter<EventModelAdapter.MyViewHolder>(){
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val eventName:TextView=itemView.findViewById(R.id.eventNameID)
        val eventTime:TextView=itemView.findViewById(R.id.eventTimeID)
        val eventImg:ImageView=itemView.findViewById(R.id.imageID)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.events_cardview,parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return eventlist.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem=eventlist[position]
        holder.eventName.text=currentitem.eventName
        holder.eventTime.text=currentitem.eventTime
        holder.itemView.setOnClickListener{

            val activity=it!!.context as AppCompatActivity
            activity.supportFragmentManager.beginTransaction().apply {
                val popUp=EventInfo(currentitem.eventName.toString(),
                currentitem.eventDate.toString(),
                currentitem.eventTime.toString(),
                currentitem.eventLocation.toString(),
                currentitem.eventOrganizer.toString(),
                currentitem.eventType.toString(),
                currentitem.eventCapacity.toString(),
                currentitem.eventDescription.toString(),
                currentitem.eventFlyer.toString(),
                currentitem.eventIcon.toString(),
                currentitem.eventId.toString())
                replace(R.id.frame_layout,popUp).commit()

            }

        }


        Glide.with(context).load(currentitem.eventIcon).into(holder.eventImg)
    }

}