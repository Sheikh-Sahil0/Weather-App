package com.example.weatherapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.databinding.ActivityMainBinding;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.SharedPreferences;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private static final String PREFERENCES_FILE = "weather_preferences";
    private static final String KEY_DEFAULT_CITY = "default_city";
    private static final String API_KEY = "API Key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Check if a default city is already set
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
        String defaultCity = preferences.getString(KEY_DEFAULT_CITY, null);

        if (defaultCity == null) {
            // Prompt user to set the default city
            showDefaultCityDialog();
        } else {
            // Proceed with fetching weather data for the default city
            fetchWeatherData(defaultCity);
        }
        // This will allow our text to scroll horizontally (right to left)
        binding.txtCurrWeatherCondtion.setSelected(true);
        binding.txtSeaLevel.setSelected(true);
        searchCity();
    }

    // used to set the default value
    private void showDefaultCityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Default City");

        // Set up an input field for city name
        final EditText input = new EditText(this);
        input.setHint("Enter city name");
        builder.setView(input);

        builder.setCancelable(false); // Prevent the dialog from being dismissed

        builder.setPositiveButton("OK", null); // Set to null to handle custom behavior

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String cityName = input.getText().toString().trim();

            if (!cityName.isEmpty()) {
                validateCityName(cityName, dialog);
            } else {
                Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateCityName(String cityName, AlertDialog dialog) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterface apiService = retrofit.create(ApiInterface.class);

        Call<WeatherApp> call = apiService.getWeatherData(cityName,API_KEY , "metric");
        call.enqueue(new Callback<WeatherApp>() {
            @Override
            public void onResponse(Call<WeatherApp> call, Response<WeatherApp> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Save city to SharedPreferences and dismiss dialog
                    SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(KEY_DEFAULT_CITY, cityName);
                    editor.apply();

                    fetchWeatherData(cityName); // Fetch weather for valid city
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid city name. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherApp> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error occurred. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
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

        Call<WeatherApp> call = apiService.getWeatherData(cityName, API_KEY, "metric");
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
                    binding.txtCityName.setText(cityName.substring(0,1).toUpperCase() + cityName.substring(1).toLowerCase());
                    binding.txtDayName.setText(dayName(System.currentTimeMillis()));
                    binding.txtDate.setText(date());

                } else {
                    Toast.makeText(MainActivity.this, "Weather data unavailable for this city.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherApp> call, Throwable t) {
                // Request failed, handle the error here
                if (t instanceof IOException) {
                    Toast.makeText(MainActivity.this, "Network failure. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String dayName(Long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        return simpleDateFormat.format(new Date(timestamp));
    }


    private String time(Long timestamp) {
        // Convert timestamp from seconds to milliseconds
        Date date = new Date(timestamp * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return simpleDateFormat.format(date);
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