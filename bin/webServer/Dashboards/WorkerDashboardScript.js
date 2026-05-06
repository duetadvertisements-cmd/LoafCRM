const themes = {
    cobalt_horizon: { bg1: "#1e293b", bg2: "#333155", "main-bg": "#ffffff", "heading-accent": "#3b82f6", "ticket-bg": "#ffffff", "ticket-border": "#3b82f6",dshbackground1: "#1e293b", dshbackground3: "#ffffff" },
    royalLuxe: { bg1: "#4e54c8", bg2: "#8f94fb", "main-bg": "#fafaff", "heading-accent": "#6a6efb", "ticket-bg": "#ecebff", "ticket-border": "#6a6efb", dshbackground1: "#4e54c8", dshbackground3: "#fafaff"},
    phantom_forest: { bg1: "#0f1d1a", bg2: "#1c3a33", "main-bg": "#fcfdfd", "heading-accent": "#10b981", "ticket-bg": "#ffffff", "ticket-border": "#10b981", dshbackground1: "#0f1d1a", dshbackground3: "#fcfdfd"},
    desert_slate: { bg1: "#2d2926", bg2: "#403a33", "main-bg": "#fffcf9", "heading-accent": "#d97757", "ticket-bg": "#ffffff", "ticket-border": "#d97757", dshbackground1: "#2d2926", dshbackground3: "#fffcf9" },
    basic: { bg1: "#667eea", bg2: "#49BC9B", "main-bg": "white", "heading-accent": "#5a67d8", "ticket-bg": "white", "ticket-border": "#49BC9B",dshbackground1: "#667eea", dshbackground3: "white" }
};

/* ================= INIT ================= */
const BASE_URL = "http://192.168.1.33:8080";
let initRan = false;
let isClockingIn = false;
let allowUnloadClockOut = false;

document.addEventListener("DOMContentLoaded", async () => {
    applyTheme(sessionStorage.getItem("preferredTheme") || "cobalt_horizon");

    await initClockSystem(); // controlled init

    if (sessionStorage.getItem ("ticketsLoaded")) {
        displayTickets (JSON.parse (sessionStorage.getItem ("tickets")), "ALL")
    }
    else {
        getTickets ();
        sessionStorage.setItem ("ticketsLoaded", "true")
    }

    getEmail (sessionStorage.getItem ("username"), "SET");
});

/* ================= UI ================= */

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
    toggleMenu();
}

function applyTheme(name) {
    const theme = themes[name];
    if (!theme) return;

    for (let key in theme) {
        document.documentElement.style.setProperty(`--${key}`, theme[key]);
    }

    sessionStorage.setItem("preferredTheme", name);
}

/* ================= TICKETS ================= */

async function getTickets() {
    const username = sessionStorage.getItem("username");
    if (!username) return;

    try {
        const response = await fetch(BASE_URL+"/getTicketsEmployee", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `username=${encodeURIComponent(username)}`
        });

        const tickets = JSON.parse(await response.text());
        displayTickets(tickets, "ALL");
    } catch (e) {
        console.error("Server Error", e);
    }
}

function displayTickets(tickets, filter) {
        const container = document.getElementById("ticketContainer");
        console.log (tickets);
    
    container.innerHTML = "";  // Clear previous tickets
    // console.log (Object.keys(tickets).length);
    if (!tickets || tickets.length === 0) {
        container.innerHTML = "<p class='no-tickets'>No tickets found</p>";
        return;
    }
    if (filter === "ALL") {

        tickets.forEach(ticket => {
            if (ticket.toDoList !== "true") {
                ticket.toDoList = "false";
            }
            renderTicket (ticket, container);
        });
    }

    if (filter === "OPEN") {
        tickets.forEach(ticket => {
            const statusText = Number (ticket.status) === 1 ? "Open" : "Closed";
            if (statusText === "Open") {
                renderTicket (ticket, container);
            }
        });
    }

    if (filter === "CLOSED") {
        tickets.forEach(ticket => {
            const statusText = Number (ticket.status) === 1 ? "Open" : "Closed";
            if (statusText === "Closed") {
                renderTicket (ticket, container);
            }
        });
    }

    if (filter === "TODOLIST") {
        tickets.forEach(ticket => {
            const statusText = Number (ticket.status) === 1 ? "Open" : "Closed";
            if (ticket.toDoList === "true") {
                renderTicket (ticket, container);
            }
        });
    }
    sessionStorage.setItem("tickets", JSON.stringify(tickets));
}

