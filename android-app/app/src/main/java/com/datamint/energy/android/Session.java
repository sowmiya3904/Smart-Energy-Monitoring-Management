package com.datamint.energy.android;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores the server URL and the login token. */
public class Session {
    private final SharedPreferences sp;
    public Session(Context c) { sp = c.getSharedPreferences("ems", Context.MODE_PRIVATE); }

    public String baseUrl() { return sp.getString("baseUrl", "http://localhost:8081/"); }
    public String token() { return sp.getString("token", null); }
    public String name() { return sp.getString("name", ""); }

    public void save(String baseUrl, String token, String name) {
        if (!baseUrl.endsWith("/")) baseUrl += "/";
        sp.edit().putString("baseUrl", baseUrl).putString("token", token).putString("name", name).apply();
    }
    public void clearToken() { sp.edit().remove("token").apply(); }
    public void setBaseUrl(String u) { sp.edit().putString("baseUrl", u.endsWith("/") ? u : u + "/").apply(); }
}
