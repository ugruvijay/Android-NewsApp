package com.example.newsapp.ui.headlines;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.R;
import com.example.newsapp.ui.article.DetailedActivity;
import com.example.newsapp.ui.home.NewsItem;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class WorldHeadlineFragment extends Fragment implements HeadlinesNewsAdapter.OnNewsItemListener{
    private static final String TAG = "WORLD_FRAGMENT";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private WorldHeadlineFragment fragment;
    private ArrayList<HeadlinesNewsItem> newsList;
    private Context thiscontext;
    private ProgressBar mProgressBar;

    public WorldHeadlineFragment() {
        fragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_world_headline, container, false);
        thiscontext = container.getContext();
        mProgressBar = root.findViewById(R.id.progressBar_world);
        mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(thiscontext,android.R.color.holo_purple), android.graphics.PorterDuff.Mode.MULTIPLY);
        mRecyclerView = root.findViewById(R.id.world_recyclerview);
        mAdapter = new HeadlinesNewsAdapter(newsList, fragment, thiscontext);

        newsList = getNews(thiscontext, root);
        final SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_items_world);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
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

    private ArrayList<HeadlinesNewsItem> getNews(final Context thiscontext, final View root){
        final ArrayList<HeadlinesNewsItem> newsList = new ArrayList<>();
        RequestQueue requestQueue = Volley.newRequestQueue(thiscontext);
        final String defaultImageUrl = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";

        String url = "https://node-assignment9-ugru.wl.r.appspot.com/guardian/world";

        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject responseObject = response.getJSONObject("response");
                            JSONArray results = responseObject.getJSONArray("results");

                            for(int i = 0; i < results.length(); i++) {
                                JSONObject jsonObject = results.getJSONObject(i);
                                String imageUrl = defaultImageUrl;

                                JSONObject blocksObject = jsonObject.has("blocks") ? jsonObject.getJSONObject("blocks") : null;
                                if(blocksObject != null) {
                                    JSONObject mainObject = blocksObject.has("main") ? blocksObject.getJSONObject("main") : null;
                                    if(mainObject != null){
                                        JSONObject elementsObject = mainObject.has("elements") ? mainObject.getJSONArray("elements").getJSONObject(0) : null;
                                        if(elementsObject != null){
                                            JSONArray assetsArray = elementsObject.has("assets") ? elementsObject.getJSONArray("assets") : null;
                                            if(assetsArray != null){
                                                JSONObject assetsObject = assetsArray.length() > 0 ? assetsArray.getJSONObject(0) : null;
                                                if(assetsObject != null){
                                                    imageUrl = assetsObject.has("file") ? assetsObject.getString("file") : defaultImageUrl;
                                                }
                                            }
                                        }
                                    }
                                }
                                String title = jsonObject.getString("webTitle");
                                String webUrl = jsonObject.getString("webUrl");
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
                                HeadlinesNewsItem news = new HeadlinesNewsItem(imageUrl, title, ago + " | " + sectionName , id, webUrl, dd + " " + mm + " | " + sectionName);
                                newsList.add(news);
                            }

                            mRecyclerView = root.findViewById(R.id.world_recyclerview);
                            mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
                            mRecyclerView.setHasFixedSize(true);
                            mLayoutManager = new LinearLayoutManager(thiscontext);
                            mAdapter = new HeadlinesNewsAdapter(newsList, fragment, thiscontext);

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
        HeadlinesNewsItem clickedItem = newsList.get(position);
        String id = clickedItem.getId();
        Log.d(TAG, "onNewsItemClick: " + id);
        Intent intent = new Intent(getActivity(), DetailedActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);

    }

    public void onLongClick(int position){
        final HeadlinesNewsItem clickedItem = newsList.get(position);
        String id = clickedItem.getId();
        Log.d(TAG, "onNewsItemClick: " + id);

        LayoutInflater inflater = getLayoutInflater();
        View newView = (View) inflater.inflate(R.layout.custom_dialog, null);

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
}
