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
    document.getElementById("empClosed").textContent = emp.sucessfullyClosed;
}
loadEmployee ();

function goBack() {
    const goBackLocation = sessionStorage.getItem ("dashboardHref");
    window.location.href = goBackLocation;
}

function displayEmployees(employees) {
    const container = document.getElementById("employeeContainer");
    container.innerHTML = "";

    if (!employees || employees.length === 0) {
        container.innerHTML = "<p>No employees found.</p>";
        return;
    }

    employees.forEach(emp => {
        const card = document.createElement("div");
        card.className = "employee-card";

        card.innerHTML = `
            <div class="employee-top-strip"></div>

            <h3 class="employee-name">Employee #${emp.id}</h3>

            <p class="employee-username">
                <strong>Username:</strong> ${emp.username}
            </p>

            <p class="employee-email">
                <strong>Email:</strong> ${emp.email}
            </p>

            <p class="employee-closed">
                <strong>Successfully Closed:</strong> ${emp.sucessfullyClosed}
            </p>
        `;

        container.appendChild(card);
    });
}

const themes = {
    basic: {
        "--bg1": "#667eea",
        "--bg2": "#49BC9B",
        "--card-bg": "#ffffff",
        "--card-border": "#e0e0e0",
        "--card-top-strip": "#49BC9B",

        "--ticket-title": "#222",
        "--ticket-text": "#444",
        "--ticket-id-bg": "linear-gradient(135deg, #667eea, #49BC9B)",

        "--comment-bg": "#fafafa",
        "--comment-title": "#333",
        "--comment-text": "#555",
        "--comment-border": "#ddd",

        "--button-bg": "#667eea",
        "--button-hover": "#5a67d8",
        "--button-text": "#fff",

        "--input-bg": "#fff",
        "--input-border": "#ccc",
        "--input-focus": "#667eea"
    },

    sunsetPro: {
        "--bg1": "#ff9966",
        "--bg2": "#ff5e62",
        "--card-bg": "#fff7f3",
        "--card-border": "#ffb199",
        "--card-top-strip": "#ff5e62",

        "--ticket-title": "#662200",
        "--ticket-text": "#773300",
        "--ticket-id-bg": "linear-gradient(135deg, #ff9966, #ff5e62)",

        "--comment-bg": "#fff1e8",
        "--comment-title": "#552200",
        "--comment-text": "#663300",
        "--comment-border": "#ffb199",

        "--button-bg": "#ff5e62",
        "--button-hover": "#ff3b42",
        "--button-text": "#fff",

        "--input-bg": "#fff7f3",
        "--input-border": "#ffb199",
        "--input-focus": "#ff5e62"
    },

    aquaBreeze: {
        "--bg1": "#6dd5ed",
        "--bg2": "#2193b0",
        "--card-bg": "#f0fcff",
        "--card-border": "#a8e9f6",
        "--card-top-strip": "#2193b0",

        "--ticket-title": "#003a4d",
        "--ticket-text": "#004d66",
        "--ticket-id-bg": "linear-gradient(135deg, #6dd5ed, #2193b0)",

        "--comment-bg": "#e6f9ff",
        "--comment-title": "#003644",
        "--comment-text": "#005066",
        "--comment-border": "#a8e9f6",

        "--button-bg": "#2193b0",
        "--button-hover": "#1a6f85",
        "--button-text": "#fff",

        "--input-bg": "#f0fcff",
        "--input-border": "#a8e9f6",
        "--input-focus": "#2193b0"
    },

    emeraldMist: {
        "--bg1": "#00b894",
        "--bg2": "#55efc4",
        "--card-bg": "#f3fffa",
        "--card-border": "#a5f2d6",
        "--card-top-strip": "#00b894",

        "--ticket-title": "#004d3d",
        "--ticket-text": "#006652",
        "--ticket-id-bg": "linear-gradient(135deg, #00b894, #55efc4)",

        "--comment-bg": "#e8fff7",
        "--comment-title": "#004033",
        "--comment-text": "#00604d",
        "--comment-border": "#a5f2d6",

        "--button-bg": "#00b894",
        "--button-hover": "#009a7a",
        "--button-text": "#fff",

        "--input-bg": "#f3fffa",
        "--input-border": "#a5f2d6",
        "--input-focus": "#00b894"
    },

    royalLuxe: {
        "--bg1": "#654ea3",
        "--bg2": "#eaafc8",
        "--card-bg": "#faf5ff",
        "--card-border": "#d8b7ff",
        "--card-top-strip": "#654ea3",

        "--ticket-title": "#2f1b52",
        "--ticket-text": "#3c2466",
        "--ticket-id-bg": "linear-gradient(135deg, #654ea3, #eaafc8)",

        "--comment-bg": "#f4ebff",
        "--comment-title": "#2c1a4b",
        "--comment-text": "#3a2362",
        "--comment-border": "#d8b7ff",

        "--button-bg": "#654ea3",
        "--button-hover": "#513d85",
        "--button-text": "#fff",

        "--input-bg": "#faf5ff",
        "--input-border": "#d8b7ff",
        "--input-focus": "#654ea3"
    },

    cyberNight2: {
        "--bg1": "#0a0f24",
        "--bg2": "#111d4a",
        "--card-bg": "#141a33",
        "--card-border": "#1e2b55",
        "--card-top-strip": "#00eaff",

        "--ticket-title": "#d6eaff",
        "--ticket-text": "#b6c9ff",
        "--ticket-id-bg": "linear-gradient(135deg, #00eaff, #0088ff)",

        "--comment-bg": "#10162c",
        "--comment-title": "#c5d8ff",
        "--comment-text": "#a6bbff",
        "--comment-border": "#1e2b55",

        "--button-bg": "#0088ff",
        "--button-hover": "#006bd1",
        "--button-text": "#fff",

        "--input-bg": "#0d142b",
        "--input-border": "#1e2b55",
        "--input-focus": "#00eaff",
        "--ticket-meta-1": "white"
    }
};

function applyTheme(name) {
    const theme = themes[name];
    Object.entries(theme).forEach(([key, value]) => {
        document.documentElement.style.setProperty(key, value);
    });
}
applyTheme ("royalLuxe");