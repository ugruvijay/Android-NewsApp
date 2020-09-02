package com.example.newsapp.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.LightingColorFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.R;
import com.example.newsapp.ui.article.DetailedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements NewsAdapter.OnNewsItemListener {
    private static final String TAG = "HOME_FRAGMENT";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private HomeFragment fragment;
    private ProgressBar mProgressBar;

    private HomeViewModel homeViewModel;
    private CardView mCardView;
    private Context thiscontext;
    private ArrayList<NewsItem> newsList;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;
    double latitude, longitude;
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        thiscontext = container.getContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(thiscontext);

        getLastLocation();

        root = inflater.inflate(R.layout.fragment_home, container, false);

        fragment = this;
        mAdapter = new NewsAdapter(newsList, fragment, thiscontext);
        mProgressBar = root.findViewById(R.id.progressBar_home);
        mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(thiscontext,android.R.color.holo_purple), android.graphics.PorterDuff.Mode.MULTIPLY);
        mRecyclerView = root.findViewById(R.id.home_recyclerview);
        newsList = getNews(thiscontext, root);
        mCardView = root.findViewById(R.id.cardview);

        final SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.swiperefresh_items);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(latitude != 0.0 && longitude != 0.0)
                    getWeatherReport(thiscontext, latitude, longitude);
                newsList = getNews(thiscontext, root);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 1000);
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    private void getWeatherReport(Context thiscontext, double latitude, double longitude) {
        RequestQueue requestQueue = Volley.newRequestQueue(thiscontext);
        String city1 = "";
        String state1 = "";
        Geocoder gcd = new Geocoder(thiscontext, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            city1 = addresses.get(0).getLocality();
            state1 = addresses.get(0).getAdminArea();
        }

        final ImageView weatherImageView = root.findViewById(R.id.image_weather);
        final TextView city = root.findViewById(R.id.text_city);
        final TextView state = root.findViewById(R.id.text_state);
        final TextView temperature = root.findViewById(R.id.text_temperature);
        final TextView weather = root.findViewById(R.id.text_weather);

        String url = "https://api.openweathermap.org/data/2.5/weather?q="+ city1 +"&units=metric&appid=b1ae08e63d8ead4ee889d5b1e8fd8283";

        final String finalState = state1;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Debug", response.toString());
                        try {
                            JSONObject temp = response.getJSONObject("main");
                            JSONObject weatherJson = response.getJSONArray("weather").getJSONObject(0);
                            int temp1 = Math.round(Float.parseFloat(temp.getString("temp")));
                            city.setText(response.getString("name"));
                            state.setText(finalState);
                            temperature.setText(temp1 + "\u2103");
                            weather.setText(weatherJson.getString("main"));

                            switch (weatherJson.getString("main")) {
                                case "Clear":
                                    weatherImageView.setImageResource(R.drawable.clear_weather);
                                    break;
                                case "Clouds":
                                    weatherImageView.setImageResource(R.drawable.cloudy_weather);
                                    break;
                                case "Snow":
                                    weatherImageView.setImageResource(R.drawable.snowy_weather);
                                    break;
                                case "Rain":
                                case "Drizzle":
                                    weatherImageView.setImageResource(R.drawable.rainy_weather);
                                    break;
                                default:
                                    weatherImageView.setImageResource(R.drawable.sunny_weather);
                                    break;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Debug", error.toString());
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private ArrayList<NewsItem> getNews(final Context thiscontext, final View root){
        final ArrayList<NewsItem> newsList = new ArrayList<>();
        RequestQueue requestQueue = Volley.newRequestQueue(thiscontext);
        final String defaultImageUrl = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";

        String url = "https://node-assignment9-ugru.wl.r.appspot.com/guardian";

        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                try {
                    JSONObject responseObject = response.getJSONObject("response");
                    JSONArray results = responseObject.getJSONArray("results");

                    for(int i = 0; i < results.length(); i++){
                        JSONObject jsonObject = results.getJSONObject(i);
                        String title = jsonObject.getString("webTitle");
                        String id = jsonObject.getString("id");
                        String time = jsonObject.getString("webPublicationDate");
                        LocalDateTime now = LocalDateTime.now();
                        ZoneId zoneId = ZoneId.of( "America/Los_Angeles" );
                        ZonedDateTime time1 = ZonedDateTime.parse(time);
                        ZonedDateTime time2 = now.atZone( zoneId );
                        String yyyy = "" + time1.getYear();
                        String mm = "" + time1.getMonth();
                        mm = mm.charAt(0) + mm.substring(1, 3).toLowerCase();
                        String dd = "" + time1.getDayOfMonth();
                        Long agoSeconds = getDifference(time1, time2, ChronoUnit.SECONDS);
                        Long agoMinutes = getDifference(time1, time2, ChronoUnit.MINUTES);
                        Long agoHours = getDifference(time1, time2, ChronoUnit.HOURS);
                        String ago = "";
                        if(agoHours > 0){
                            ago = agoHours + "h ago";
                        }
                        else if(agoMinutes > 0){
                            ago = agoMinutes + "m ago";
                        }
                        else if(agoSeconds > 0){
                            ago = agoSeconds + "s ago";
                        }

                        String sectionName = jsonObject.getString("sectionName");
                        String webUrl = jsonObject.getString("webUrl");
                        JSONObject fieldsObject = jsonObject.getJSONObject("fields");
                        String imageUrl = fieldsObject.has("thumbnail") ? fieldsObject.getString("thumbnail") : defaultImageUrl;
                        NewsItem news = new NewsItem(imageUrl, title, ago + " | " + sectionName , id, webUrl, dd + " " + mm + " | " + sectionName);
                        newsList.add(news);
                    }

                    mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
                    mRecyclerView.setHasFixedSize(true);
                    mLayoutManager = new LinearLayoutManager(thiscontext);
                    mAdapter = new NewsAdapter(newsList, fragment, thiscontext);

                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mRecyclerView.setAdapter(mAdapter);

                    mProgressBar.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Debug", error.toString());
                }
            });

        requestQueue.add(jsonObjectRequest);

        return newsList;
    }

    public Long getDifference(ZonedDateTime time1, ZonedDateTime time2, ChronoUnit unit){
        return unit.between(time1, time2);
    }

    @Override
    public void onNewsItemClick(int position) {
        NewsItem clickedItem = newsList.get(position);
        String id = clickedItem.getId();
        Log.d(TAG, "onNewsItemClick: " + id);

        Intent intent = new Intent(getActivity(), DetailedActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);

    }

    public void onLongClick(int position){
        final NewsItem clickedItem = newsList.get(position);

        LayoutInflater inflater = getLayoutInflater();
        View newView = inflater.inflate(R.layout.custom_dialog, null);

        final Dialog dialog = new Dialog(thiscontext);

        dialog.setContentView(newView);

        TextView titleTextView = dialog.findViewById(R.id.dialog_title);
        titleTextView.setText(clickedItem.getTitle());

        ImageView dialogImageView = newView.findViewById(R.id.dialog_imageView);
        dialog.show();
        Picasso.with(newView.getContext()).load(clickedItem.getImageUrl()).resize(300, 239)
                .into(dialogImageView);


        ImageButton twitterButton = newView.findViewById(R.id.dialog_twitter);
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://twitter.com/intent/tweet?text=Check out this link:&url=" + clickedItem.getWebUrl() + "&hashtags=CSCI571NewsSearch";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        
        final ImageView bookmarkButton = newView.findViewById(R.id.dialog_bookmark);

        if(fileExist(clickedItem.getId())){
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_24px);
        }
        else{
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_border_24px);
        }

        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileExist(clickedItem.getId())){
                    bookmarkButton.setImageResource(R.drawable.ic_bookmark_border_24px);
                    Toast.makeText(thiscontext, "" + clickedItem.getTitle() + " was removed from bookmarks", Toast.LENGTH_SHORT).show();
                    String yourFilePath = thiscontext.getFilesDir() + "/" + clickedItem.getId().hashCode();
                    File yourFile = new File(yourFilePath);
                    yourFile.delete();
                    mAdapter.notifyDataSetChanged();
                }
                else {
                    bookmarkButton.setImageResource(R.drawable.ic_bookmark_24px);
                    Toast.makeText(thiscontext, "" + clickedItem.getTitle() + " was added to bookmarks", Toast.LENGTH_SHORT).show();
                    Gson gson = new Gson();
                    String yourObjectJson = gson.toJson(clickedItem);
                    writeFile(yourObjectJson, clickedItem.getId());
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    private void writeFile(String jsonObject, String id){
        String yourFilePath = thiscontext.getFilesDir() + "/" + id.hashCode();
        File yourFile = new File(yourFilePath);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(yourFile);
            fileOutputStream.write(jsonObject.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean fileExist(String fname){
        File file = getActivity().getApplicationContext().getFileStreamPath("" + fname.hashCode());
        return file.exists();
    }

    private boolean checkPermissions(){
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    private void requestPermissions(){
        ActivityCompat.requestPermissions(
                getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    getWeatherReport(thiscontext, latitude, longitude);
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(thiscontext, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(thiscontext);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            getWeatherReport(thiscontext, latitude, longitude);
        }
    };
}
