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

    const ticket = `title=${encodeURIComponent(title)}&data=${encodeURIComponent(data)}&creator=${encodeURIComponent(creator)}`;    

    try {
        const serverResponse = await fetch("http://192.168.1.33:8080/addTicket", {
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
        const serverResponse = await fetch("http://192.168.1.33:8080/getTickets", {
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

async function loadDashboardStats() {
    const username = sessionStorage.getItem("username");
    if (!username) {
        console.warn("No username in session, skipping dashboard stats.");
        return;
    }

    try {   
        const response = await fetch("http://192.168.1.33:8080/getTickets", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `username=${encodeURIComponent(username)}`
        });

        const text = await response.text();
        const tickets = JSON.parse(text || "[]");

        const totalTickets = tickets.length;
        const completedTasks = tickets.filter(ticket => {
            const status = ticket.status || "";
            return status.toString().toLowerCase().includes("complete") || status.toString().toLowerCase().includes("done");
        }).length;

        const totalTicketsEl = document.getElementById('totalTickets');
        const activeUsersEl = document.getElementById('activeUsers');
        const completedTasksEl = document.getElementById('completedTasks');

        if (totalTicketsEl) totalTicketsEl.textContent = totalTickets;
        if (activeUsersEl) activeUsersEl.textContent = Math.max(1, totalTickets > 0 ? 1 : 0).toString();
        if (completedTasksEl) completedTasksEl.textContent = completedTasks;

    } catch (error) {
        console.error('Failed to load dashboard stats:', error);
    }
}

getTickets ();

const themes = {
    basic: {
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
    sunsetPro: {
        dshbackground1: "#ff6a3d",
        dshbackground2: "#ff9a3d",
        dshbackground3: "#fff7f3",
        dshnav1: "#6b3b2f",
        dshhead1: "#3c1f15",
        dshhead2: "#ff7a55",
        dshhead3: "#f2d5c8",
        dshticket1: "#f7e3d8",
        tickettitle: "black",
        tickettext: "black",
        dshticket2: "#a77d6e"
    },
    aquaBreeze: {
        dshbackground1: "#009ffd",
        dshbackground2: "#2a2a72",
        dshbackground3: "#ffffff",
        dshnav1: "#4d5d7a",
        dshhead1: "#212b40",
        dshhead2: "#2678ff",
        dshhead3: "#d0d8e8",
        dshticket1: "#e8f1ff",
        dshticket2: "#7e8cab",
        tickettitle: "black",
        tickettext: "black",
    },
    emeraldMist: { 
        dshbackground1: "#0bab64",
        dshbackground2: "#3bb78f",
        dshbackground3: "#f6fffa",
        dshnav1: "#3b5c52",
        dshhead1: "#233c34",
        dshhead2: "#0abf74",
        dshhead3: "#cfe8dd",
        dshticket1: "#e4f7ef",
        dshticket2: "#84a797",
        tickettitle: "black",
        tickettext: "black",
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
        dshticket2: "#9d9ccc",
        tickettitle: "black",
        tickettext: "black",
    },
    cyberNight2: {
        dshbackground1: "#1f2b3a",   // gradient start (soft navy)
        dshbackground2: "#354a62",   // gradient end (lighter navy)
        dshbackground3: "#f5f7fa",   // MAIN CONTENT background (light!)
        
        dshnav1: "#4e5f77",          // sidebar text
        dshhead1: "#1c2533",         // main titles
        dshhead2: "#3ca3ff",         // button hover + highlight
        dshhead3: "#c7d2e5",         // input borders
        
        dshticket1: "#e3e9f2",       // ticket border lines
        dshticket2: "#6e7f99",        // ticket small text
        tickettitle: "black",
        tickettext: "black",
    },
}

function applyTheme(name) {
  const theme = themes[name];
  for (let key in theme) {
    document.documentElement.style.setProperty(`--${key}`, theme[key]);
  }
}
applyTheme ("cyberNight2");