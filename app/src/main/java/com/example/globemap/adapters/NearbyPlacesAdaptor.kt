package com.example.globemap.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.globemap.R
import com.example.globemap.models.NearbyPlacesModel

class NearbyPlacesAdaptor(
    private val places: List<NearbyPlacesModel>,
    private val onItemClick: (NearbyPlacesModel) -> Unit
) : RecyclerView.Adapter<NearbyPlacesAdaptor.PlaceViewHolder>() {

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.ivIcon)
        val name: TextView = itemView.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.icon.setImageResource(place.iconRes)
        holder.name.text = place.name
        holder.itemView.setOnClickListener { onItemClick(place) }
    }

    override fun getItemCount(): Int = places.size
}
