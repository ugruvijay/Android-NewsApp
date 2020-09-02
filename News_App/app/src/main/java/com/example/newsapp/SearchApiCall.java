package com.example.newsapp;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class SearchApiCall {
    private static SearchApiCall mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    public SearchApiCall(Context ctx) {
        mCtx = ctx;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized SearchApiCall getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SearchApiCall(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static void make(Context ctx, String query, Response.Listener<String>
            listener, Response.ErrorListener errorListener) {
        String url = "https://autosearch-vijay.cognitiveservices.azure.com/bing/v7.0/suggestions?mkt=en-US&q=" + query;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Ocp-Apim-Subscription-Key", "92838e8248854910b0a0dbf6336819b2");
                return params;
            }
        };
        SearchApiCall.getInstance(ctx).addToRequestQueue(stringRequest);
    }
}