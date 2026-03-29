function setWelcomeTitle () {
    document.getElementById ("welcomeTitle").innerText = "Welcome, " + sessionStorage.getItem ("username");
}
setWelcomeTitle ();

// Navigation functions
function showSection(sectionName) {
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

    // Add active class to clicked nav item
    event.target.closest('.nav-item').classList.add('active');

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
    alert("Button pressed");
    console.log("Ticket clicked:", ticket);
}

// Close mobile menu when clicking outside or on a nav item
document.addEventListener('click', function(event) {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.querySelector('.sidebar-overlay');
    const mobileToggle = document.querySelector('.mobile-menu-toggle');

    if (!sidebar.contains(event.target) &&
        !mobileToggle.contains(event.target) &&
        sidebar.classList.contains('open')) {
        closeMobileMenu();
    }
});

async function addTicket () {
    const title = document.getElementById ("titleInput").value;
    const data = document.getElementById ("dataInput").value;
    const creator = sessionStorage.getItem ("username");

    const ticket = `title=${encodeURIComponent(title)}&data=${encodeURIComponent(data)}&creator=${encodeURIComponent(creator)}`;    

    try {
        const serverResponse = await fetch("http://localhost:8080/addTicket", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: ticket
        });

        const responseText = await serverResponse.text();

        console.log("Server response:");
        console.log(responseText);

        if (responseText == "SUCCESS") {
            alert ("Success");
        }

        else {
            alert ("Failed");
        }

    } catch (error) {
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
    }
}

async function getTickets () {  

    const username = sessionStorage.getItem ("username");
    const ticket = `username=${encodeURIComponent(username)}`;
    try {
        const serverResponse = await fetch("http://localhost:8080/getTickets", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: ticket
        });
        const responseText = await serverResponse.text();

        console.log("Server response:");
        console.log(responseText);

        console.log ("Parsing JSON...");
        const tickets = JSON.parse (responseText);
        console.log ("Parsed JSON:");
        console.log (tickets);

        displayTickets(tickets);
    }
    catch (error) {
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
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