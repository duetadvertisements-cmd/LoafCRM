/* ==========================================
   DASHBOARD SCRIPT - FULL FIXED VERSION
   Keeps all functionality working
   ========================================== */

const BASE_URL = "http://192.168.1.34:8080";

/* ==========================================
   HELPERS
   ========================================== */

function escapeHTML(str) {
    if (str == null) return "";
    return String(str)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function getStoredTickets() {
    const raw = sessionStorage.getItem("tickets");

    if (!raw) return [];

    try {
        return JSON.parse(raw);
    } catch {
        return [];
    }
}

function setStoredTickets(tickets) {
    sessionStorage.setItem("tickets", JSON.stringify(tickets));
}

/* ==========================================
   WELCOME TITLE
   ========================================== */

function setWelcomeTitle() {
    const username = sessionStorage.getItem("username") || "User";
    const el = document.getElementById("welcomeTitle");

    if (el) {
        el.innerText = "Welcome, " + username;
    }
}

setWelcomeTitle();

/* ==========================================
   SECTION NAVIGATION
   ========================================== */

function showSection(sectionName, event = null) {

    const sections = document.querySelectorAll(".content-section");
    sections.forEach(section => section.classList.remove("active"));

    const navItems = document.querySelectorAll(".nav-item");
    navItems.forEach(item => item.classList.remove("active"));

    const target = document.getElementById(sectionName + "-section");

    if (target) {
        target.classList.add("active");
    }

    if (event && event.target) {
        const clicked = event.target.closest(".nav-item");

        if (clicked) {
            clicked.classList.add("active");
        }
    }

    if (sectionName === "dashboard") {
        if (typeof loadDashboardStats === "function") {
            loadDashboardStats();
        }
    }
}

/* ==========================================
   LOGOUT
   ========================================== */

function logout() {
    sessionStorage.clear();
    localStorage.removeItem("clockedIntoATicket");
    window.location.href = "../index.HTML";
}

/* ==========================================
   MOBILE MENU
   ========================================== */

function toggleMobileMenu() {

    const sidebar = document.getElementById("sidebar");
    const overlay = document.querySelector(".sidebar-overlay");

    if (sidebar) sidebar.classList.toggle("open");
    if (overlay) overlay.classList.toggle("active");
}

/* ==========================================
   TICKET CLICKED
   ========================================== */

function ticketClicked(ticket) {

    sessionStorage.setItem("currentTicket", JSON.stringify(ticket));

    sessionStorage.setItem(
        "dashboardHref",
        "dashboards/dashboard.html"
    );

    window.location.href = "../ticketPage.html";
}

/* ==========================================
   ADD TICKET
   ========================================== */

async function addTicket() {

    const title =
        document.getElementById("titleInput")?.value.trim() || "";

    const data =
        document.getElementById("dataInput")?.value.trim() || "";

    const creator =
        sessionStorage.getItem("username") || "";

    const email =
        sessionStorage.getItem("email") || "";

    if (!title || !data) {
        alert("Please fill all fields.");
        return;
    }

    const body =
        `title=${encodeURIComponent(title)}` +
        `&data=${encodeURIComponent(data)}` +
        `&creator=${encodeURIComponent(creator)}` +
        `&toEmail=${encodeURIComponent(email)}` +
        `&madeBy=${encodeURIComponent(creator)}`;

    try {

        const response = await fetch(`${BASE_URL}/addTicket`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body
        });

        const text = await response.text();

        console.log("Add Ticket Response:", text);

        if (text === "SUCCESS") {

            alert("Success");

            await getTickets(); // refresh tickets

            // optional clear form
            document.getElementById("titleInput").value = "";
            document.getElementById("dataInput").value = "";

            // optional: only if you WANT full reload
            // location.reload();

        } else {
            alert("Failed");
        }

    } catch (error) {
        console.error("Failed to add ticket:", error);
        alert("Server Error");
    }
}

/* ==========================================
   GET TICKETS
   ========================================== */

async function getTickets() {

    const username =
        sessionStorage.getItem("username") || "";

    const body =
        `username=${encodeURIComponent(username)}`;

    try {

        const response = await fetch(`${BASE_URL}/getTickets`, {
            method: "POST",
            headers: {
                "Content-Type":
                    "application/x-www-form-urlencoded"
            },
            body
        });

        const text = await response.text();

        console.log("Get Tickets Response:", text);

        const tickets = JSON.parse(text);

        setStoredTickets(tickets);

        displayTickets(tickets, "ALL");

    } catch (error) {
        console.error("Failed to get tickets:", error);
    }
}

/* ==========================================
   GET EMAIL
   ========================================== */

