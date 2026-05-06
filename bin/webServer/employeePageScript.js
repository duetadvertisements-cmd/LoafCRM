function loadEmployee() {
    const empJSON = sessionStorage.getItem("currentEmployee");

    if (!empJSON) {
        console.error("No employee found in sessionStorage");
        return;
    }

    const emp = JSON.parse(empJSON);

    // Display the employee
    document.getElementById("empId").textContent = emp.id;
    document.getElementById("empUsername").textContent = emp.username;
    document.getElementById("empEmail").textContent = emp.email;
    document.getElementById("empExpectedHours").textContent = emp.hoursAssigned;
}
loadEmployee ();

function goBack() {
    const goBackLocation = sessionStorage.getItem ("dashboardHref");
    window.location.href = goBackLocation;
}

const themes = {
    basic: {
        "bg1": "#667eea",
        "bg2": "#49BC9B",
        "card-bg": "#ffffff",
        "card-border": "#49BC9B",
        "card-top-strip": "#667eea",
        "ticket-title": "#333",
        "ticket-text": "#555",
        "meta-strong": "#777",
        "button-bg": "#667eea",
        "button-hover": "#5a67d8",
        "button-text": "#ffffff",
        "input-bg": "#f7f9fc",
        "input-border": "#ddd"
    },
    royalLuxe: {
        "bg1": "#4e54c8",
        "bg2": "#8f94fb",
        "card-bg": "#ffffff",
        "card-border": "#8f94fb",
        "card-top-strip": "#4e54c8",
        "ticket-title": "#2e3160",
        "ticket-text": "#4a4e89",
        "meta-strong": "#9d9ccc",
        "button-bg": "#6a6efb",
        "button-hover": "#4e54c8",
        "button-text": "#ffffff",
        "input-bg": "#f5f6ff",
        "input-border": "#d7d9ff"
    },
    desert_slate: {
        "bg1": "#2d2926",
        "bg2": "#403a33",
        "card-bg": "#ffffff",
        "card-border": "#d97757",
        "card-top-strip": "#2d2926",
        "ticket-title": "#1a1a1a",
        "ticket-text": "#5d544b",
        "meta-strong": "#a69d95",
        "button-bg": "#d97757",
        "button-hover": "#bf6336",
        "button-text": "#ffffff",
        "input-bg": "#fdfaf7",
        "input-border": "#e8e2db"
    },
    cobalt_horizon: {
        "bg1": "#1e293b",
        "bg2": "#333155",
        "card-bg": "#ffffff",
        "card-border": "#3b82f6",
        "card-top-strip": "#1e293b",
        "ticket-title": "#0f172a",
        "ticket-text": "#475569",
        "meta-strong": "#94a3b8",
        "button-bg": "#3b82f6",
        "button-hover": "#2563eb",
        "button-text": "#ffffff",
        "input-bg": "#f1f5f9",
        "input-border": "#e2e8f0"
    },
    phantom_forest: {
        "bg1": "#0f1d1a",
        "bg2": "#1c3a33",
        "card-bg": "#ffffff",
        "card-border": "#10b981",
        "card-top-strip": "#0f1d1a",
        "ticket-title": "#122621",
        "ticket-text": "#2d3733",
        "meta-strong": "#7ca29a",
        "button-bg": "#10b981",
        "button-hover": "#059669",
        "button-text": "#ffffff",
        "input-bg": "#f9fcfb",
        "input-border": "#e6efed"
    }
};

function applyTheme(name) {
    const theme = themes[name];
    if (!theme) return;
    
    for (const key in theme) {
        // Automatically adds the -- prefix
        document.documentElement.style.setProperty(`--${key}`, theme[key]);
    }
}

// Call it to test
applyTheme("desert_slate");