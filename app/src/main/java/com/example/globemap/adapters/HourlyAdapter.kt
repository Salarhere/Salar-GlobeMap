package com.example.globemap.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.globemap.R
import com.example.globemap.models.HourlyForecast
import com.google.android.material.card.MaterialCardView


class HourlyAdapter(
    private val hourlyList: List<HourlyForecast>
) : RecyclerView.Adapter<HourlyAdapter.HourlyViewHolder>() {

    private var selectedPosition = -1

    inner class HourlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvHourTemp)
        val tvTemp: TextView = itemView.findViewById(R.id.tvHour)
        val ivWeatherIcon: ImageView = itemView.findViewById(R.id.ivHourIcon)
        val cardView: MaterialCardView = itemView as MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly, parent, false)
        return HourlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        val item = hourlyList[position]

        holder.tvTime.text = item.time
        holder.tvTemp.text = item.temperature
        holder.ivWeatherIcon.setImageResource(item.weatherIcon)

        if (selectedPosition == position) {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.lightPurple)
            )
        } else {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
        }

        holder.itemView.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int = hourlyList.size
}