async function getEmail() {

    const username =
        sessionStorage.getItem("username") || "";

    const body =
        `username=${encodeURIComponent(username)}`;

    try {

        const response = await fetch(`${BASE_URL}/getEmail`, {
            method: "POST",
            headers: {
                "Content-Type":
                    "application/x-www-form-urlencoded"
            },
            body
        });

        const text = await response.text();

        console.log("Email:", text);

        sessionStorage.setItem("email", text);

    } catch (error) {
        console.error("Failed to get email:", error);
    }
}

getEmail();

/* ==========================================
   DISPLAY TICKETS
   ========================================== */

function displayTickets(tickets, filter = "ALL") {

    const container =
        document.getElementById("ticketContainer");

    if (!container) return;

    container.innerHTML = "";

    if (!Array.isArray(tickets) || tickets.length === 0) {
        container.innerHTML =
            "<p class='no-tickets'>No tickets found</p>";
        return;
    }

    let filtered = tickets;

    if (filter === "OPEN") {
        filtered = tickets.filter(
            t => Number(t.status) === 1
        );
    }

    if (filter === "CLOSED") {
        filtered = tickets.filter(
            t => Number(t.status) === 2
        );
    }

    if (filtered.length === 0) {
        container.innerHTML =
            "<p class='no-tickets'>No tickets found</p>";
        return;
    }

    filtered.forEach(ticket => {
        renderTicket(ticket, container);
    });
}

/* ==========================================
   RENDER SINGLE TICKET
   ========================================== */

function renderTicket(ticket, container) {

    const status =
        Number(ticket.status) === 1
            ? "Open"
            : "Closed";

    const div = document.createElement("div");

    div.className = "ticket";

    div.onclick = () => ticketClicked(ticket);

    div.innerHTML = `
        <div class="ticket-header">
            <h3 class="ticket-title">
                ${escapeHTML(ticket.title)}
            </h3>

            <span class="ticket-id">
                #${ticket.id}
            </span>
        </div>

        <p class="ticket-data">
            ${escapeHTML(ticket.data)}
        </p>

        <p class="ticket-creator">
            Created by:
            ${escapeHTML(ticket.creator)}
        </p>

        <p>Status: ${status}</p>
    `;

    container.appendChild(div);
}

/* ==========================================
   LOAD TICKETS ON START
   ========================================== */

(function initTickets() {

    const stored = getStoredTickets();

    if (stored.length > 0) {
        displayTickets(stored, "ALL");
    } else {
        getTickets();
    }
})();

/* ==========================================
   THEMES
   ========================================== */

const themes = {
    basic: {
        dshbackground1: "#667eea",
        dshbackground2: "#49BC9B",
        dshbackground3: "#ffffff",
        dshnav1: "#666",
        dshhead1: "#333",
        dshhead2: "#5a67d8",
        dshhead3: "#ddd",
        dshticket1: "#eee",
        dshticket2: "#999"
    },

    royalLuxe: {
        dshbackground1: "#4e54c8",
        dshbackground2: "#8f94fb",
        dshbackground3: "#fafaff",
        dshnav1: "#4a4e89",
        dshhead1: "#2e3160",
        dshhead2: "#6a6efb",
        dshhead3: "#d7d9ff",
        dshticket1: "#ecebff",
        dshticket2: "#9d9ccc"
    },

    desert_slate: {
        dshbackground1: "#2d2926",
        dshbackground2: "#403a34",
        dshbackground3: "#fffcf9",
        dshnav1: "#5d544b",
        dshhead1: "#1a1a1a",
        dshhead2: "#d97757",
        dshhead3: "#e8e2db",
        dshticket1: "#f2ede7",
        dshticket2: "#a69d95"
    },

    cobalt_horizon: {
        dshbackground1: "#1e293b",
        dshbackground2: "#334155",
        dshbackground3: "#ffffff",
        dshnav1: "#64748b",
        dshhead1: "#0f172a",
        dshhead2: "#3b82f6",
        dshhead3: "#e2e8f0",
        dshticket1: "#f1f5f9",
        dshticket2: "#94a3b8"
    },

    phantom_forest: {
        dshbackground1: "#0f1d1a",
        dshbackground2: "#1c3a34",
        dshbackground3: "#fcfdfd",
        dshnav1: "#4a6b65",
        dshhead1: "#122621",
        dshhead2: "#10b981",
        dshhead3: "#e6efed",
        dshticket1: "#f0f7f5",
        dshticket2: "#7ca29a"
    }
};

function applyTheme(name) {

    const theme = themes[name];

    if (!theme) return;

    for (const key in theme) {
        document.documentElement.style.setProperty(
            `--${key}`,
            theme[key]
        );
    }
}

/* Uses saved theme or fallback */
applyTheme(
    localStorage.getItem("preferredTheme")
        || "cobalt_horizon"
);

/* ==========================================
   FILTER BUTTONS
   ========================================== */

function changeFilter(filter) {

    const tickets = getStoredTickets();

    displayTickets(tickets, filter);
}