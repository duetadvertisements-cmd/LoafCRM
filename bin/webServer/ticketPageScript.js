const BASE_URL = "http://192.168.1.33:8080";

/* ===============================
   HELPERS
   =============================== */

function getCurrentTicket() {
    const raw = sessionStorage.getItem("currentTicket");

    if (!raw) {
        console.error("No currentTicket found in sessionStorage");
        return null;
    }

    try {
        const parsed = JSON.parse(raw);

        // Supports BOTH shapes:
        // {ticket:{...}} OR {...}
        return parsed.ticket ? parsed.ticket : parsed;

    } catch (error) {
        console.error("Invalid currentTicket JSON", error);
        return null;
    }
}

function escapeHTML(str) {
    if (str == null) return "";
    return String(str)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

/* ===============================
   MAIN TICKET DISPLAY
   =============================== */

function setMainTicket(ticket = null) {

    if (!ticket) {
        ticket = getCurrentTicket();
    }

    if (!ticket) return;

    console.log("Loaded Ticket:", ticket);

    const statusNum = Number(ticket.status);

    let ticketStatus = "Unknown";
    console.log (statusNum);
    if (statusNum === 1) {
        ticketStatus = "Open"; 
        document.getElementById("status-selector").selectedIndex = 0;
    }   
    if (statusNum === 2) {
        ticketStatus = "Closed"; 
        document.getElementById("status-selector").selectedIndex = 1;
    } 
    if (statusNum === 3) {
        ticketStatus = "Parking"; 
        document.getElementById("status-selector").selectedIndex = 2;   
    }   
    if (statusNum === 4) {
        ticketStatus = "In Progress";
        document.getElementById("status-selector").selectedIndex = 3;
    }

    let madeByDisplay = ticket.madeBy || "";

    if (
        String(ticket.madeBy).trim().toLowerCase() !==
        String(ticket.creator).trim().toLowerCase()
    ) {
        madeByDisplay = "Employee";
    }

    document.getElementById("mainTicket").innerHTML = `
        <div class="ticket-header">
            <h1 class="ticket-title">${escapeHTML(ticket.title)}</h1>
            <span class="ticket-id">#${ticket.id}</span>
        </div>

        <div class="ticket-content">
            <h3>Ticket Details</h3>
            <p>${escapeHTML(ticket.data)}</p>
        </div>

        <hr>

        <div class="ticket-meta">
            <div class="meta-item">
                <strong>Created by:</strong> ${escapeHTML(ticket.creator)} <br>
                <strong>Made by:</strong> ${escapeHTML(madeByDisplay)} <br>
                <strong>Customer:</strong> ${escapeHTML(ticket.workingForCustomer)}
            </div>

            <div class="meta-item">
                <strong>Ticket ID:</strong> #${ticket.id} <br>
                <strong id="statusDisplay">Ticket Status:</strong> ${ticketStatus}
            </div>
        </div>
    `;
}
function setMainTicketAsSubTicket (ticket = null) {
    
    if (!ticket) {
        ticket = JSON.parse (sessionStorage.getItem ("currentSubTicket"));
    }

    if (!ticket) return;

    console.log("Loaded Ticket:", ticket);

    const statusNum = Number(ticket.status);

    let ticketStatus = "Unknown";
    console.log (statusNum);
    if (statusNum === 1) {
        ticketStatus = "Open"; 
        document.getElementById("status-selector").selectedIndex = 0;
    }   
    if (statusNum === 2) {
        ticketStatus = "Closed"; 
        document.getElementById("status-selector").selectedIndex = 1;
    } 
    if (statusNum === 3) {
        ticketStatus = "Parking"; 
        document.getElementById("status-selector").selectedIndex = 2;   
    }   
    if (statusNum === 4) {
        ticketStatus = "In Progress";
        document.getElementById("status-selector").selectedIndex = 3;
    }

    let madeByDisplay = ticket.madeBy || "";

    if (
        String(ticket.madeBy).trim().toLowerCase() !==
        String(ticket.creator).trim().toLowerCase()
    ) {
        madeByDisplay = "Employee";
    }

    document.getElementById("mainTicket").innerHTML = `
        <div class="ticket-header">
            <h1 class="ticket-title">${escapeHTML(ticket.title)}</h1>
            <span class="ticket-id">#${ticket.id}</span>
        </div>

        <div class="ticket-content">
            <h3>Ticket Details</h3>
            <p>${escapeHTML(ticket.data)}</p>
        </div>

        <hr>

        <div class="ticket-meta">
            <div class="meta-item">
                <strong>Created by:</strong> ${escapeHTML(ticket.creator)} <br>
                <strong>Made by:</strong> ${escapeHTML(madeByDisplay)} <br>
                <strong>Customer:</strong> ${escapeHTML(ticket.workingForCustomer)}
            </div>

            <div class="meta-item">
                <strong>Ticket ID:</strong> #${ticket.id} <br>
                <strong id="statusDisplay">Ticket Status:</strong> ${ticketStatus}
            </div>
        </div>
    `;
}
if (sessionStorage.getItem ("inSubTicket") === "false" || !sessionStorage.getItem ("inSubTicket")) {
    setMainTicket();
}
else {
    setMainTicketAsSubTicket ();
}


/* ===============================
   BACK BUTTON
   =============================== */

function goBack() {
    const url = sessionStorage.getItem("dashboardHref");

    if (url) {
        window.location.href = url;
    } else {
        history.back();
    }
}

/* ===============================
   COMMENT MODAL
   =============================== */

function addComment() {
    document.getElementById("commentModal").classList.remove("hidden");
}

function closeModal() {
    document.getElementById("commentModal").classList.add("hidden");
}

/* ===============================
   SUBMIT COMMENT
   =============================== */

async function submitComment() {
    if (sessionStorage.getItem ("inSubTicket") === "true") {
        submitSubTicketComment ();
        return;
    }
    const title = document.getElementById("commentTitle").value.trim();
    const text = document.getElementById("commentText").value.trim();
    const file = document.getElementById("commentFile").files[0];
    let employeeOnlyToggle = getEmployeeOnlyCommentToggleValue (); 
    let subTicketToggle = getSubTicketToggleValue (); 

    console.log (employeeOnlyToggle);
    console.log (subTicketToggle);
    if (subTicketToggle === "on") {
       if (!title || !text) {
            alert("Please fill all fields.");
            return;
        } 
        const ticket = getCurrentTicket();
        if (!ticket) return;

        const body = new URLSearchParams();

        body.append("title", title);
        body.append("data", text);
        body.append("creator", sessionStorage.getItem("username"));
        body.append("madeBy", sessionStorage.getItem("username"));
        body.append("parentTicket", ticket.id);
        body.append("toEmail", sessionStorage.getItem("email"));

        try {
            const response = await fetch(`${BASE_URL}/addSubTicket`, {
                method: "POST",
                body: body.toString ()
            });

            const result = await response.text();

            if (result === "SUCCESS") {
                closeModal();
                location.reload();
            } else {
                alert("Failed: " + result);
            }

        } catch (error) {
            console.error(error);
            alert("Server Error");
        }

    }
    else {
        if (!title || !text) {
            alert("Please fill all fields.");
            return;
        }
        if (employeeOnlyToggle === "on") {employeeOnlyToggle = 1}
        else {employeeOnlyToggle = 2}
        const ticket = getCurrentTicket();

        if (!ticket) return;

        const formData = new FormData();

        formData.append("title", title);
        formData.append("data", text);
        formData.append("creator", sessionStorage.getItem("username"));
        formData.append("ticket", ticket.id);
        formData.append("toEmail", sessionStorage.getItem("email"));
        formData.append("isEmployeeOnly", employeeOnlyToggle);

        if (file) {
            formData.append("file", file);
        }

        try {
            const response = await fetch(`${BASE_URL}/addComment`, {
                method: "POST",
                body: formData
            });

            const result = await response.text();

            if (result === "SUCCESS") {
                closeModal();
                location.reload();
            } else {
                alert("Failed: " + result);
            }

        } catch (error) {
            console.error(error);
            alert("Server Error");
        }
    }
    
}

/* ===============================
   GET COMMENTS
   =============================== */

async function getComments() {

    const ticket = getCurrentTicket();

    if (!ticket) return;

    const body = `ticketID=${encodeURIComponent(ticket.id)}`;

    try {

        const response = await fetch(`${BASE_URL}/getComments`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body
        });

        const text = await response.text();

        console.log("Comments Raw:", text);

        const comments = JSON.parse(text);

        displayComments(comments);

    } catch (error) {
        console.error("Failed loading comments:", error);
    }
}
if (sessionStorage.getItem ("inSubTicket") === "false" || !sessionStorage.getItem ("inSubTicket")) {
    getComments();
}
else {
    getSubTicketComments ();
}


