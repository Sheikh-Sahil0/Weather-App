package com.example.weatherapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fetchWeatherData("Indore");
        binding.txtCurrWeatherCondtion.setSelected(true);
        binding.txtSeaLevel.setSelected(true);
        searchCity();
    }

    private void searchCity() {
        SearchView searchView = binding.searchView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null) {
                    fetchWeatherData(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    private void fetchWeatherData(String cityName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterface apiService = retrofit.create(ApiInterface.class);

        Call<WeatherApp> call = apiService.getWeatherData(cityName, "031505cb0f41f6a468152ec72e91a57d", "metric");
        call.enqueue(new Callback<WeatherApp>() {
            @Override
            public void onResponse(Call<WeatherApp> call, Response<WeatherApp> response) {
                if (response.isSuccessful() && response.body() != null) {

                    WeatherApp responseBody = response.body();
                    double temperature = responseBody.getMain().getTemp();
                    int humidity = responseBody.getMain().getHumidity();
                    double windSpeed = responseBody.getWind().getSpeed();
                    long sunRise = responseBody.getSys().getSunrise();
                    long sunSet = responseBody.getSys().getSunset();
                    double seaLevel = responseBody.getMain().getPressure();
                    String condition = responseBody.getWeather().get(0).getDescription(); // Assuming weather is an array
                    double maxTemp = responseBody.getMain().getTemp_max();
                    double minTemp = responseBody.getMain().getTemp_min();

                    updateBackground(condition);

                    binding.txtCurrTemp.setText(String.format("%d°", (int) Math.round(temperature)));
                    binding.txtCurrWeatherCondtion.setText(condition);
                    binding.txtCurrWeatherAnimName.setText(condition);
                    binding.txtMaxTemp.setText(String.format("Max Temp: %.2f °C", maxTemp));
                    binding.txtMinTemp.setText(String.format("Min Temp: %.2f °C", minTemp));
                    binding.txtHumidityPercent.setText(String.format("%d %%", humidity));
                    binding.txtWindSpeed.setText(String.format("%.2f m/s", windSpeed));
                    binding.txtSunrise.setText(time(sunRise));
                    binding.txtSunset.setText(time(sunSet));
                    binding.txtSeaLevel.setText(String.format("%.2f hPa", seaLevel));
                    binding.txtCityName.setText(cityName);
                    binding.txtDayName.setText(dayName(System.currentTimeMillis()));
                    binding.txtDate.setText(date());

                }
            }

            @Override
            public void onFailure(Call<WeatherApp> call, Throwable t) {
                // Request failed, handle the error here
                t.printStackTrace();
            }
        });
    }

    private String dayName(Long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        return simpleDateFormat.format(new Date(timestamp * 1000));
    }

    private String time(Long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return simpleDateFormat.format(new Date(timestamp));
    }

    private String date() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        return simpleDateFormat.format(new Date());
    }

    // Function to set the background based on the weather condition
    private void updateBackground(String condition) {
        // Convert condition to lowercase for consistency
        condition = condition.toLowerCase();

        // Set background based on weather categories
        if (condition.contains("rain")) {
            binding.main.setBackgroundResource(R.drawable.rain_background); // Rainy background
            binding.weatherAnimation.setAnimation(R.raw.rain);
        } else if (condition.contains("snow")) {
            binding.main.setBackgroundResource(R.drawable.snow_background); // Snowy background
            binding.weatherAnimation.setAnimation(R.raw.snow);
        } else if (condition.contains("cloud")) {
            binding.main.setBackgroundResource(R.drawable.cloud_background_1); // Cloudy background
            binding.weatherAnimation.setAnimation(R.raw.cloud);
        } else {
            binding.main.setBackgroundResource(R.drawable.sunny_background); // Default sunny background
            binding.weatherAnimation.setAnimation(R.raw.sunny);
        }

        binding.weatherAnimation.playAnimation();
    }

}