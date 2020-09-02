package com.example.newsapp.ui.article;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.R;
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

public class DetailedActivity extends AppCompatActivity {
    Toolbar toolbar;
    ImageButton bookmarkButton, twitterButton, backButton;
    TextView titleToolbar;
    Context mContext;
    private ProgressBar mProgressBar;
    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        titleToolbar = findViewById(R.id.title_toolbar);
        backButton = findViewById(R.id.back_button_detailed);
        bookmarkButton = findViewById(R.id.bookmark_Button_Toolbar);
        twitterButton = findViewById(R.id.twitter_button_toolbar);
        Intent intent = getIntent();
        mContext = getApplicationContext();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final String id = intent.getStringExtra("id");
        mProgressBar = findViewById(R.id.progressBar_detailed);
        mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(),android.R.color.holo_purple), android.graphics.PorterDuff.Mode.MULTIPLY);
        mScrollView = findViewById(R.id.scrollView2);

        getDetailedArticle(getApplicationContext(), id);
    }

    private void getDetailedArticle(final Context thiscontext, final String id) {
        RequestQueue requestQueue = Volley.newRequestQueue(thiscontext);
        final String defaultImageUrl = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";

        String url = "https://node-assignment9-ugru.wl.r.appspot.com/guardian/detailed?id=" + id;

        mProgressBar.setVisibility(View.VISIBLE);
        mScrollView.setVisibility(View.GONE);
        bookmarkButton.setVisibility(View.GONE);
        twitterButton.setVisibility(View.GONE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject responseObject = response.getJSONObject("response");
                            JSONObject content = responseObject.getJSONObject("content");
                            JSONObject blocksObject = content != null ? content.getJSONObject("blocks") : null;
                            JSONObject mainObject = blocksObject != null ? blocksObject.getJSONObject("main") : null;
                            JSONArray bodyObject = blocksObject != null ? blocksObject.getJSONArray("body") : null;
                            String desc = "";

                            for(int i = 0; i < bodyObject.length(); i++){
                                JSONObject item = bodyObject.getJSONObject(i);
                                desc += item.getString("bodyHtml");
                            }


                            JSONObject elementsObject = mainObject != null ? mainObject.getJSONArray("elements").getJSONObject(0) : null;
                            JSONArray assetsArray = elementsObject != null ? elementsObject.getJSONArray("assets") : null;
                            JSONObject assetsObject = assetsArray.length() > 0 ? assetsArray.getJSONObject(0) : null;
                            final String title = content.getString("webTitle");
                            String time = content.getString("webPublicationDate");

                            LocalDateTime now = LocalDateTime.now();
                            ZoneId zoneId = ZoneId.of( "America/Los_Angeles" );
                            ZonedDateTime time1 = ZonedDateTime.parse(time);
                            String yyyy = "" + time1.getYear();
                            String mm = "" + time1.getMonth();
                            String dd = "" + time1.getDayOfMonth();

                            String sectionName = content.getString("sectionName");
                            String imageUrl = assetsObject != null ? assetsObject.has("file") ? assetsObject.getString("file") : defaultImageUrl : defaultImageUrl;
                            final String webUrl = content.getString("webUrl");

                            ImageView imageView = findViewById(R.id.detailed_imageView);
                            TextView titleTextView = findViewById(R.id.title);
                            TextView sectionTextView = findViewById(R.id.section);
                            TextView publicationDateTextView = findViewById(R.id.publicationDate);
                            TextView descriptionTextView = findViewById(R.id.description);
                            TextView hyperlink = findViewById(R.id.hyperlink);

                            titleToolbar.setText(title);
                            titleTextView.setText(title);
                            sectionTextView.setText(sectionName);
                            publicationDateTextView.setText(dd + " " + mm + " " + yyyy);
                            descriptionTextView.setText(Html.fromHtml(desc));
                            hyperlink.setText(Html.fromHtml("<a style=\"text-decoration:none\" href=" + webUrl + ">View Full Article </a>"));
                            hyperlink.setMovementMethod(LinkMovementMethod.getInstance());

                            final NewsItem item = new NewsItem(imageUrl, title, dd + " " + mm + " | " + sectionName, id, webUrl, dd + " " + mm);

                            Picasso.with(thiscontext).load(imageUrl).fit().into(imageView);

                            if(fileExist(id)){
                                bookmarkButton.setImageResource(R.drawable.ic_bookmark_24px);
                            }
                            else {
                                bookmarkButton.setImageResource(R.drawable.ic_bookmark_border_24px);
                            }

                            bookmarkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(fileExist(id)){
                                        bookmarkButton.setImageResource(R.drawable.ic_bookmark_border_24px);
                                        Toast.makeText(mContext, "" + title + " was removed from bookmarks", Toast.LENGTH_SHORT).show();
                                        String yourFilePath = mContext.getFilesDir() + "/" + id.hashCode();
                                        File yourFile = new File(yourFilePath);
                                        yourFile.delete();
                                    }
                                    else {
                                        bookmarkButton.setImageResource(R.drawable.ic_bookmark_24px);
                                        Toast.makeText(mContext, "" + title + " was added to bookmarks", Toast.LENGTH_SHORT).show();
                                        Gson gson = new Gson();
                                        String yourObjectJson = gson.toJson(item);
                                        writeFile(yourObjectJson, id);
                                    }
                                }
                            });

                            twitterButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String url = "https://twitter.com/intent/tweet?text=Check out this link:&url=" + webUrl  + "&hashtags=CSCI571NewsSearch";
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(url));
                                    startActivity(i);
                                }
                            });

                            mProgressBar.setVisibility(View.GONE);
                            mScrollView.setVisibility(View.VISIBLE);
                            bookmarkButton.setVisibility(View.VISIBLE);
                            twitterButton.setVisibility(View.VISIBLE);

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

    public boolean fileExist(String fname){
        File file = mContext.getFileStreamPath("" + fname.hashCode());
        return file.exists();
    }

    public void writeFile(String jsonObject, String id){
        String yourFilePath = mContext.getFilesDir() + "/" + id.hashCode();
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
}