/* ===============================
   DISPLAY COMMENTS
   =============================== */

function displayComments(commentsArray) {

    const container = document.getElementById("commentsContainer");

    container.innerHTML = "";

    commentsArray.forEach(c => {

        const div = document.createElement("div");
        div.classList.add("commentBox");

        let attachmentHTML = "";

        if (c.filepath && c.filepath !== "null") {

            const fullUrl = `${BASE_URL}/${c.filepath}`;
            const fileName = c.filepath.split("/").pop();
            const extension = fileName.split(".").pop().toLowerCase();

            const imageTypes = ["jpg", "jpeg", "png", "gif", "webp"];

            if (imageTypes.includes(extension)) {

                attachmentHTML = `
                    <div class="file-preview">
                        <img src="${fullUrl}" style="max-width:200px;border-radius:8px;border:1px solid #ddd;">
                        <br>
                        <a href="${fullUrl}" target="_blank">View Full Image</a>
                    </div>
                `;

            } else {

                let icon = "📁";

                if (extension === "pdf") icon = "📕";
                if (extension === "txt") icon = "📄";
                if (extension === "jar") icon = "☕";
                if (extension === "zip" || extension === "rar") icon = "📦";

                attachmentHTML = `
                    <div class="file-card" style="display:flex;gap:10px;align-items:center;padding:12px;background:#f8f9fa;border:1px solid #e0e0e0;border-radius:8px;margin-top:10px;">
                        <span style="font-size:24px;">${icon}</span>

                        <div style="flex-grow:1;">
                            <div style="font-weight:bold;font-size:14px;word-break:break-all;">
                                ${escapeHTML(fileName)}
                            </div>

                            <small style="color:#666;">
                                ${extension.toUpperCase()} File
                            </small>
                        </div>

                        <a href="${fullUrl}" download="${escapeHTML(fileName)}"
                           style="padding:5px 15px;background:#007bff;color:white;border-radius:5px;text-decoration:none;font-size:13px;">
                           Download
                        </a>
                    </div>
                `;
            }
        }

        div.innerHTML = `
            <h4>${escapeHTML(c.commentTitle)}</h4>
            <p>${escapeHTML(c.comment)}</p>

            ${attachmentHTML}

            <small style="display:block;margin-top:15px;color:#888;">
                By ${escapeHTML(c.username)} • ${escapeHTML(c.madeOn)}
            </small>
        `;

        if (c.isEmployeeOnly == 1 && sessionStorage.getItem ("role") != "CLIENT") {
            div.classList.add("employee-only-comment");
            container.appendChild(div);
        }
        if (c.isEmployeeOnly == 2) {
            container.append (div);
        }
        
    });
}

