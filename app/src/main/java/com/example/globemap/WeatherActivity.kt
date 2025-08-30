package com.example.globemap

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globemap.adapters.ForecastAdapter
import com.example.globemap.adapters.HourlyAdapter
import com.example.globemap.models.HourlyForecast
import com.example.globemap.models.ForecastItem


class WeatherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }


        val rvHourly = findViewById<RecyclerView>(R.id.rvHourly)
        rvHourly.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val hourlyData = listOf(
            HourlyForecast("15:00", "24°", R.drawable.sun_cloud),
            HourlyForecast("16:00", "25°", R.drawable.sun_cloud),
            HourlyForecast("17:00", "26°", R.drawable.clouds),
            HourlyForecast("18:00", "27°", R.drawable.moon_cloud)
        )

        val hourlyAdapter = HourlyAdapter(hourlyData)
        rvHourly.adapter = hourlyAdapter

        val rvDaily = findViewById<RecyclerView>(R.id.rvDaily)

        val forecastList = listOf(
            ForecastItem("Monday", R.drawable.big_rain_drops, "13°C", "10°C"),
            ForecastItem("Tuesday", R.drawable.sun_cloud, "17°C", "12°C"),
            ForecastItem("Wednesday", R.drawable.sun_cloud, "20°C", "15°C"),
        )

        val dailyAdapter = ForecastAdapter(forecastList)
        rvDaily.layoutManager = LinearLayoutManager(this)
        rvDaily.adapter = dailyAdapter

    }
}