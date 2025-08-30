package com.example.globemap.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.globemap.R
import com.example.globemap.models.ForecastItem

class ForecastAdapter(
    private val items: List<ForecastItem>
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    inner class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val ivWeatherIcon: ImageView = itemView.findViewById(R.id.ivWeatherIcon)
        val tvMaxTemp: TextView = itemView.findViewById(R.id.tvMaxTemp)
        val tvMinTemp: TextView = itemView.findViewById(R.id.tvMinTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = items[position]
        holder.tvDay.text = item.day
        holder.ivWeatherIcon.setImageResource(item.iconRes)
        holder.tvMaxTemp.text = item.maxTemp
        holder.tvMinTemp.text = item.minTemp
    }

    override fun getItemCount(): Int = items.size
}
