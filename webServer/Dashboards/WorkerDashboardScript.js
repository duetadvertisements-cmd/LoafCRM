const themes = {
    cobalt_horizon: { bg1: "#1e293b", bg2: "#334155", "main-bg": "#ffffff", "heading-accent": "#3b82f6", "ticket-bg": "#ffffff", "ticket-border": "#3b82f6" },
    royalLuxe: { bg1: "#4e54c8", bg2: "#8f94fb", "main-bg": "#fafaff", "heading-accent": "#6a6efb", "ticket-bg": "#ecebff", "ticket-border": "#6a6efb" },
    phantom_forest: { bg1: "#0f1d1a", bg2: "#1c3a34", "main-bg": "#fcfdfd", "heading-accent": "#10b981", "ticket-bg": "#ffffff", "ticket-border": "#10b981" },
    desert_slate: { bg1: "#2d2926", bg2: "#403a34", "main-bg": "#fffcf9", "heading-accent": "#d97757", "ticket-bg": "#ffffff", "ticket-border": "#d97757" },
    basic: { bg1: "#667eea", bg2: "#49BC9B", "main-bg": "white", "heading-accent": "#5a67d8", "ticket-bg": "white", "ticket-border": "#49BC9B" }
};

document.addEventListener("DOMContentLoaded", () => {
    applyTheme(localStorage.getItem("preferredTheme") || "cobalt_horizon");
    getTickets();
});

function toggleMenu() {
    document.getElementById('navMenu').classList.toggle('open');
    document.querySelector('.sidebar-overlay').classList.toggle('active');
}

function showSection(name) {
    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

    document.getElementById(`${name}-section`).classList.add('active');
    document.getElementById(`nav-${name}`).classList.add('active');

    if (name === 'settings') loadSettingsData();
    toggleMenu(); // Close menu after selection
}

function applyTheme(name) {
    const theme = themes[name];
    if (!theme) return;
    for (let key in theme) {
        document.documentElement.style.setProperty(`--${key}`, theme[key]);
    }
    localStorage.setItem("preferredTheme", name);
}

async function getTickets() {
    const username = sessionStorage.getItem("username");
    try {
        const response = await fetch("http://192.168.1.36:8080/getTicketsEmployee", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `username=${encodeURIComponent(username)}`
        });
        const tickets = JSON.parse(await response.text());
        displayTickets(tickets);
    } catch (e) { console.error("Server Error"); }
}

function displayTickets(tickets) {
    const container = document.getElementById("ticketContainer");
    container.innerHTML = tickets.length ? "" : "<p>No tickets found.</p>";
    tickets.forEach(ticket => {
        const div = document.createElement("div");
        div.className = "ticket";
        div.onclick = () => {
            sessionStorage.setItem("currentTicket", JSON.stringify(ticket));
            sessionStorage.setItem ("dashboardHref", "dashboards/WorkerDashboard.html")
            window.location.href = "../ticketPage.html";
        };
        div.innerHTML = `<h3>${ticket.title}</h3><p>${ticket.data}</p><small>ID: #${ticket.id}</small>`;
        container.appendChild(div);
    });
}

function loadSettingsData() {
    document.getElementById("settings-username").value = sessionStorage.getItem("username") || "";
    document.getElementById("settings-email").value = sessionStorage.getItem("email") || "";
}

function logout() {
    sessionStorage.clear();
    window.location.href = '../index.HTML';
}

async function clockIn () {
    const formData = `userID=${encodeURIComponent(sessionStorage.getItem ("userID"))}`;
    try {
        const response = await fetch ("http://192.168.1.36:8080/clockIn", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded"},
            body: formData
        });
        console.log ("Clocked In: "+await response.text ());
    }
    catch (error) {
        console.error (error);
    }
}

async function clockOut () {
    const formData = `userID=${encodeURIComponent(sessionStorage.getItem ("userID"))}`;
    try {
        const response = await fetch ("http://192.168.1.36:8080/clockOut", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded"},
            body: formData
        });
        console.log ("Clocked Out: "+await response.text ());
    }
    catch (error) {
        console.error (error);
    }
}

async function getUserID () {
    const formData = `username=${encodeURIComponent (sessionStorage.getItem ("username"))}`
    try {
        const response = await fetch ("http://192.168.1.36:8080/getUserID", {
            method: "POST",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: formData
        });
        console.log ("Got User ID");
        sessionStorage.setItem ("userID", await response.text ());
    }
    catch (error) {
        console.error (error);
    }
}
getUserID ();

if (!localStorage.getItem("clockedIn")) {
    clockIn();
    localStorage.setItem("clockedIn", "true");
}

window.addEventListener("beforeunload", () => {
    clockOut ();
});