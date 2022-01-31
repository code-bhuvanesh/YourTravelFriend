package com.example.your_travel_friend.adpters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.your_travel_friend.R
import com.example.your_travel_friend.model.UserData
import org.w3c.dom.Text

class DriversListView(val context: Activity ,val driversList: MutableList<UserData>): ArrayAdapter<UserData>(context,
    R.layout.driver_details) {
    lateinit var  view: View

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        view = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.driver_details, parent, false)
        val cUser = driversList[position]
//        val profileImg: ImageView = view.findViewById(R.id.driverImage)
        val driverName: TextView = view.findViewById(R.id.driver_name)
        val arivalTime: TextView = view.findViewById(R.id.arival_time)
        val rating: TextView = view.findViewById(R.id.rating)
        val requestBtn: TextView = view.findViewById(R.id.request_button)
        val vehicleImg: ImageView = view.findViewById(R.id.vehicle_logo)

//        Glide.with(this).load(currentUser!!.photoUrl).into(profilePic)
        driverName.text = cUser.geUserData()["userName"]
        return view
    }

    override fun getCount(): Int {
        return driversList.size
    }
}