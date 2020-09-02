package com.example.newsapp.ui.trending;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.R;
import com.example.newsapp.ui.home.NewsAdapter;
import com.example.newsapp.ui.home.NewsItem;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TrendingFragment extends Fragment {

    private TrendingViewModel trendingViewModel;
    private LineChart mLineChart;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        trendingViewModel =
                ViewModelProviders.of(this).get(TrendingViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_trending, container, false);
        final Context thisContext = container.getContext();
        final EditText editText = root.findViewById(R.id.text_trending_search);

        String query = editText.getText().toString();

        if(query.isEmpty()){
            query = "Coronavirus";
        }

        final String finalQuery = query;
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    getTrendingData(thisContext, root, editText.getText().toString());
                    return true;
                }
                return false;
            }
        });

        getTrendingData(thisContext, root, finalQuery);
        return root;
    }

    private void getTrendingData(final Context thiscontext, final View root, final String query){
        final ArrayList<Integer> valueList = new ArrayList<>();
        RequestQueue requestQueue = Volley.newRequestQueue(thiscontext);

        String url = "https://node-assignment9-ugru.wl.r.appspot.com/trending?q=" + query;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject defaultObject = response.getJSONObject("default");
                            JSONArray results = defaultObject.getJSONArray("timelineData");

                            for(int i = 0; i < results.length(); i++){
                                JSONObject jsonObject = results.getJSONObject(i);
                                JSONArray value = jsonObject.getJSONArray("value");
                                String val = value.toString().substring(1, value.toString().indexOf("]"));
                                valueList.add(Integer.parseInt(val));
                            }
                            mLineChart = root.findViewById(R.id.line_chart);
                            mLineChart.setScaleEnabled(false);
                            mLineChart.getAxisRight().setDrawGridLines(false);
                            mLineChart.getAxisLeft().setDrawGridLines(false);
                            mLineChart.getXAxis().setDrawGridLines(false);
                            ArrayList<Entry> yValues = new ArrayList<>();
                            int count = 0;
                            for(Integer i : valueList){
                                yValues.add(new Entry(count, i));
                                count++;
                            }

                            LineDataSet set1 = new LineDataSet(yValues, "Trending Chart for " + query);
                            Legend legend = mLineChart.getLegend();
                            legend.setTextSize(15f);
                            legend.setFormSize(15f);
                            set1.setFillAlpha(110);
                            set1.setCircleColor(Color.BLUE);
                            set1.setColor(Color.BLUE);
                            set1.setValueTextColor(Color.BLUE);
                            set1.setCircleHoleColor(Color.BLUE);
                            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                            dataSets.add(set1);
                            LineData data = new LineData(dataSets);
                            mLineChart.setData(data);
                            mLineChart.notifyDataSetChanged();
                            mLineChart.invalidate();

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

}
