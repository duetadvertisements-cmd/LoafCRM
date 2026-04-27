

if (sessionStorage.getItem ("tickets")) {
    displayTickets (JSON.parse (sessionStorage.getItem ("tickets")) , "ticketContainer", "ALL");
}
else {
    getTickets ();
}

function setWelcomeTitle () {
    document.getElementById ("welcomeTitle").innerText = "Welcome, " + sessionStorage.getItem ("username");
}

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
}

function toggleMobileMenu() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.querySelector('.sidebar-overlay');

    sidebar.classList.toggle('open');
    overlay.classList.toggle('active');
}

// Close mobile menu when clicking outside or on a nav item
document.addEventListener('click', function(event) {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.querySelector('.sidebar-overlay');
    const mobileToggle = document.querySelector('.mobile-menu-toggle');

    if (!sidebar.contains(event.target) &&
        !mobileToggle.contains(event.target) &&
        sidebar.classList.contains('open')) {

        sidebar.classList.remove('open');
        overlay.classList.remove('active');
    }
});

async function addTicket () {
    const title = document.getElementById ("titleInput").value;
    const data = document.getElementById ("dataInput").value;
    const creator = sessionStorage.getItem ("username");

    const ticket = `title=${encodeURIComponent(title)}
                    &data=${encodeURIComponent(data)}
                    &creator=${encodeURIComponent(creator)}
                    &toEmail=${encodeURIComponent (sessionStorage.getItem ("email"))}
                    &madeBy=${encodeURIComponent (creator)}`;    

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


        await getTickets();
        await getUnassignedTickets();
    }
    catch (error) {
        console.log ("---------------------error to addtickets ()---------------------------------");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
        console.log ("------------------------------------------------------");
    }
}

async function getTickets () {  

    const ticket = `username=${encodeURIComponent("4k60cHb2E4Li6nf3w4K4ly3UvM4BKeUe")}`;
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

        displayTickets(tickets, "ticketContainer", "ALL");
        sessionStorage.setItem ("tickets", JSON.stringify(tickets));
    }
    catch (error) {
        console.error ("----------------------error to get tickets--------------------------------");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
        console.error ("------------------------------------------------------");
    }   
}

function displayTickets(tickets, containerID, filter) {
    const container = document.getElementById(containerID);
    
    container.innerHTML = "";  // Clear previous tickets

    if (tickets.length === 0) {
        container.innerHTML = "<p class='no-tickets'>No tickets found</p>";
        return;
    }
    if (filter === "ALL") {
        tickets.forEach(ticket => {
            renderTicket (ticket, container);
        });
    }

    if (filter === "OPEN") {
        tickets.forEach(ticket => {
            if (ticket.status === 1) {
                renderTicket (ticket,  container);
            }
        });
    }

    if (filter === "CLOSED") {
        tickets.forEach(ticket => {
            if (ticket.status === 2) {
                renderTicket (ticket, container);
            }
        });
    }
}

