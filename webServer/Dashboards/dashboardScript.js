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
        id: ticket.id
    }));
    sessionStorage.setItem ("dashboardHref", "webSerer/dashboard.html")
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
        const response = await fetch("http://192.168.1.34:8080/getTickets", {
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