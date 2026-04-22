function setWelcomeTitle () {
    document.getElementById ("welcomeTitle").innerText = "Welcome, " + sessionStorage.getItem ("username");
}
setWelcomeTitle ();

// Navigation functions
function showSection(sectionName, event) {
    // Hide all sections
    const sections = document.querySelectorAll('.content-section');
    sections.forEach(section => section.classList.remove('active'));

    // Remove active class from all nav items
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => item.classList.remove('active'));

    // Show selected section
    const targetSection = document.getElementById(sectionName + '-section');
    if (targetSection) {
        targetSection.classList.add('active');
    }

    // Add active class to clicked nav item, if event exists
    if (event && event.target) {
        const clickedNav = event.target.closest('.nav-item');
        if (clickedNav) clickedNav.classList.add('active');
    }

    // Load data for specific sections
    if (sectionName === 'dashboard') {
        loadDashboardStats();
    }
}

function logout() {
    sessionStorage.clear();
    window.location.href = '../index.HTML';
}

// Mobile menu functions
function toggleMobileMenu() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.querySelector('.sidebar-overlay');

    sidebar.classList.toggle('open');
    overlay.classList.toggle('active');
}

function ticketClicked(ticket) {
    sessionStorage.setItem ("currentTicket", JSON.stringify ({
        title: ticket.title,
        data: ticket.data,
        creator: ticket.creator,
        id: ticket.id,
        status: ticket.status
    }));
    sessionStorage.setItem ("dashboardHref", "dashboards/dashboard.html")
    window.location.href = "../ticketPage.html";
}

// Close mobile menu when clicking outside or on a nav item
document.addEventListener('click', function(event) {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.querySelector('.sidebar-overlay');
    const mobileToggle = document.querySelector('.mobile-menu-toggle');

    if (!sidebar.contains(event.target) &&
        !mobileToggle.contains(event.target) &&
        sidebar.classList.contains('open')) {
    }
});

async function addTicket () {
    const title = document.getElementById ("titleInput").value;
    const data = document.getElementById ("dataInput").value;
    const creator = sessionStorage.getItem ("username");

    const ticket = `title=${encodeURIComponent(title)}&data=${encodeURIComponent(data)}&creator=${encodeURIComponent(creator)}&toEmail=${encodeURIComponent(sessionStorage.getItem ("email"))}`;    

    try {
        const serverResponse = await fetch("http://192.168.1.34:8080/addTicket", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: ticket
        });

        const responseText = await serverResponse.text();

        console.log ("------------------------------------------------------");
        console.log("Server response to addTicket:");
        console.log(responseText);
        console.log ("------------------------------------------------------");

        if (responseText == "SUCCESS") {
            alert ("Success");
        }

        else {
            alert ("Failed");
        }

        history.go ();

    } catch (error) {
        console.log ("---------------------error to addtickets ()---------------------------------");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
        console.log ("------------------------------------------------------");
    }
}

async function getTickets () {  

    const username = sessionStorage.getItem ("username");
    const ticket = `username=${encodeURIComponent(username)}`;
    try {
        const serverResponse = await fetch("http://192.168.1.34:8080/getTickets", {
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

async function getEmail () {  

    const username = sessionStorage.getItem ("username");
    const ticket = `username=${encodeURIComponent (sessionStorage.getItem ("username"))}`
    try {
        const serverResponse = await fetch("http://192.168.1.34:8080/getEmail", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: ticket
        });
        const responseText = await serverResponse.text();

        console.log ("------------------------------------------------------");
        console.log("Server response to getEmail ():");
        console.log(responseText);
        console.log ("------------------------------------------------------");

        sessionStorage.setItem ("email", responseText);
    }
    catch (error) {
        console.error ("----------------------error to get email--------------------------------");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
        console.error ("------------------------------------------------------");
    }
}
getEmail ();

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

const themes = {
    basic: { //GOOD
        dshbackground1: "#667eea",
        dshbackground2: "#49BC9B",
        dshbackground3: "white",
        dshnav1: "#666",
        dshhead1: "#333",
        dshhead2: "#5a67d8",
        dshhead3: "#ddd",
        dshticket1: "#eee" ,
        dshticket2: "#999"
    },
    royalLuxe: { //GOOD
        dshbackground1: "#4e54c8",
        dshbackground2: "#8f94fb",
        dshbackground3: "#fafaff",
        dshnav1: "#4a4e89",
        dshhead1: "#2e3160",
        dshhead2: "#6a6efb",
        dshhead3: "#d7d9ff",
        dshticket1: "#ecebff",
        dshticket2: "#9d9ccc",
        tickettitle: "black",
        tickettext: "black",
    },
    desert_slate: { //GOOD
        dshbackground1: "#2d2926", // Charcoal Umber
        dshbackground2: "#403a34", // Warm Slate
        dshbackground3: "#fffcf9", // Paper White
        dshnav1: "#5d544b",        // Deep Taupe
        dshhead1: "#1a1a1a",        // Ink Black
        dshhead2: "#d97757",        // Terracotta
        dshhead3: "#e8e2db",        // Sand Border
        dshticket1: "#f2ede7",      // Light Grain
        dshticket2: "#a69d95"       // Stone Gray
    },
    cobalt_horizon: { //GOOD
        dshbackground1: "#1e293b", // Deep Navy
        dshbackground2: "#334155", // Slate Blue
        dshbackground3: "#ffffff", // Clean White Card (contrast)
        dshnav1: "#64748b",        // Cool Slate
        dshhead1: "#0f172a",        // Midnight Text
        dshhead2: "#3b82f6",        // Bright Azure Accent
        dshhead3: "#e2e8f0",        // Soft Border
        dshticket1: "#f1f5f9",      // Ice Gray
        dshticket2: "#94a3b8"       // Muted Steel
    },
    phantom_forest: { //GOOD
        dshbackground1: "#0f1d1a", // Dark Forest
        dshbackground2: "#1c3a34", // Deep Teal
        dshbackground3: "#fcfdfd", // Off-White Card
        dshnav1: "#4a6b65",        // Muted Sage
        dshhead1: "#122621",        // Deep Spruce Text
        dshhead2: "#10b981",        // Emerald Pop
        dshhead3: "#e6efed",        // Pale Mint Border
        dshticket1: "#f0f7f5",      // Soft Mint Wash
        dshticket2: "#7ca29a"       // Dusty Teal
    },
}

function applyTheme(name) {
  const theme = themes[name];
  for (let key in theme) {
    document.documentElement.style.setProperty(`--${key}`, theme[key]);
  }
}
applyTheme ("royalLuxe"); 



async function postForm (url, dataObj) {
    const body = new URLSearchParams(dataObj).toString();

    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: body
    });

    return await response.text();
}

