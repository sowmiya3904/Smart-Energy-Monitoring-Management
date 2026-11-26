package com.datamint.energy.android;

/** The 12 modules: which API endpoints each screen shows. Same list as the web modules page. */
public class Modules {
    public static class V {
        public final String name, url; public boolean crud; public String[][] actions;
        V(String n, String u) { name = n; url = u; }
        V crud() { crud = true; return this; }
        V actions(String[]... a) { actions = a; return this; }
    }

    public static class M {
        public final String key, title, icon; public final V[] views; public String[][] tools; public boolean settings;
        M(String k, String t, String i, V... v) { key = k; title = t; icon = i; views = v; }
        M tools(String[]... t) { tools = t; return this; }
        M settings() { settings = true; return this; }
    }

    private static V v(String n, String u) { return new V(n, u); }

    public static final M[] ALL = {
            new M("dashboard", "Dashboard Management", "📈"),
            new M("monitor", "Real-Time Energy Monitoring", "⚡",
                    v("Latest", "api/readings/latest?limit=20"), v("History", "api/readings?size=30"), v("Missing data", "api/readings/missing?days=30")),
            new M("buildings", "Building & Department Management", "🏢",
                    v("Buildings", "api/buildings").crud(), v("Departments", "api/departments").crud(), v("Meters", "api/meters").crud()),
            new M("analysis", "Energy Consumption Analysis", "📊",
                    v("Daily", "api/analytics/consumption?period=daily&days=30"), v("Weekly", "api/analytics/consumption?period=weekly&days=90"),
                    v("Monthly", "api/analytics/consumption?period=monthly&days=183"), v("Peak / off-peak", "api/analytics/peak-offpeak"),
                    v("Load type", "api/analytics/load-type"), v("Departments", "api/analytics/department-wise")),
            new M("cost", "Electricity Cost Management", "💰",
                    v("Oct 2026 bill", "api/cost/estimate?month=2026-10"), v("Monthly comparison", "api/cost/monthly-comparison"),
                    v("Tariffs", "api/tariffs").crud(), v("Actual bills", "api/bills").crud()),
            new M("alerts", "Smart Alerts & Notifications", "🔔",
                    v("Open", "api/alerts?status=OPEN").actions(new String[]{"Acknowledge", "PUT", "api/alerts/{id}/acknowledge"}, new String[]{"Resolve", "PUT", "api/alerts/{id}/resolve"}),
                    v("All", "api/alerts").actions(new String[]{"Resolve", "PUT", "api/alerts/{id}/resolve"}))
                    .tools(new String[]{"Scan last 7 days", "POST", "api/alerts/scan?days=7"}),
            new M("optimization", "Energy Optimization", "💡", v("Recommendations", "api/optimization/recommendations")),
            new M("carbon", "Carbon Footprint Management", "🌿", v("Last 30 days", "api/carbon/summary?days=30")),
            new M("equipment", "Equipment Management", "🏭", v("Equipment", "api/equipment").crud()),
            new M("reports", "Reports & Documentation", "📄",
                    v("Daily", "api/reports/daily"), v("Monthly", "api/reports/monthly"), v("Cost", "api/reports/cost"),
                    v("Efficiency", "api/reports/efficiency"), v("Alerts", "api/reports/alerts")),
            new M("users", "User & Access Management", "👤", v("Users", "api/users").crud(), v("Audit log", "api/audit")),
            new M("settings", "System Settings", "⚙️", v("Settings", "api/settings")).settings()
    };

    public static M find(String key) {
        for (M m : ALL) if (m.key.equals(key)) return m;
        return ALL[1];
    }
}
