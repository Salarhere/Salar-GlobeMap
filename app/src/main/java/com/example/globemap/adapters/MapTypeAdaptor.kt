package com.example.globemap.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.globemap.R
import com.example.globemap.models.MapType

class MapTypeAdaptor(
    private val imageList: List<MapType>,
    private val onItemSelected: (Int) -> Unit
) : RecyclerView.Adapter<MapTypeAdaptor.ImageViewHolder>() {

    private var selectedPosition = -1

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val checkIcon: ImageView = itemView.findViewById(R.id.checkIcon)
        val container: FrameLayout = itemView.findViewById(R.id.backgroundView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.map_type_view, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val imageItem = imageList[position]
        holder.imageView.setImageResource(imageItem.imageResId)

        if (position == selectedPosition) {
            holder.imageView.setBackgroundResource(R.drawable.selected_stroke)
            holder.checkIcon.visibility = View.VISIBLE
        } else {
            holder.container.setBackgroundResource(0)
            holder.checkIcon.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val oldPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
            onItemSelected(position)
        }
    }

    override fun getItemCount(): Int = imageList.size
}
