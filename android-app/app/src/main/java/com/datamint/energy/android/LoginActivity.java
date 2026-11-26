package com.datamint.energy.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonObject;
import retrofit2.*;

public class LoginActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (new Session(this).token() != null) { startActivity(new Intent(this, MainActivity.class)); finish(); return; }
        setContentView(R.layout.activity_login);
        EditText server = findViewById(R.id.etServer), user = findViewById(R.id.etUser), pass = findViewById(R.id.etPass);
        TextView err = findViewById(R.id.tvError);
        server.setText(new Session(this).baseUrl());
        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            err.setText("Signing in...");
            new Session(this).setBaseUrl(server.getText().toString().trim());
            JsonObject body = new JsonObject();
            body.addProperty("username", user.getText().toString().trim());
            body.addProperty("password", pass.getText().toString());
            Api.get(this).login(body).enqueue(new Callback<JsonObject>() {
                @Override public void onResponse(Call<JsonObject> c, Response<JsonObject> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        JsonObject o = r.body();
                        new Session(LoginActivity.this).save(server.getText().toString().trim(), o.get("token").getAsString(), o.get("fullName").getAsString());
                        startActivity(new Intent(LoginActivity.this, MainActivity.class)); finish();
                    } else err.setText("Invalid username or password");
                }
                @Override public void onFailure(Call<JsonObject> c, Throwable t) { err.setText("Cannot reach server: " + t.getMessage()); }
            });
        });
    }
}