function renderTicket (ticket, container) {
    let ticketStatus;
    if (ticket.status === 1) {ticketStatus = "Open"}
    else {ticketStatus = "Closed"}

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
        <p>Status: ${ticketStatus}</p>
    `;
    container.appendChild(ticketDiv);
}

async function getUnassignedTickets () {  

    const ticket = `key=${encodeURIComponent("4k60cHb2E4Li6nf3w4K4ly3UvM4BKeUe")}`;
    try {
        const serverResponse = await fetch("http://192.168.1.34:8080/getUnassignedTickets", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: ticket
        });
        const responseText = await serverResponse.text();

        console.log ("------------------------------------------------------");
        console.log("Server response to getUnassignedTickets ():");
        console.log(responseText);
        console.log ("------------------------------------------------------");

        const tickets = JSON.parse (responseText);
        console.log ("-----------------------getUnassignedTickets ()-------------------------------");
        console.log (tickets);
        console.log ("------------------------------------------------------");

        displayUnassignedTickets(tickets, "unassignedTicketContainer");
    }
    catch (error) {
        console.error ("----------------------error to get Unassigned Tickets--------------------------------");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
        console.error ("------------------------------------------------------");
    }
}

function displayUnassignedTickets(tickets, containerID) {
        const container = document.getElementById(containerID);
    
    container.innerHTML = "";  // Clear previous tickets

    if (tickets.length === 0) {
        container.innerHTML = "<p class='no-tickets'>No tickets found</p>";
        return;
    }

    tickets.forEach(ticket => {
        const ticketDiv = document.createElement("div");
        ticketDiv.className = "unassignedTicket";
        ticketDiv.innerHTML = `
            <div class="ticket-header">
                <h3 class="ticket-title">${ticket.title}</h3>
                <span class="ticket-id">#${ticket.id}</span>
            </div>
            <p class="ticket-data">${ticket.data}</p>
            <p class="ticket-creator">Created by: ${ticket.creator}</p> <br><br>

            <button onclick = "assignTicket (${ticket.id})" class = "assignTicketButton">Assign</button>
        `;
        container.appendChild(ticketDiv);
    });
}

function ticketClicked(ticket) {
    sessionStorage.setItem ("currentTicket", JSON.stringify ({ticket}));
    sessionStorage.setItem ("dashboardHref", "dashboards/AdminDashboard.html")
    window.location.href = "../ticketPage.html";
}

async function getEmployees () {  

    const employee = `username=${encodeURIComponent("4k60cHb2E4Li6nf3w4K4ly3UvM4BKeUe")}`;
    try {
        const serverResponse = await fetch("http://192.168.1.34:8080/getEmployees", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: employee
        });
        const responseText = await serverResponse.text();

        console.log ("------------------------------------------------------");
        console.log("Server response to getEmployees ():");
        console.log(responseText);
        console.log ("------------------------------------------------------");

        const employees = JSON.parse (responseText);
        console.log ("-----------------------GETEMPLOYEES ()-------------------------------");
        console.log (employees);
        console.log ("------------------------------------------------------");

        displayEmployees (employees);
    }
    catch (error) {
        console.error ("----------------------error to get employees--------------------------------");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
        console.error ("------------------------------------------------------");
    }
}

function displayEmployees (employees) {
    const container = document.getElementById("employeesContainer");
    container.innerHTML = "";

    if (employees.length === 0) {
        container.innerHTML = "<p class='no-employees'>No Employees found</p>";
        return;
    }

    employees.forEach(employee => {
        const employeeDiv = document.createElement("div");
        employeeDiv.className = "employee";
        employeeDiv.onclick = () => employeeClicked(employee);
        employeeDiv.innerHTML = `
            <div class="employee-header">
            <h3 class="employee-title">${employee.username}</h3>
            <span class="employee-id">#${employee.id}</span>
        </div>
        <p class="employee-email">Email: ${employee.email}</p>
        `;  
        container.appendChild(employeeDiv);
    });
}

async function employeeClicked (employee) {
    const hoursAssigned = await getHoursAssigned (employee.id);
    sessionStorage.setItem ("currentEmployee", JSON.stringify ({
        username: employee.username,
        email: employee.email,
        id: employee.id,
        hoursAssigned: hoursAssigned
    }));
    sessionStorage.setItem ("dashboardHref", "dashboards/AdminDashboard.html")
    window.location.href = "../employeePage.html";
}

async function getUsers () {  

    const users = `username=${encodeURIComponent("4k60cHb2E4Li6nf3w4K4ly3UvM4BKeUe")}`;
    try {
        const serverResponse = await fetch("http://192.168.1.34:8080/getUsers", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: users
        });
        const responseText = await serverResponse.text();

        console.log ("------------------------------------------------------");
        console.log("Server response to getUsers ():");
        console.log(responseText);
        console.log ("------------------------------------------------------");

        const usersObtained = JSON.parse (responseText);
        console.log ("-----------------------GETUSERS ()-------------------------------");
        console.log (users);
        console.log ("------------------------------------------------------");

        displayUsers (usersObtained);
    }
    catch (error) {
        console.error ("----------------------error to get users--------------------------------");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
        console.error ("------------------------------------------------------");
    }
}

function displayUsers (users) {
    const container = document.getElementById("usersContainer");
    container.innerHTML = "";

    if (users.length === 0) {
        container.innerHTML = "<p class='no-employees'>No Employees found</p>";
        return;
    }

    users.forEach(user => {
        const userDiv = document.createElement("div");
        userDiv.className = "user";
        userDiv.innerHTML = `
            <div class="user-header">
            <h3 class="user-title">${user.username}</h3>
            <span class="user-id">#${user.id}</span>
        </div>
        <p class="user-email">Email: ${user.email}</p>
        <br>
        <p><button onclick = "promoteUserToWorker (${user.id})" class = 'userPromotionButton'>Promote To Worker</button></p>
        `;  
        container.appendChild(userDiv);
    });
}

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
        dshticket2: "#999",
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
    desert_slate: {
        dshbackground1: "#2d2926", 
        dshbackground2: "#403a34", 
        dshbackground3: "#fffcf9", 
        dshnav1: "#5d544b",        
        dshhead1: "#1a1a1a",        
        dshhead2: "#d97757",        
        dshhead3: "#e8e2db",        
        dshticket1: "#f2ede7",      
        dshticket2: "#a69d95",
        tickettitle: "#1a1a1a"
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
        dshticket2: "#94a3b8",
        tickettitle: "#0f172a"
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
        dshticket2: "#7ca29a",
        tickettitle: "#122621"
    },
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

async function getUserID(username, action) {
    if (!username) return;

    try {
        const response = await fetch("http://192.168.1.34:8080/getUserID", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `username=${encodeURIComponent(username)}`
        });

        if (action === "SET") {
            sessionStorage.setItem("userID", await response.text());
        }
        if (action === "GET") {
            return await response.text ();
        }
        
        console.log("Got User ID");
    } catch (error) {
        console.error(error);
    }
}

getUserID (sessionStorage.getItem ("username"), "SET");

async function promoteUserToWorker (id) {
    const response = await fetch("http://192.168.1.34:8080/promoteUserToWorker", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: `id=${encodeURIComponent(id)}`
    });
    console.log ("Promoted User To Worker")
    console.log (response.text);
    await getUsers ();
}

async function assignTicket (ticketID) {
    const assignTo = prompt ("Assign To:");
    const hoursAssigned = prompt ("Hours Assigned:");

    if (!assignTo) {
        alert("Assignment cancelled");
        return;
    }

    const assignToID = await getUserID(assignTo, "GET");
    const ticket = `ticketID=${encodeURIComponent(ticketID)}&assignTo=${assignToID}&toEmail=${sessionStorage.getItem ("email")}&assignedBy=${sessionStorage.getItem ("userID")}&hoursAssigned=${parseInt (hoursAssigned)}&assignToString=${assignTo}&assignToEmail=${await getEmail (assignTo, "GET")}`;
    
    try {
        const serverResponse = await fetch("http://192.168.1.34:8080/assignTicket", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: ticket
        });
        const responseText = await serverResponse.text();
        console.log (responseText);

        await getTickets();
        await getUnassignedTickets ();
    }
    catch (exception) {
        console.error (exception);
    }
}

async function getEmail (username, action) {  
    const ticket = `username=${encodeURIComponent(username)}`;
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
        if (action === "SET") {
            sessionStorage.setItem ("email", responseText);
        }
        if (action === "GET") {
            return responseText;
        }
        
    }
    catch (error) {
        console.error ("----------------------error to get email--------------------------------");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
        console.error ("------------------------------------------------------");
    }
}


async function getAmountOfTimeEmployeeHasWorked () {
    const currentYear = new Date ().getFullYear (); 
    const currentMonth = String(new Date().getMonth() + 1).padStart(2, '0');


    const userID = document.getElementById ("getTimeWorkedUserIDInput").value;
    if (userID === "") {return;}
    const ticket = `userID=${encodeURIComponent (userID)}&workingYear=${encodeURIComponent (currentYear)}&workingMonth=${encodeURIComponent (currentMonth)}`

    try {
        const serverResponse = await fetch ("http://192.168.1.34:8080/getTimeWorked", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded"},
            body: ticket
        });

        const workedHours = await serverResponse.text ();
        console.log (workedHours);
        document.getElementById ("workingHoursDisplay").innerHTML = workedHours+ " Hours";
    }
    catch (error) {
        console.error (error);
        alert ("An Unexpected Error Occured");
    }
}

async function getAmountOfTimeEmployeeHasWorkedOnATicket () {
    let currentMonth = new Date().getMonth() + 1;
    const currentYear = new Date ().getFullYear (); 

    switch (currentMonth) {
        case 1: currentMonth = "01"
        break;
        case 2: currentMonth = "02"
        break;
        case 3: currentMonth = "03"
        break;
        case 4: currentMonth = "04"
        break;
        case 5: currentMonth = "05"
        break;
        case 6: currentMonth = "06"
        break;
        case 7: currentMonth = "07"
        break;
        case 8: currentMonth = "08"
        break;
        case 9: currentMonth = "09"
        break;
    }

    const ticketID = document.getElementById ("getTimeWorkedOnATicketTicketIDInput").value;
    const userID = document.getElementById ("getTimeWorkedOnATicketUserIDInput").value;
    if (userID === "" || ticketID === "") {return;}
    const ticket = `userID=${encodeURIComponent (userID)}&workingYear=${encodeURIComponent (currentYear)}&workingMonth=${encodeURIComponent (currentMonth)}&ticketID=${encodeURIComponent (ticketID)}`

    try {
        const serverResponse = await fetch ("http://192.168.1.34:8080/getTimeWorkedOnATicket", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded"},
            body: ticket
        });

        const workedHours = await serverResponse.text ();
        console.log (workedHours);
        document.getElementById ("workingHoursOfATicketDisplay").innerHTML = workedHours+ " Hours";
    }
    catch (error) {
        console.error (error);
        alert ("An Unexpected Error Occured");
    }
}

function changeFilter(filter) {
    const stored = sessionStorage.getItem("tickets");
    if (!stored) return;

    const tickets = JSON.parse(stored);
    displayTickets(tickets, "ticketContainer", filter);
}

function logout() {
    sessionStorage.clear();
    window.location.href = '../index.HTML';
}

setWelcomeTitle ();
getEmployees ();
getUsers ();
applyTheme ("desert_slate");
getEmail (sessionStorage.getItem ("username"), "SET");
getUnassignedTickets ();

async function getHoursAssigned (id) {
    const formData = `userID=${encodeURIComponent(id)}`;

    try {
        const response = await fetch("http://192.168.1.34:8080/getExpectedEmployeeHoursRemaining", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        });
        return await response.text ();

    } catch (error) {
        console.error("Error getting hours:", error);
        return null;
    }
}