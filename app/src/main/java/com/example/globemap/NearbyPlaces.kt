package com.example.globemap

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globemap.adapters.NearbyPlacesAdaptor
import com.example.globemap.models.NearbyPlacesModel

class NearbyPlacesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NearbyPlacesAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_places)

        val backBtn: ImageView = findViewById(R.id.ivBack)
        backBtn.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.nearby_places_view)

        val placesList = listOf(
            NearbyPlacesModel("Hospital", R.drawable.hospital),
            NearbyPlacesModel("Hotels", R.drawable.hotel),
            NearbyPlacesModel("Mosques", R.drawable.mosque),
            NearbyPlacesModel("Airports", R.drawable.airport),
            NearbyPlacesModel("Bank", R.drawable.bank),
            NearbyPlacesModel("ATM", R.drawable.atm),
            NearbyPlacesModel("Post Office", R.drawable.post_office),
            NearbyPlacesModel("Schools", R.drawable.school),
            NearbyPlacesModel("University", R.drawable.university),
            NearbyPlacesModel("Fire Station", R.drawable.fire_station),
            NearbyPlacesModel("Police Station", R.drawable.police_station),
            NearbyPlacesModel("Parks", R.drawable.theme_park),
            NearbyPlacesModel("Bakery", R.drawable.bakery),
            NearbyPlacesModel("Cafe", R.drawable.cafe),
            NearbyPlacesModel("Service Station", R.drawable.service_station),
            NearbyPlacesModel("Church", R.drawable.church),
            NearbyPlacesModel("Clothing Store", R.drawable.male_character),
            NearbyPlacesModel("Dentist", R.drawable.dental_checkup),
            NearbyPlacesModel("Doctor", R.drawable.doctor),
            NearbyPlacesModel("Gas Station", R.drawable.gas_station),
            NearbyPlacesModel("Beauty Salon", R.drawable.makeover),
            NearbyPlacesModel("Jewellery", R.drawable.jewellery),
            NearbyPlacesModel("Pet Store", R.drawable.pet_shop),
            NearbyPlacesModel("Pharmacy", R.drawable.pharmacy),
            NearbyPlacesModel("Shoe Store", R.drawable.shoe_shop),
            NearbyPlacesModel("Shopping Mall", R.drawable.shopping_center),
            NearbyPlacesModel("Subway Station", R.drawable.train_station),
            NearbyPlacesModel("Zoo", R.drawable.zoo),
            NearbyPlacesModel("Stadium", R.drawable.stadium),
            NearbyPlacesModel("Bus Stop", R.drawable.bus_stop),
            NearbyPlacesModel("Temple", R.drawable.temple),
            NearbyPlacesModel("Bar", R.drawable.bar),
            NearbyPlacesModel("Theatre", R.drawable.theater),
            NearbyPlacesModel("Art Gallery", R.drawable.gallery)
        )

        adapter = NearbyPlacesAdaptor(placesList) { place ->
            Toast.makeText(this, "Clicked: ${place.name}", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }
}
