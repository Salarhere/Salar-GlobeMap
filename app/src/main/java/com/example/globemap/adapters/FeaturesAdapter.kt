package com.example.globemap.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.globemap.R
import com.example.globemap.models.FeatureItem
import com.google.android.material.card.MaterialCardView

class FeaturesAdapter(private val items: List<FeatureItem>) :
    RecyclerView.Adapter<FeaturesAdapter.FeatureViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    inner class FeatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.imageViewItem)
        val title: TextView = itemView.findViewById(R.id.textViewItem)
        val backgroundView: MaterialCardView = itemView.findViewById(R.id.backgroundView)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                    listener?.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feature_item, parent, false)
        return FeatureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconRes)
        holder.backgroundView.setCardBackgroundColor(item.backgroundColor)
        holder.title.text = item.title

    }

    override fun getItemCount(): Int = items.size
}