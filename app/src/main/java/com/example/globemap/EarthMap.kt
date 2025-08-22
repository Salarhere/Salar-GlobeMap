package com.example.globemap

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globemap.adapters.MapTypeAdaptor
import com.example.globemap.models.MapType

class EarthMap : AppCompatActivity() {
    private var backBtn: ImageButton? = null
    private var selectedMapType: Int = 0  // default map type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earth_map)

        backBtn = findViewById(R.id.backBtn)

        val images = listOf(
            MapType(R.drawable.image),         // Standard
            MapType(R.drawable.hybrid_view),   // Hybrid
            MapType(R.drawable.satellite_view),// Satellite
            MapType(R.drawable.terrain_view)   // Terrain
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = MapTypeAdaptor(images) { position ->
            selectedMapType = position
        }

        backBtn!!.setOnClickListener {
            val intent = Intent()
            intent.putExtra("selectedMapType", selectedMapType)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}
