function setMainTicket(ticket) {
    if (ticket != "null") {
        ticket = JSON.parse(sessionStorage.getItem("currentTicket"));
    }
    
    if (ticket.status == "1") {
        ticket.status = "Open";
    }
    else {
        ticket.status = "Closed"
    }
    document.getElementById("mainTicket").innerHTML = `
        <div class="ticket-header">
            <h1 class="ticket-title">${ticket.title}</h1>
            <span class="ticket-id">#${ticket.id}</span>
        </div>

        <div class="ticket-content">
            <h3>Ticket Details</h3>
            <p>${ticket.data}</p>
        </div>

        <hr>

        <div class="ticket-meta">
            <div class="meta-item">
                <strong>Created by:</strong> ${ticket.creator}
            </div>
            <div class="meta-item">
                <strong>Ticket ID:</strong> #${ticket.id} <br>
                <strong id = "statusDisplay">Ticket Status:</strong> ${ticket.status}
            </div>
        </div>
    `;
    if (ticket.status == "Open") {
        ticket.status = 1;
    }
    else {
        ticket.status = 2;
    }
}

async function setSubTicket (ticket) {
    
    if (ticket.status == "1") {
        ticket.status = "Open";
    }
    else {
        ticket.status = "Closed"
    }
    document.getElementById("mainTicket").innerHTML = `
        <div class="ticket-header">
            <h1 class="ticket-title">${ticket.commentTitle}</h1>
        </div>

        <div class="ticket-content">
            <h3>Ticket Details</h3>
            <p>${ticket.comment}</p>
        </div>

        <hr>

        <div class="ticket-meta">
            <div class="meta-item">
                <strong>Created by:</strong> ${ticket.username}
            </div>
            <div class="meta-item">
                <strong>Made On:</strong> #${ticket.madeOn} <br>
            </div>
        </div>
    `;
    if (ticket.status == "Open") {
        ticket.status = 1;
    }
    else {
        ticket.status = 2;
    }

    document.getElementById ("commentsContainer").innerHTML = ``;

    ticket = `key=${encodeURIComponent("4k60cHb2E4Li6nf3w4K4ly3UvM4BKeUe")}&madeOn=${encodeURIComponent(ticket.madeOn)}&comment=${encodeURIComponent(ticket.comment)}&username=${encodeURIComponent(ticket.username)}&commentTitle=${encodeURIComponent(ticket.commentTitle)}`;
    try {
        const serverResponse = await fetch("http://192.168.1.35:8080/getSubTicketComments", {
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

        displayTickets(tickets, "ticketContainer");
    }
    catch (error) {
        console.error ("----------------------error to get tickets--------------------------------");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
        console.error ("------------------------------------------------------");
    }
}

function goBack() {
    const goBackLocation = sessionStorage.getItem ("dashboardHref");
    window.location.href = goBackLocation;
}

setMainTicket();

function addComment() {
    document.getElementById("commentModal").classList.remove("hidden");
}

function closeModal() {
    document.getElementById("commentModal").classList.add("hidden");
}

async function submitComment() {
    const title = document.getElementById("commentTitle").value;
    const text = document.getElementById("commentText").value;
    const file = document.getElementById("commentFile").files[0];

    const creator = sessionStorage.getItem("username");
    const ticket = JSON.parse(sessionStorage.getItem("currentTicket")).id;

    const formData = new FormData();
    formData.append("title", title);
    formData.append("data", text);
    formData.append("creator", creator);
    formData.append("ticket", ticket);
    if (file) formData.append("file", file);
    console.log (file);

    try {
        const response = await fetch("http://192.168.1.35:8080/addComment", {
            method: "POST",
            body: formData
        });

        const result = await response.text();
        if (result === "SUCCESS") {
            closeModal();
            history.go();
        } else {
            alert("Failed: " + result);
        }
    } catch (err) {
        console.error(err);
        alert("Server error");
    }
}

async function getComments () {
    const ticketID = JSON.parse(sessionStorage.getItem("currentTicket")).id;
    const ticket = `ticketID=${encodeURIComponent(ticketID)}`;

    try {
        const serverResponse = await fetch("http://192.168.1.35:8080/getComments", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: ticket
        });
        const responseText = await serverResponse.text();

        console.log ("------------------------------------------------------");
        console.log("Server response to getComments ():");
        console.log(responseText);
        console.log ("------------------------------------------------------");

        console.log ("----------------------------GETCOMMENTS ()--------------------------");
        const comments = JSON.parse (responseText);
        console.log (comments);
        console.log ("------------------------------------------------------");
        displayComments (comments);
    }
    catch (error) {
        console.error ("--------------------------ERROR TO GETCOMMENTS ()----------------------------");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
        console.error ("------------------------------------------------------");
    }
}
getComments ();

function displayComments(commentsArray) {
    const container = document.getElementById("commentsContainer");
    container.innerHTML = "";

    commentsArray.forEach(c => {
        const div = document.createElement("div");
        div.classList.add("commentBox");

        let attachmentHTML = "";
        
        if (c.filepath && c.filepath !== "null") {
            const fullUrl = `http://192.168.1.35:8080/${c.filepath}`;
            const fileName = c.filepath.split('/').pop(); 
            const extension = fileName.split('.').pop().toLowerCase();

            // Category mapping
            const images = ["jpg", "jpeg", "png", "gif", "webp"];
            const code = ["java", "cpp", "py", "js", "html", "css", "jar"];
            const docs = ["pdf", "txt", "doc", "docx"];

            if (images.includes(extension)) {
                // Show Image Preview
                attachmentHTML = `
                    <div class="file-preview">
                        <img src="${fullUrl}" style="max-width:200px; border-radius:8px; border:1px solid #ddd;">
                        <br><a href="${fullUrl}" target="_blank">View Full Image</a>
                    </div>`;
            } else {
                // Determine Icon
                let icon = "📁";
                if (extension === "pdf") icon = "📕";
                if (extension === "txt") icon = "📄";
                if (extension === "jar") icon = "☕";
                if (extension === "zip" || extension === "rar") icon = "📦";

                // Show Download Card
                attachmentHTML = `
                    <div class="file-card" style="display:flex; align-items:center; gap:10px; padding:12px; background:#f8f9fa; border:1px solid #e0e0e0; border-radius:8px; margin-top:10px;">
                        <span style="font-size:24px;">${icon}</span>
                        <div style="flex-grow:1;">
                            <div style="font-weight:bold; font-size:14px; word-break:break-all;">${fileName}</div>
                            <small style="color:#666;">${extension.toUpperCase()} File</small>
                        </div>
                        <a href="${fullUrl}" download="${fileName}" style="padding:5px 15px; background:#007bff; color:white; border-radius:5px; text-decoration:none; font-size:13px;">Download</a>
                    </div>`;
            }
        }
            div.innerHTML = `
                <h4>${c.commentTitle}</h4>
                <p>${c.comment}</p>
                ${attachmentHTML}
                <small style="display:block; margin-top:15px; color:#888;">By ${c.username} • ${c.madeOn}</small>
            `;
        
        container.appendChild(div);
    });
}

const themes = {
    basic: {
        "bg1": "#667eea", "bg2": "#49BC9B",
        "card-bg": "#ffffff", "card-border": "#49BC9B", "card-top-strip": "#667eea",
        "ticket-title": "#333", "ticket-text": "#444",
        "comment-bg": "#ffffff", "comment-border": "#6366f1", "comment-title": "#1f2937",
        "comment-text": "#374151", "comment-small": "#6b7280",
        "button-bg": "#667eea", "button-hover": "#5a67d8", "button-text": "#ffffff",
        "input-bg": "#f7f9fc", "input-border": "#d1d5db",
        "modal-bg": "#ffffff", "modal-strip": "#667eea"
    },
    royalLuxe: {
        "bg1": "#4e54c8", "bg2": "#8f94fb",
        "card-bg": "#ffffff", "card-border": "#8f94fb", "card-top-strip": "#4e54c8",
        "ticket-title": "#2e3160", "ticket-text": "#4a4e89",
        "comment-bg": "#fafaff", "comment-border": "#d7d9ff", "comment-title": "#2e3160",
        "comment-text": "#4a4e89", "comment-small": "#9d9ccc",
        "button-bg": "#6a6efb", "button-hover": "#4e54c8", "button-text": "#ffffff",
        "input-bg": "#f5f6ff", "input-border": "#d7d9ff",
        "modal-bg": "#ffffff", "modal-strip": "#4e54c8"
    },
    desert_slate: {
        "bg1": "#2d2926", "bg2": "#403a34",
        "card-bg": "#ffffff", "card-border": "#d97757", "card-top-strip": "#2d2926",
        "ticket-title": "#1a1a1a", "ticket-text": "#5d544b",
        "comment-bg": "#fffcf9", "comment-border": "#e8e2db", "comment-title": "#1a1a1a",
        "comment-text": "#5d544b", "comment-small": "#a69d95",
        "button-bg": "#d97757", "button-hover": "#bf6346", "button-text": "#ffffff",
        "input-bg": "#fdfaf7", "input-border": "#e8e2db",
        "modal-bg": "#ffffff", "modal-strip": "#2d2926"
    },
    cobalt_horizon: {
        "bg1": "#1e293b", "bg2": "#334155",
        "card-bg": "#ffffff", "card-border": "#3b82f6", "card-top-strip": "#1e293b",
        "ticket-title": "#0f172a", "ticket-text": "#475569",
        "comment-bg": "#f8fafc", "comment-border": "#e2e8f0", "comment-title": "#0f172a",
        "comment-text": "#475569", "comment-small": "#94a3b8",
        "button-bg": "#3b82f6", "button-hover": "#2563eb", "button-text": "#ffffff",
        "input-bg": "#f1f5f9", "input-border": "#e2e8f0",
        "modal-bg": "#ffffff", "modal-strip": "#1e293b"
    },
    phantom_forest: {
        "bg1": "#0f1d1a", "bg2": "#1c3a34",
        "card-bg": "#ffffff", "card-border": "#10b981", "card-top-strip": "#0f1d1a",
        "ticket-title": "#122621", "ticket-text": "#2d3735",
        "comment-bg": "#f0f7f5", "comment-border": "#e6efed", "comment-title": "#122621",
        "comment-text": "#2d3735", "comment-small": "#7ca29a",
        "button-bg": "#10b981", "button-hover": "#059669", "button-text": "#ffffff",
        "input-bg": "#f9fcfb", "input-border": "#e6efed",
        "modal-bg": "#ffffff", "modal-strip": "#0f1d1a"
    }
};

function applyTheme(name) {
    const theme = themes[name];
    if (!theme) return;
    
    Object.keys(theme).forEach(key => {
        document.documentElement.style.setProperty(`--${key}`, theme[key]);
    });
}

// Initial call
applyTheme('cobalt_horizon');

function setCorrectStatusButton () {
     const ticket = JSON.parse(sessionStorage.getItem("currentTicket"));
    if (ticket.status == 1) {
        document.getElementById ("changeStatusButton").innerText = "Close Ticket";
        console.log ("Ticket Is Currently Open");
    }
    else {
        document.getElementById ("changeStatusButton").innerText = "Open Ticket";
        console.log ("Ticket Is Currently Closed")
    }
}   

setCorrectStatusButton ();

async function changeStatus () {
    const ticketID = JSON.parse(sessionStorage.getItem("currentTicket")).id;
    const statusButtonText = document.getElementById ("changeStatusButton").innerText;
    let toStatus = 1;
    if (statusButtonText == "Close Ticket") {toStatus = 2}
    const ticket = `ticketID=${encodeURIComponent(ticketID)}&toStatus=${encodeURIComponent(toStatus)}`;

    try {
        const serverResponse = await fetch("http://192.168.1.35:8080/changeStatus", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: ticket
        });
        const responseText = await serverResponse.text();
        console.log ("changed Status");
    }
    catch (exception) {
        console.error (exception);
    }
    goBack ();
}
