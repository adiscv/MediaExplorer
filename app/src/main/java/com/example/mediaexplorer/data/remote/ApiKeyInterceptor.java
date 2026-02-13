package com.example.mediaexplorer.data.remote;

import android.util.Log;

import com.example.mediaexplorer.BuildConfig;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ApiKeyInterceptor implements Interceptor {
    private static final String TAG = "ApiKeyInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        HttpUrl originalHttpUrl = original.url();

        String apiKey = BuildConfig.TMDB_API_KEY;
        Log.d(TAG, "Intercepting request. API Key empty: " + apiKey.isEmpty() + ", URL: " + originalHttpUrl);

        HttpUrl url = originalHttpUrl.newBuilder()
                .addQueryParameter("api_key", apiKey)
                .build();

        Log.d(TAG, "Final URL: " + url);

        Request.Builder requestBuilder = original.newBuilder().url(url);
        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}