function renderTicket (ticket, container) {
    let statusText;
    switch (Number (ticket.status)) {
        case 1: statusText = "Open"; break;
        case 2: statusText = "Closed"; break;
        case 3: statusText = "Parking"; break;
        case 4: statusText = "In Progress"; break;
    }

    const [year, month, day, time] = ticket.expectedFinishingTime.split("            ").map(Number);

    const ticketDiv = document.createElement("div");
        ticketDiv.className = "ticket";
        ticketDiv.onclick = () => {
            sessionStorage.setItem("currentTicket", JSON.stringify(ticket));
            sessionStorage.setItem("dashboardHref", "dashboards/WorkerDashboard.html");
            window.location.href = "../ticketPage.html";
        };

        ticketDiv.innerHTML = `
            <div class="ticket-header">
                <h3 class="ticket-title">${ticket.title}</h3>
                <input type="date" class="ticket-date" onclick = "event.stopPropagation();" data-ticket = ${encodeURIComponent(JSON.stringify(ticket))}>
                <span class="ticket-id">#${ticket.id}</span>
                
            </div>
            <p class="ticket-data">${ticket.data}</p>
            <p class="ticket-creator">Created by: ${ticket.creator}</p>
            <p>Status: ${statusText}</p><br><br>
        <button onclick = "event.stopPropagation(); addToToDoList (${ticket.id})" class = "addToToDoListButton">Add To ToDo List</button>
        `;
        if (year && month && day) {
            const formattedTime = `${year}-${month.toString().padStart(2, "0")}-${day.toString().padStart(2, "0")}`;
            ticketDiv.querySelector (".ticket-date").value = formattedTime;
        }
        
        container.appendChild(ticketDiv);
        
}

/* ================= SETTINGS ================= */

function loadSettingsData() {
    document.getElementById("settings-username").value = sessionStorage.getItem("username") || "";
    document.getElementById("settings-email").value = sessionStorage.getItem("email") || "";
}

/* ================= AUTH ================= */

function logout() {
    clockOut();
    sessionStorage.clear();
    window.location.href = '../index.HTML';
}

/* ================= CLOCK SYSTEM ================= */

async function initClockSystem() {
    if (initRan) return;
    initRan = true;

    await getUserID();

    if (sessionStorage.getItem("clockedIn") !== "true") {
        await clockIn();
        sessionStorage.setItem("clockedIn", "true");
    }

    // 🔥 IMPORTANT: delay enabling unload (fixes login-triggered clockOut)
    setTimeout(() => {
        allowUnloadClockOut = true;
    }, 2000);
}

async function clockIn() {
    if (isClockingIn) return;
    isClockingIn = true;

    const id = sessionStorage.getItem("userID");
    if (!id) return;

    try {
        const response = await fetch(BASE_URL+"/clockIn", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `userID=${encodeURIComponent(id)}`
        });

        console.log("Clocked In:", await response.text());
    } catch (error) {
        console.error(error);
    }

    isClockingIn = false;
}

async function clockOut() {
    const id = sessionStorage.getItem("userID");
    if (!id) return;

    try {
        const response = await fetch(BASE_URL+"/clockOut", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `userID=${encodeURIComponent(id)}`
        });

        console.log("Clocked Out:", await response.text());

        // ONLY safe place to update state
        sessionStorage.setItem("clockedIn", "false");
    } catch (error) {
        console.error(error);
    }
}

async function getUserID() {
    const username = sessionStorage.getItem("username");
    if (!username) return;

    try {
        const response = await fetch(BASE_URL+"/getUserID", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `username=${encodeURIComponent(username)}`
        });

        sessionStorage.setItem("userID", await response.text());
        console.log("Got User ID");
    } catch (error) {
        console.error(error);
    }
}

/* ================= PAGE EXIT ================= */

let isReload = false;

const navEntry = performance.getEntriesByType("navigation")[0];
if (navEntry && navEntry.type === "reload") {
    isReload = true;
}

window.addEventListener("beforeunload", () => {
    if (!allowUnloadClockOut) return;
    if (isReload) return; // 🚨 IGNORE reloads

    if (sessionStorage.getItem("clockedIn") === "true") {
        const id = sessionStorage.getItem("userID");
        if (!id) return;

        const data = new URLSearchParams();
        data.append("userID", id);

        navigator.sendBeacon(
            BASE_URL+"/clockOut",
            data.toString()
        );
    }
});

