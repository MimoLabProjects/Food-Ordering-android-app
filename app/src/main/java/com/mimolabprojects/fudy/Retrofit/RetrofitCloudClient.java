package com.mimolabprojects.fudy.Retrofit;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitCloudClient {

    private static Retrofit instance;
    public static Retrofit getInstance(){

        if (instance == null)
            instance = new Retrofit.Builder()
                    .baseUrl("https://firebase.google.com/docs/functions/write-firebase-functions/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
        return instance;
    }

}