/* ===============================
   THEMES
   =============================== */

const themes = {
    basic: {
        "bg1": "#667eea",
        "bg2": "#49BC9B",
        "card-bg": "#ffffff",
        "card-border": "#49BC9B",
        "card-top-strip": "#667eea",
        "ticket-title": "#333",
        "ticket-text": "#444"
    },

    royalLuxe: {
        "bg1": "#4e54c8",
        "bg2": "#8f94fb"
    },

    desert_slate: {
        "bg1": "#2d2926",
        "bg2": "#403a33"
    },

    cobalt_horizon: {
        "bg1": "#1e293b",
        "bg2": "#333155"
    },

    phantom_forest: {
        "bg1": "#0f1d1a",
        "bg2": "#1c3a33"
    }
};

function applyTheme(name) {

    const theme = themes[name];

    if (!theme) return;

    Object.keys(theme).forEach(key => {
        document.documentElement.style.setProperty(`--${key}`, theme[key]);
    });
}
document.addEventListener("DOMContentLoaded", () => {
    
    const savedTheme = localStorage.getItem("preferredTheme") || "basic";
    applyTheme(savedTheme);
});

/* ===============================
   STATUS BUTTON
   =============================== */


/* ===============================
   CHANGE STATUS
   =============================== */

async function changeStatus(status) {

    const ticket = getCurrentTicket();

    if (!ticket) return;
    
    let toStatus;
    switch (status) {
        case 'OPEN': toStatus = 1;  break;
        case 'CLOSED': toStatus = 2;  break;
        case 'PARKING': toStatus = 3;  break;
        case 'IN_PROGRESS': toStatus = 4;  break;
    }

    console.log (toStatus);

    const body =
        `ticketID=${encodeURIComponent(ticket.id)}&toStatus=${encodeURIComponent(toStatus)}`;

    try {

        await fetch(`${BASE_URL}/changeStatus`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body
        });

        goBack();

    } catch (error) {
        console.error(error);
    }
}

