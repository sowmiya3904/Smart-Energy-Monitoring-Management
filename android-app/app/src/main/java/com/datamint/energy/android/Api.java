package com.datamint.energy.android;

import android.content.Context;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;

/** REST client. The app never talks to MySQL - only to the Spring Boot API. */
public class Api {
    public interface Service {
        @POST("api/auth/login") Call<JsonObject> login(@Body JsonObject body);
        @GET Call<JsonElement> get(@Url String url);
        @POST Call<ResponseBody> post(@Url String url, @Body JsonElement body);
        @PUT Call<ResponseBody> put(@Url String url, @Body JsonElement body);
        @DELETE Call<ResponseBody> delete(@Url String url);
    }

    private static Service service;
    private static String builtFor;

    public static synchronized Service get(Context ctx) {
        final Context app = ctx.getApplicationContext();
        String base = new Session(app).baseUrl();
        if (service == null || !base.equals(builtFor)) {
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain -> {
                Request.Builder b = chain.request().newBuilder();
                String t = new Session(app).token();
                if (t != null) b.header("Authorization", "Bearer " + t);
                return chain.proceed(b.build());
            }).build();
            service = new Retrofit.Builder().baseUrl(base).client(client)
                    .addConverterFactory(GsonConverterFactory.create()).build().create(Service.class);
            builtFor = base;
        }
        return service;
    }
}
