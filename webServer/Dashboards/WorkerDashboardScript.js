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

        console.log ("------------------------------------------------------");
        console.log("Server response to getTickets ():");
        console.log(responseText);
        console.log ("------------------------------------------------------");

        const tickets = JSON.parse (responseText);
        console.log ("-----------------------GETTICKETS ()-------------------------------");
        console.log (tickets);
        console.log ("------------------------------------------------------");

        displayTickets(tickets);
    }
    catch (error) {
        console.error ("----------------------error to get tickets--------------------------------");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
        console.error ("------------------------------------------------------");
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
    sunsetPro: {
        bg1: "#ff6a3d",
        bg2: "#ff9a3d",
        mainBg: "#fff7f3",

        navText: "#6b3b2f",
        headingMain: "#3c1f15",
        headingAccent: "#ff7a55",
        headingBorder: "#f2d5c8",

        ticketBg: "#f7e3d8",
        ticketBorder: "#ff9a3d",
        ticketTitle: "white",
        ticketText: "white",
        ticketSmall: "#a77d6e"
    },
    aquaBreeze: {
        bg1: "#009ffd",
        bg2: "#2a2a72",
        mainBg: "#ffffff",

        navText: "#4d5d7a",
        headingMain: "#212b40",
        headingAccent: "#2678ff",
        headingBorder: "#d0d8e8",

        ticketBg: "#e8f1ff",
        ticketBorder: "#2678ff",
        ticketTitle: "white",
        ticketText: "white",
        ticketSmall: "#7e8cab"
    },
    emeraldMist: {
        bg1: "#0bab64",
        bg2: "#3bb78f",
        mainBg: "#f6fffa",

        navText: "#3b5c52",
        headingMain: "#233c34",
        headingAccent: "#0abf74",
        headingBorder: "#cfe8dd",

        ticketBg: "#e4f7ef",
        ticketBorder: "#0abf74",
        ticketTitle: "white",
        ticketText: "white",
        ticketSmall: "#84a797"
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
    cyberNight2: {
        bg1: "#1f2b3a",
        bg2: "#354a62",
        mainBg: "#0f1622",

        navText: "#4e5f77",
        headingMain: "#c7d2e5",
        headingAccent: "#3ca3ff",
        headingBorder: "#2a3545",

        ticketBg: "#1c2533",
        ticketBorder: "#3ca3ff",
        ticketTitle: "white",
        ticketText: "white",
        ticketSmall: "#6e7f99"
    }
}

function applyTheme(name) {
  const theme = themes[name];
  for (let key in theme) {
    document.documentElement.style.setProperty(`--${key}`, theme[key]);
  }
}
applyTheme ("royalLuxe");


