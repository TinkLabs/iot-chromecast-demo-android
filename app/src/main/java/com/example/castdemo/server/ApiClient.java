package com.example.castdemo.server;

import com.example.castdemo.display.DisplayItem;

import io.reactivex.Single;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class ApiClient {
    private static ApiClient sApiClient = null;
    private OkHttpClient mHttpClient;
    private Api mApi;

    public static synchronized ApiClient getInstance() {
        if (sApiClient == null) {
            sApiClient = new ApiClient();
        }
        return sApiClient;
    }

    private ApiClient() {
        mHttpClient = new OkHttpClient();
        mApi = new Retrofit.Builder()
                .baseUrl("http://rom.handy.travel/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(mHttpClient)
                .build()
                .create(Api.class);
    }

    private Api api() {
        return mApi;
    }

    public static Single<Response<DisplayItem>> getDisplayInfosRx() {
        return ApiClient.getInstance().api().getDisplayInfos();
    }

    public interface Api {
        @GET("tv/tv.json")
        Single<Response<DisplayItem>> getDisplayInfos();

    }
}
