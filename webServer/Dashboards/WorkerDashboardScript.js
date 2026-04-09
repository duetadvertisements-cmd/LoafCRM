async function getTickets () {  

    const username = sessionStorage.getItem ("username");
    const ticket = `username=${encodeURIComponent(username)}`;
    try {
        const serverResponse = await fetch("http://192.168.1.35:8080/getTicketsEmployee", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: ticket
        });
        const responseText = await serverResponse.text();

        console.log ("");
        console.log("Server response to getTickets ():");
        console.log(responseText);
        console.log ("");

        const tickets = JSON.parse (responseText);
        console.log ("-GETTICKETS ()-");
        console.log (tickets);
        console.log ("");

        displayTickets(tickets);
    }
    catch (error) {
        console.error ("error to get tickets");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
        console.error ("");
    }
}

function displayTickets(tickets) {
    const container = document.getElementById("ticketContainer");
    container.innerHTML = "";  // Clear previous tickets

    if (tickets.length === 0) {
        container.innerHTML = "<p class='no-tickets'>No tickets found</p>";
        return;
    }

    tickets.forEach(ticket => {
        const ticketDiv = document.createElement("div");
        ticketDiv.className = "ticket";
        ticketDiv.onclick = () => ticketClicked(ticket);
        ticketDiv.innerHTML = `
            <div class="ticket-header">
                <h3 class="ticket-title">${ticket.title}</h3>
                <span class="ticket-id">#${ticket.id}</span>
            </div>
            <p class="ticket-data">${ticket.data}</p>
            <p class="ticket-creator">Created by: ${ticket.creator}</p>
        `;
        container.appendChild(ticketDiv);
    });
}

getTickets ();

function ticketClicked(ticket) {
    sessionStorage.setItem ("currentTicket", JSON.stringify ({
        title: ticket.title,
        data: ticket.data,
        creator: ticket.creator,
        id: ticket.id,
        status: ticket.status
    }));
    sessionStorage.setItem ("dashboardHref", "dashboards/WorkerDashboard.html")
    window.location.href = "../ticketPage.html";
}

const themes = {
    basic: {
        bg1: "#667eea",
        bg2: "#49BC9B",
        mainBg: "white",

        navText: "#666",
        headingMain: "#333",
        headingAccent: "#5a67d8",
        headingBorder: "#ddd",

        ticketBg: "white",
        ticketBorder: "#49BC9B",
        ticketTitle: "#333",
        ticketText: "#555",
        ticketSmall: "#777"
    },
    royalLuxe: {
        bg1: "#4e54c8",
        bg2: "#8f94fb",
        mainBg: "#fafaff",

        navText: "#4a4e89",
        headingMain: "#2e3160",
        headingAccent: "#6a6efb",
        headingBorder: "#d7d9ff",

        ticketBg: "#ecebff",
        ticketBorder: "#6a6efb",
        ticketTitle: "black",
        ticketText: "black",
        ticketSmall: "#9d9ccc"
    },
    desert_slate: {
        "bg1": "#2d2926",
        "bg2": "#403a34",
        "main-bg": "#fffcf9",
        "nav-text": "#5d544b",
        "heading-main": "#1a1a1a",
        "heading-accent": "#d97757",
        "heading-border": "#e8e2db",
        "ticket-bg": "#ffffff",
        "ticket-border": "#d97757",
        "ticket-title": "#1a1a1a",
        "ticket-text": "#5d544b",
        "ticket-smalltext": "#a69d95"
    },
    cobalt_horizon: {
        "bg1": "#1e293b",
        "bg2": "#334155",
        "main-bg": "#ffffff",
        "nav-text": "#64748b",
        "heading-main": "#0f172a",
        "heading-accent": "#3b82f6",
        "heading-border": "#e2e8f0",
        "ticket-bg": "#ffffff",
        "ticket-border": "#3b82f6",
        "ticket-title": "#0f172a",
        "ticket-text": "#475569",
        "ticket-smalltext": "#94a3b8"
    },
    phantom_forest: {
        "bg1": "#0f1d1a",
        "bg2": "#1c3a34",
        "main-bg": "#fcfdfd",
        "nav-text": "#4a6b65",
        "heading-main": "#122621",
        "heading-accent": "#10b981",
        "heading-border": "#e6efed",
        "ticket-bg": "#ffffff",
        "ticket-border": "#10b981",
        "ticket-title": "#122621",
        "ticket-text": "#2d3735",
        "ticket-smalltext": "#7ca29a"
    }
}

function applyTheme(name) {
    const theme = themes[name];
    if (!theme) return; // Safety check

    for (let key in theme) {
        // If the key doesn't start with '--', add it
        const cssVar = key.startsWith('--') ? key : `--${key}`;
        document.documentElement.style.setProperty(cssVar, theme[key]);
    }
}
applyTheme ("cobalt_horizon");