async function addTicket () {
    const title = document.getElementById ("titleInput").value;
    const data = document.getElementById ("dataInput").value;
    const madeBy = sessionStorage.getItem ("username");
    const creator = document.getElementById ("madeByInput").value;

    console.log ("title: "+title);
    console.log ("data: "+data);
    console.log ("madeBy: "+madeBy);
    console.log ("creatir: "+creator);

    const ticket = `title=${encodeURIComponent(title)}
                    &data=${encodeURIComponent(data)}
                    &creator=${encodeURIComponent(creator)}
                    &to =${encodeURIComponent(sessionStorage.getItem ("email"))}
                    &madeBy=${encodeURIComponent (madeBy)}`;    

    try {
        const serverResponse = await fetch(BASE_URL+"/addTicket", {
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


function changeFilter(filter) {
    const stored = sessionStorage.getItem("tickets");
    if (!stored) return;

    const tickets = JSON.parse(stored);
    displayTickets(tickets, filter);
}

function addToToDoList (ticketID) {
    const tickets = JSON.parse(sessionStorage.getItem("tickets"));
    const ticket = tickets.find(t => t.id === ticketID);
    if (!ticket) return;

    ticket.toDoList = "true";
    console.log ("Sucessfully added To ToDo List")
    sessionStorage.setItem("tickets", JSON.stringify(tickets));

    displayTickets(tickets, "ALL");
}

const expectedTimeDateInput = document.querySelector(".ticket-date");

document.addEventListener("change", function (e) {
    if (e.target.classList.contains("ticket-date")) {
        const ticket = JSON.parse (decodeURIComponent (e.target.dataset.ticket));
        setExpectedFinishingTime (e.target.value, ticket);
    }
});

document.addEventListener("change", function (e) {
    if (e.target.classList.contains("filter-expected-time-date")) {
        filterByDateFromUI (e.target.value);
    }
});

async function setExpectedFinishingTime (date, ticket) {
    const expectedFinishingTime = new Date (date);
    expectedFinishingYear = expectedFinishingTime.getFullYear ();
    expectedFinishingMonth = expectedFinishingTime.getMonth ()+1;
    expectedFinishingDate = expectedFinishingTime.getDate ();

    console.log ("expectedFinishingYear: "+expectedFinishingYear);
    console.log ("expectedFinishingMonth: "+expectedFinishingMonth);
    console.log ("expectedFinishingDate: "+expectedFinishingDate);
    console.log ("ticket: ", ticket);

    const toEmail = await getEmail (ticket.creator, "GET");
    console.log ("Email: ", toEmail);


    try {
        const serverResponse = await fetch(BASE_URL+"/addExpectedFinishingTime", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `expectedFinishingTimeYear=${encodeURIComponent(expectedFinishingYear)}
            &expectedFinishingTimeMonth=${encodeURIComponent (expectedFinishingMonth)}
            &expectedFinishingTimeDate=${encodeURIComponent (expectedFinishingDate)}
            &ticketID=${encodeURIComponent (ticket.id)}&toEmail=${encodeURIComponent (toEmail)}`
        });
        console.log ("serverResponse: ",serverResponse);
        console.log (await serverResponse.text);
    } catch (error) {
        console.error(error);
    }
}

async function getEmail (username, action) {  
    const ticket = `username=${encodeURIComponent(username)}`;
    try {
        const serverResponse = await fetch("http://192.168.1.33:8080/getEmail", {
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

async function filterByDateFromUI(selectedDate) {
    const tickets = await document.querySelectorAll(".ticket");

    tickets.forEach(ticketDiv => {
        const expectedTimeDateInput = ticketDiv.querySelector(".ticket-date");

        if (!expectedTimeDateInput) return;

        if (expectedTimeDateInput.value === selectedDate) {
            ticketDiv.style.display = "block"; // show
        } else {
            ticketDiv.style.display = "none"; // hide
        }
    });
}

function clearDateFilter() {
    const tickets = document.querySelectorAll(".ticket");

    tickets.forEach(ticketDiv => {
        ticketDiv.style.display = ""; // reset to default
    });

    // also clear the input
    const input = document.getElementById("filterDate");
    if (input) input.value = "";
}

sessionStorage.setItem ("inSubTicket", false);