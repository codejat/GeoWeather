package com.ecandle.example.geoweather;
/**
 * Created by juantomaylla on 19/11/16.
 */

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ecandle.example.geoweather.Interface.HelperWS;
import com.ecandle.example.geoweather.Model.ResponseData;
import com.ecandle.example.geoweather.Model.WeatherData;
import com.ecandle.example.geoweather.Service.ConfigurationWS;
import com.ecandle.example.geoweather.Service.TrackGPS;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ecandle.example.com.geoweather.R;
import retrofit2.Call;

import static ecandle.example.com.geoweather.R.menu.weather;


public class ScrollingActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = "SPLASH";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Typeface weatherFont;
    private String place_location="";
    private String weather_info="";
    private String current_weather_info="";
    private ProgressBar spinner;

    TextView weather_report,place,weather_icon,country,icon_text;
    List myList ;
    String API_KEY;
    int PERMISSION_CODE_1 = 23;
    private final static String PATH_TO_WEATHER_FONT = "fonts/weather.ttf";
    private ListView lv;

    private TrackGPS gps;

        @Override
    protected void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scrolling);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);

        weather_icon=(TextView)findViewById(R.id.weather_icon);
        country=(TextView)findViewById(R.id.country);
        weatherFont = Typeface.createFromAsset(getAssets(), PATH_TO_WEATHER_FONT);
        weather_icon.setTypeface(weatherFont);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_id);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        API_KEY = getResources().getString(R.string.open_weather_maps_app_id);

        weather_report = (TextView) findViewById(R.id.weather_report);
        place =(TextView)findViewById(R.id.place);

        gps = new TrackGPS(ScrollingActivity.this);
        if (Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission(ScrollingActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(ScrollingActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(ScrollingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION, Toast.LENGTH_SHORT).show();

                    requestpermisions();

                } else {

                    // No explanation needed, we can request the permission.

                    requestpermisions();

                }
            }

            if(gps.canGetLocation()){

                String stringLatitude = String.valueOf(gps.latitude);
                String stringLongitude = String.valueOf(gps.longitude);
                Toast.makeText(getApplicationContext(),"Longitude:"+stringLongitude +"\nLatitude:"+stringLatitude, Toast.LENGTH_SHORT).show();
                getCurrentWeather(stringLatitude,stringLongitude);
                getWeatherData(stringLatitude,stringLongitude);

            }
            else
            {

                gps.showSettingsAlert();
            }

        } else {
            if(gps.canGetLocation()){

                String stringLatitude = String.valueOf(gps.latitude);
                String stringLongitude = String.valueOf(gps.longitude);
                Toast.makeText(getApplicationContext(),"Longitude:"+stringLongitude +"\nLatitude:"+stringLatitude, Toast.LENGTH_SHORT).show();
                getCurrentWeather(stringLatitude,stringLongitude);
                getWeatherData(stringLatitude,stringLongitude);

            }
            else
            {

                gps.showSettingsAlert();
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getString(R.string.location_of_weather) + place_location, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

    }

    public void requestpermisions() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE_1);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.change_city) {
            showInputDialog();
        }
        if (item.getItemId() == R.id.show_info) {
            showInfoDialog();
        }
        return false;
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.change_city));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton(getResources().getString(R.string.change), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
            }
        });
        builder.show();
    }
    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.city_weather_info));
        final TextView tv = new TextView(this);
        //input.setInputType(InputType.TYPE_CLASS_TEXT);
        tv.setText(weather_info);
        builder.setView(tv);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //changeCity(input.getText().toString());
            }
        });
        builder.show();
    }

    private void getWeatherData(final String lat, final String lon) {
        
        HelperWS helperWS = ConfigurationWS.getConfiguration().create(HelperWS.class);
        Call<WeatherData> result = helperWS.getWeatherData("forecast/weather?lat=" + lat + "&lon=" + lon +"&units=metric&cnt=10&appid=" + API_KEY + "&lang=" + Locale.getDefault().getLanguage());
        result.enqueue(new retrofit2.Callback<WeatherData>() {
            public List<Weather> weathers;
            @Override
            public void onResponse(Call<WeatherData> call, retrofit2.Response<WeatherData> response) {
                WeatherData resultado = response.body();
                if (resultado != null) {

                    //weather_report.setText(resultado.getList().get(0).getWeather().get(0).getDescription());
                    weather_report.setText(current_weather_info);
                    place.setText(resultado.getCity().getName());
                    country.setText(resultado.getCity().getCountry());
                    place_location =resultado.getCity().getName();
                    
                    Log.w("icon", resultado.getList().get(0).getWeather().get(0).getIcon());
                    switch (resultado.getList().get(0).getWeather().get(0).getIcon()){
                        case "01d":
                            weather_icon.setText(R.string.wi_day_sunny);
                            break;
                        case "02d":
                            weather_icon.setText(R.string.wi_cloudy_gusts);
                            break;
                        case "03d":
                            weather_icon.setText(R.string.wi_cloud_down);
                            break;
                        case "04d":
                            weather_icon.setText(R.string.wi_cloudy);
                            break;
                        case "04n":
                            weather_icon.setText(R.string.wi_night_cloudy);
                            break;
                        case "10d":
                            weather_icon.setText(R.string.wi_day_rain_mix);
                            break;
                        case "11d":
                            weather_icon.setText(R.string.wi_day_thunderstorm);
                            break;
                        case "13d":
                            weather_icon.setText(R.string.wi_day_snow);
                            break;
                        case "01n":
                            weather_icon.setText(R.string.wi_night_clear);
                            break;
                        case "02n":
                            weather_icon.setText(R.string.wi_night_cloudy);
                            break;
                        case "03n":
                            weather_icon.setText(R.string.wi_night_cloudy_gusts);
                            break;
                        case "10n":
                            weather_icon.setText(R.string.wi_night_cloudy_gusts);
                            break;
                        case "11n":
                            weather_icon.setText(R.string.wi_night_rain);
                            break;
                        case "13n":
                            weather_icon.setText(R.string.wi_night_snow);
                            break;



                    }
                    String[]humidity = new String[10];
                    String[]rain_description=new String[10];
                    String[]icon=new String[10];
                    String[]time=new String[10];
                    String[]temp = new String[10];
                    weathers = new ArrayList<>();
                    for (int i=0; i<resultado.getList().size();i++){
                        humidity[i] = getResources().getString(R.string.humidity) + String.valueOf(resultado.getList().get(i).getMain().getHumidity());
                        temp[i] = String.valueOf(resultado.getList().get(i).getMain().getTemp());
                        rain_description[i] = String.valueOf(resultado.getList().get(i).getWeather().get(0).getDescription());
                        icon[i] = String.valueOf(resultado.getList().get(i).getWeather().get(0).getIcon());

                        time[i] = String.valueOf(resultado.getList().get(i).getDt());

                        String detailsField =
                                resultado.getList().get(i).getWeather().get(0).getDescription().toUpperCase(Locale.US) +
                                        "\n" + getResources().getString(R.string.pressure) + resultado.getList().get(i).getMain().getPressure() + " hPa"+
                                        "\n" + getResources().getString(R.string.temperature) + resultado.getList().get(i).getMain().getTemp() + " C";
                        Log.w("humidity",humidity[i]);
                        Log.w("rain_description",rain_description[i]);
                        Log.w("icon",icon[i]);resultado.getList().get(i);
                        Log.w("time",time[i]);
                        Log.w("temp",temp[i]);
                        Log.w("detailsField",detailsField);
                        weathers.add(new Weather(
                                String.valueOf(resultado.getList().get(i).getWeather().get(0).getIcon()),
                                humidity[i]+ "%" ,
                                detailsField,
                                time[i]));

                    }
                    mAdapter = new WeatherAdapter(weathers,weatherFont);
                    mRecyclerView.setAdapter(mAdapter);

                    spinner.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {

            }
        });
    }

    private void getCurrentWeather(final String lat, final String lon) {
        HelperWS helperWS = ConfigurationWS.getConfiguration().create(HelperWS.class);
        Call<ResponseData> result = helperWS.getWeather("weather?lat=" + lat + "&lon=" + lon +"&units=metric&cnt=10&appid=" + API_KEY + "&lang=" + Locale.getDefault().getLanguage());

        result.enqueue(new retrofit2.Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, retrofit2.Response<ResponseData> response) {
                ResponseData resultado = response.body();
                if (resultado != null) {
                    //String cityField = resultado.getSys().getCountry();
                    String detailsField =
                            resultado.getWeather().get(0).getDescription().toUpperCase(Locale.US) +
                                    "\n" + getResources().getString(R.string.humidity) + resultado.getMain().getHumidity() + "%" +
                                    "\n" + getResources().getString(R.string.pressure) + resultado.getMain().getPressure() + " hPa"+
                                    "\n" + getResources().getString(R.string.temperature) + resultado.getMain().getTemp() + " C";
                    String currentTemperatureField = String.valueOf(resultado.getMain().getTemp());
                    DateFormat df = DateFormat.getDateTimeInstance();
                    String updatedOn = df.format(new Date(resultado.getDt() * 1000));
                    String updatedField = getResources().getString(R.string.last_update) + updatedOn;

                    Log.w("detailsField",detailsField);
                    Log.w("currentTemperatureField",currentTemperatureField);
                    Log.w("updatedOn",updatedOn);
                    Log.w("updatedField",updatedField);

                    StringBuilder sb = new StringBuilder();

                    sb.append(detailsField);
                    sb.append("\n");
                    sb.append(updatedField);
                    current_weather_info = sb.toString();

                    Log.w("current_weather_info",current_weather_info);
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {

            }
        });

    }
    
    private void updateWeatherData(final String city) {
        HelperWS helperWS = ConfigurationWS.getConfiguration().create(HelperWS.class);
        Call<ResponseData> result = helperWS.getWeather("weather?q=" + city + "&units=metric&appid=" + API_KEY + "&" + Locale.getDefault().getLanguage());
        result.enqueue(new retrofit2.Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, retrofit2.Response<ResponseData> response) {
                ResponseData resultado = response.body();
                if (resultado != null) {
                    String cityField = resultado.getSys().getCountry();
                    String detailsField =
                            resultado.getWeather().get(0).getDescription().toUpperCase() +
                                    "\n" + getResources().getString(R.string.humidity) + resultado.getMain().getHumidity() + "%" +
                                    "\n" + getResources().getString(R.string.pressure) + resultado.getMain().getPressure() + " hPa"+
                                    "\n" + getResources().getString(R.string.temperature) + resultado.getMain().getTemp() + " C";
                    String currentTemperatureField = String.valueOf(resultado.getMain().getTemp());
                    DateFormat df = DateFormat.getDateTimeInstance();
                    String updatedOn = df.format(new Date(resultado.getDt() * 1000));
                    String updatedField = getResources().getString(R.string.last_update) + updatedOn;

                    setWeatherIcon(resultado.getWeather().get(0).getId(),
                            resultado.getSys().getSunrise() * 1000,
                            resultado.getSys().getSunset() * 1000);

                    Log.w("cityField",cityField);
                    Log.w("detailsField",detailsField);
                    Log.w("currentTemperatureField",currentTemperatureField);
                    Log.w("updatedOn",updatedOn);
                    Log.w("updatedField",updatedField);

                    StringBuilder sb = new StringBuilder();
                    sb.append(cityField + ":" + city);
                    sb.append("\n");
                    sb.append(detailsField);
                    sb.append("\n");
                    sb.append(updatedField);
                    weather_info = sb.toString();

                    Log.w("weather_info",weather_info);
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {

            }
        });

    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = getResources().getString(R.string.wi_day_sunny);
            } else {
                icon = getResources().getString(R.string.wi_night_clear);
            }
        } else {
            switch (id) {
                case 2:
                    icon = getResources().getString(R.string.wi_day_thunderstorm);
                    break;
                case 3:
                    icon = getResources().getString(R.string.wi_sprinkle);
                    break;
                case 7:
                    icon = getResources().getString(R.string.wi_day_fog);
                    break;
                case 8:
                    icon = getResources().getString(R.string.wi_day_cloudy);
                    break;
                case 6:
                    icon = getResources().getString(R.string.wi_snow);
                    break;
                case 5:
                    icon = getResources().getString(R.string.wi_rain);
                    break;
            }
        }
        //weatherIcon.setText(icon);
    }

    public void changeCity(String city) {
        updateWeatherData(city);
    }


}