/* ===============================
   CLOCK SYSTEM
   =============================== */

function initClockStorage() {

    const val = localStorage.getItem("clockedIntoATicket");

    if (val !== "true") {
        localStorage.setItem("clockedIntoATicket", "");
    }
}

initClockStorage();

async function clockInTicket() {

    if (localStorage.getItem("clockedIntoATicket") === "true") {
        return;
    }

    const ticket = getCurrentTicket();

    if (!ticket) return;

    const body =
        `userID=${encodeURIComponent(sessionStorage.getItem("userID"))}` +
        `&ticketId=${encodeURIComponent(ticket.id)}`;

    try {

        const response = await fetch(`${BASE_URL}/clockInTicket`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body
        });

        console.log(await response.text());

        localStorage.setItem("clockedIntoATicket", "true");

    } catch (error) {
        console.error(error);
    }
}

async function clockOutTicket() {

    if (localStorage.getItem("clockedIntoATicket") !== "true") {
        return;
    }

    const body =
        `userID=${encodeURIComponent(sessionStorage.getItem("userID"))}`;

    try {

        const response = await fetch(`${BASE_URL}/clockOutTicket`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body
        });

        console.log(await response.text());

        localStorage.setItem("clockedIntoATicket", "");

    } catch (error) {
        console.error(error);
    }
}

if (sessionStorage.getItem("role") === "CLIENT") {

    document.querySelectorAll(".clockButton")
        .forEach(button => {
            button.style.display = "none";
        });
    document.querySelectorAll("#employeeOnlyCommentToggle")
        .forEach(checkbox => {
            checkbox.style.display = "none";
        });
    document.querySelectorAll("#employeeOnlyLabel")
    .forEach(label => {
        label.style.display = "none";
    });

}

function getEmployeeOnlyCommentToggleValue () {
    const employeeOnlyCommentToggle = document.getElementById('employeeOnlyCommentToggle');

    const value = employeeOnlyCommentToggle.checked ? "on" : "off";

    console.log("employeeOnlyCommentToggle Value on Button Click:", value);

    return value;
}

function getSubTicketToggleValue () {
    const subTicketToggle = document.getElementById('subTicketToggle');

    const value = subTicketToggle.checked ? "on" : "off";

    console.log("subTicketToggle Value on Button Click:", value);

    return value;
}
async function getSubTickets () {
    const ticket = getCurrentTicket();

    if (!ticket) return;

    const body = `parentTicketID=${encodeURIComponent(ticket.id)}`;

    try {

        const response = await fetch(`${BASE_URL}/getSubTickets`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body
        });

        const text = await response.text();

        console.log("Sub Tickets Raw:", text);

        const subTickets = JSON.parse(text);

        displaySubTickets (subTickets);

    } catch (error) {
        console.error("Failed loading sub Tickets:"+ error);
    }
}
function displaySubTickets(subTickets) {
    const container = document.getElementById("subTicketsContainer");

    container.innerHTML = "";

    subTickets.forEach(sub => {

        let statusColor = "#6c757d";
        if (sub.status == 1) statusColor = "#28a745";
        if (sub.status == 2) statusColor = "#dc3545";
        if (sub.status == 3) statusColor = "#ffc107";
        if (sub.status == 4) statusColor = "#17a2b8";

        const div = document.createElement("div");
        div.classList.add("subticket-card");

        div.innerHTML = `
            <div style="display:flex;gap:12px;align-items:flex-start;
                        padding:14px;background:#f8f9fa;
                        border:1px solid #e0e0e0;border-radius:8px;
                        margin-top:10px;cursor:pointer;">

                <span style="font-size:22px;">🧩</span>

                <div style="flex-grow:1;">
                    <div style="font-weight:bold;font-size:15px;">
                        ${escapeHTML(sub.title)}
                    </div>

                    <div style="font-size:13px;color:#555;margin-top:4px;">
                        ${escapeHTML(sub.data)}
                    </div>

                    <small style="color:#777;display:block;margin-top:8px;">
                        Created by ${escapeHTML(sub.creator)}
                    </small>

                    <small style="color:#999;">
                        Customer: ${escapeHTML(sub.workingForCustomer)}
                    </small>
                </div>

                <div style="
                    padding:4px 10px;
                    border-radius:6px;
                    font-size:12px;
                    background:${statusColor};
                    color:white;
                    height:fit-content;
                ">
                </div>
            </div>
        `;

        div.onclick = () => openSubTicket(sub);

        container.appendChild(div);
    });
}
getSubTickets ();

