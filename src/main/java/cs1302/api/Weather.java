package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**
 * Class to store the weather information from WeatherStacks.
 */
public class Weather {

    int temperature;
    @SerializedName(value = "weather_icons") String [] weatherIcons;
    @SerializedName(value = "weather_descriptions") String [] weatherDescriptions;
    int humidity;
    int feelslike;

} // Weather
