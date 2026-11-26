package com.datamint.energy.android;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.*;
import java.util.Map;
import java.util.function.Consumer;
import okhttp3.ResponseBody;
import retrofit2.*;

/** One screen for every module: tabs for the module's views, data shown as cards (not raw JSON). */
public class ModuleActivity extends AppCompatActivity {
    private Modules.M m; private int vi = 0;
    private LinearLayout tabs, tools, content;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        m = Modules.find(getIntent().getStringExtra("key"));
        setContentView(R.layout.activity_module);
        ((TextView) findViewById(R.id.tvTitle)).setText(m.icon + "  " + m.title);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tabs = findViewById(R.id.tabs); tools = findViewById(R.id.tools); content = findViewById(R.id.content);
        build();
    }

    private void build() {
        tabs.removeAllViews(); tools.removeAllViews();
        if (m.views.length == 0) { finish(); return; }
        for (int i = 0; i < m.views.length; i++) {
            final int idx = i;
            Button t = Ui.button(this, m.views[i].name, i == vi ? Ui.BLUE : Ui.BORDER);
            t.setOnClickListener(v -> { vi = idx; build(); });
            tabs.addView(t);
        }
        final Modules.V view = m.views[vi];
        if (view.crud) {
            Button add = Ui.button(this, "+ Add", Ui.GREEN); add.setTextColor(0xFF04210F);
            add.setOnClickListener(v -> askJson("Add new record (JSON)", "{}", json -> send("POST", view.url, json, this::load)));
            tools.addView(add);
        }
        if (m.tools != null) for (String[] t : m.tools) {
            Button tb = Ui.button(this, t[0], Ui.PURPLE);
            tb.setOnClickListener(v -> send(t[1], t[2], "{}", this::load));
            tools.addView(tb);
        }
        load();
    }

    private void load() {
        final Modules.V view = m.views[vi];
        content.removeAllViews(); content.addView(Ui.text(this, "Loading…", 13, Ui.MUTED, false));
        Api.get(this).get(view.url).enqueue(new Callback<JsonElement>() {
            @Override public void onResponse(Call<JsonElement> c, Response<JsonElement> r) {
                content.removeAllViews();
                if (r.code() == 401) { new Session(ModuleActivity.this).clearToken(); startActivity(new Intent(ModuleActivity.this, LoginActivity.class)); finishAffinity(); return; }
                if (r.code() == 403) { content.addView(Ui.text(ModuleActivity.this, "Your role is not allowed to open this module.", 14, Ui.ORANGE, false)); return; }
                if (!r.isSuccessful() || r.body() == null) { content.addView(Ui.text(ModuleActivity.this, "Error " + r.code(), 14, Ui.ORANGE, false)); return; }
                JsonElement e = r.body();
                if (e.isJsonArray()) cards(content, e.getAsJsonArray(), view); else if (e.isJsonObject()) obj(content, e.getAsJsonObject(), view);
            }
            @Override public void onFailure(Call<JsonElement> c, Throwable t) {
                content.removeAllViews(); content.addView(Ui.text(ModuleActivity.this, "Cannot reach server: " + t.getMessage(), 14, Ui.ORANGE, false));
            }
        });
    }

    // ---------- rendering ----------
    private void obj(LinearLayout parent, JsonObject o, Modules.V view) {
        if (o.has("content") && o.get("content").isJsonArray()) {
            cards(parent, o.getAsJsonArray("content"), view);
            parent.addView(Ui.text(this, o.get("totalElements") + " readings in total (showing newest)", 12, Ui.MUTED, false));
            return;
        }
        LinearLayout card = Ui.card(this); boolean any = false;
        for (Map.Entry<String, JsonElement> e : o.entrySet()) {
            JsonElement v = e.getValue();
            if (isObjectArray(v)) continue;
            card.addView(kv(e.getKey(), val(v), m.settings)); any = true;
        }
        if (any) parent.addView(card);
        for (Map.Entry<String, JsonElement> e : o.entrySet()) {
            if (!isObjectArray(e.getValue())) continue;
            TextView h = Ui.text(this, pretty(e.getKey()), 16, Ui.TEXT, true); h.setPadding(Ui.dp(this, 6), Ui.dp(this, 12), 0, Ui.dp(this, 2));
            parent.addView(h);
            cards(parent, e.getValue().getAsJsonArray(), null);
        }
    }

    private void cards(LinearLayout parent, JsonArray arr, Modules.V view) {
        if (arr.size() == 0) { parent.addView(Ui.text(this, "No records.", 13, Ui.MUTED, false)); return; }
        String[] titleKeys = {"name", "title", "username", "period", "month", "department", "loadType", "type", "code", "tag", "building", "time"};
        int n = 0;
        for (JsonElement e : arr) {
            if (!e.isJsonObject()) continue;
            if (++n > 60) { parent.addView(Ui.text(this, "… showing the first 60", 12, Ui.MUTED, false)); break; }
            final JsonObject o = e.getAsJsonObject(); LinearLayout card = Ui.card(this);
            String tk = null; for (String k : titleKeys) if (o.has(k) && !o.get(k).isJsonNull()) { tk = k; break; }
            LinearLayout top = Ui.row(this);
            top.addView(Ui.text(this, tk == null ? "#" + n : val(o.get(tk)), 15, Ui.TEXT, true), Ui.weight(1f));
            if (o.has("severity") && !o.get("severity").isJsonNull()) top.addView(Ui.tag(this, o.get("severity").getAsString(), color(o.get("severity").getAsString())));
            if (o.has("status") && !o.get("status").isJsonNull()) top.addView(Ui.tag(this, o.get("status").getAsString(), color(o.get("status").getAsString())));
            card.addView(top);
            for (Map.Entry<String, JsonElement> en : o.entrySet()) {
                String k = en.getKey();
                if (k.equals(tk) || k.equals("severity") || k.equals("status") || en.getValue().isJsonNull() || val(en.getValue()).isEmpty()) continue;
                card.addView(kv(k, val(en.getValue()), false));
            }
            if (view != null && (view.actions != null || view.crud) && o.has("id")) {
                LinearLayout acts = Ui.row(this);
                final String id = o.get("id").getAsString();
                if (view.actions != null) for (String[] a : view.actions) {
                    Button bt = Ui.button(this, a[0], Ui.BLUE);
                    bt.setOnClickListener(v -> send(a[1], a[2].replace("{id}", id), "{}", this::load));
                    acts.addView(bt);
                }
                if (view.crud) {
                    Button ed = Ui.button(this, "Edit", Ui.BLUE), del = Ui.button(this, "Delete", Ui.RED);
                    ed.setOnClickListener(v -> askJson("Edit record (JSON)", o.toString(), json -> send("PUT", view.url + "/" + id, json, this::load)));
                    del.setOnClickListener(v -> new AlertDialog.Builder(this).setMessage("Delete / deactivate this record?")
                            .setPositiveButton("Yes", (d, w) -> send("DELETE", view.url + "/" + id, null, this::load)).setNegativeButton("No", null).show());
                    acts.addView(ed); acts.addView(del);
                }
                card.addView(acts);
            }
            parent.addView(card);
        }
    }

    private View kv(String key, String value, boolean editable) {
        LinearLayout r = Ui.row(this); r.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));
        r.addView(Ui.text(this, pretty(key), 12, Ui.MUTED, false), Ui.weight(1f));
        TextView v = Ui.text(this, value, 13, Ui.TEXT, false); v.setGravity(android.view.Gravity.END);
        r.addView(v, Ui.weight(1f));
        if (editable) r.setOnClickListener(x -> askJson("New value for " + key, value, nv -> {
            JsonObject body = new JsonObject(); body.addProperty(key, nv.replace("\"", "")); send("PUT", "api/settings", body.toString(), this::load); }, false));
        return r;
    }

    // ---------- helpers ----------
    private boolean isObjectArray(JsonElement e) { return e.isJsonArray() && e.getAsJsonArray().size() > 0 && e.getAsJsonArray().get(0).isJsonObject(); }

    private String val(JsonElement e) {
        if (e == null || e.isJsonNull()) return "";
        if (e.isJsonPrimitive()) return e.getAsString();
        if (e.isJsonObject()) {
            JsonObject o = e.getAsJsonObject();
            for (String k : new String[]{"name", "code", "username", "id"}) if (o.has(k)) return o.get(k).getAsString();
            return o.toString();
        }
        JsonArray a = e.getAsJsonArray();
        if (a.size() == 0) return "none";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(a.size(), 3); i++) sb.append(i > 0 ? "\n" : "").append(val(a.get(i)));
        return a.size() > 3 ? sb + "\n… +" + (a.size() - 3) + " more" : sb.toString();
    }

    private static String pretty(String k) {
        String s = k.replaceAll("([a-z0-9])([A-Z])", "$1 $2").replace(".", " · ");
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static int color(String s) {
        switch (s) {
            case "CRITICAL": case "HIGH": case "INACTIVE": case "RETIRED": return Ui.RED;
            case "MEDIUM": case "OPEN": return Ui.ORANGE;
            case "ACKNOWLEDGED": case "LOW": return Ui.BLUE;
            default: return Ui.GREEN;   // RESOLVED, ACTIVE, RUNNING ...
        }
    }

    private void askJson(String title, String initial, Consumer<String> ok) { askJson(title, initial, ok, true); }

    private void askJson(String title, String initial, Consumer<String> ok, boolean multiline) {
        EditText et = new EditText(this); et.setText(initial);
        et.setInputType(multiline ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE : InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this).setTitle(title).setView(et)
                .setPositiveButton("Save", (d, w) -> ok.accept(et.getText().toString())).setNegativeButton("Cancel", null).show();
    }

    private void send(String method, String url, String json, Runnable done) {
        JsonElement body;
        try { body = json == null ? null : new JsonParser().parse(json); } catch (Exception e) { Toast.makeText(this, "Invalid JSON", Toast.LENGTH_LONG).show(); return; }
        Api.Service api = Api.get(this);
        Call<ResponseBody> call = method.equals("PUT") ? api.put(url, body) : method.equals("DELETE") ? api.delete(url) : api.post(url, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                if (r.isSuccessful()) done.run();
                else Toast.makeText(ModuleActivity.this, r.code() == 403 ? "Your role is not allowed to do this" : "Failed (" + r.code() + ")", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t) { Toast.makeText(ModuleActivity.this, "Cannot reach server", Toast.LENGTH_LONG).show(); }
        });
    }
}