const checkbox = document.getElementById("subTicketToggle");
const fileInput = document.getElementById("commentFile");

checkbox.addEventListener("change", () => {
    if (checkbox.checked) {
        fileInput.value = "";        // clear selected file
        fileInput.disabled = true;   // block file upload
    } else {
        fileInput.disabled = false;  // allow file upload
    }
});

function openSubTicket(sub) {
    sessionStorage.setItem ("currentSubTicket", JSON.stringify (sub));
    sessionStorage.setItem ("inSubTicket", "true");
    window.location.href = "ticketPage.html"; // or your page
}

async function getSubTicketComments () {
    const ticket = JSON.parse (sessionStorage.getItem ("currentSubTicket"));

    if (!ticket) return;

    const body = `ticketID=${encodeURIComponent(ticket.id)}`;

    try {

        const response = await fetch(`${BASE_URL}/getSubTicketComments`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body
        });

        const text = await response.text();

        console.log("Comments Raw:", text);

        const comments = JSON.parse(text);

        displayComments(comments);

    } catch (error) {
        console.error("Failed loading comments:", error);
    }
}

async function submitSubTicketComment () {
    const title = document.getElementById("commentTitle").value.trim();
    const text = document.getElementById("commentText").value.trim();
    const file = document.getElementById("commentFile").files[0];
    let employeeOnlyToggle = getEmployeeOnlyCommentToggleValue (); 
    let subTicketToggle = getSubTicketToggleValue (); 

    console.log (employeeOnlyToggle);
    console.log (subTicketToggle);
        if (!title || !text) {
            alert("Please fill all fields.");
            return;
        }
        if (employeeOnlyToggle === "on") {employeeOnlyToggle = 1}
        else {employeeOnlyToggle = 2}
        const ticket = JSON.parse (sessionStorage.getItem ("currentSubTicket"));

        if (!ticket) return;

        const formData = new FormData();

        formData.append("title", title);
        formData.append("data", text);
        formData.append("creator", sessionStorage.getItem("username"));
        formData.append("ticket", ticket.id);
        formData.append("toEmail", sessionStorage.getItem("email"));
        formData.append("isEmployeeOnly", employeeOnlyToggle);

        if (file) {
            formData.append("file", file);
        }

        try {
            const response = await fetch(`${BASE_URL}/addSubTicketComment`, {
                method: "POST",
                body: formData
            });

            const result = await response.text();

            if (result === "SUCCESS") {
                closeModal();
                location.reload();
            } else {
                alert("Failed: " + result);
            }

        } catch (error) {
            console.error(error);
            alert("Server Error");
        }
}

document.addEventListener("DOMContentLoaded", () => {
    if (sessionStorage.getItem("inSubTicket") === "true") {
        getSubTicketComments();
        setMainTicketAsSubTicket();
    } else {
        getComments();
        setMainTicket();
    }
});

if (sessionStorage.getItem ("role") === "CLIENT") {
    Document.getElementById ("subTicketToggle").style.display = "none";
    Document.getElementById ("subTicketLabel").style.display = "none";
}