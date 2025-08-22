package com.example.globemap.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.api.geocoding.v5.models.CarmenFeature

class SuggestionAdapter(
    private val suggestions: MutableList<CarmenFeature>,
    private val onItemClick: (CarmenFeature) -> Unit
) : RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {

    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val feature = suggestions[position]
        holder.textView.text = feature.placeName()
        holder.textView.setTextColor(Color.BLACK)
        holder.itemView.setOnClickListener { onItemClick(feature) }
    }

    override fun getItemCount(): Int = suggestions.size

    fun updateData(newData: List<CarmenFeature>) {
        suggestions.clear()
        suggestions.addAll(newData)
        notifyDataSetChanged()
    }
}
