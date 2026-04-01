function setMainTicket() {
    const ticket = JSON.parse(sessionStorage.getItem("currentTicket"));
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
                <strong>Ticket ID:</strong> #${ticket.id}
            </div>
        </div>
    `;
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

    try {
        const response = await fetch("http://192.168.1.33:8080/addComment", {
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
        const serverResponse = await fetch("http://192.168.1.33:8080/getComments", {
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
            const fullUrl = `http://192.168.1.33:8080/${c.filepath}`;
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
        "--bg1": "#667eea",
        "--bg2": "#49BC9B",
        "--card-bg": "#ffffff",
        "--card-border": "#e0e0e0",
        "--card-top-strip": "#49BC9B",

        "--ticket-title": "#222",
        "--ticket-text": "#444",
        "--ticket-id-bg": "linear-gradient(135deg, #667eea, #49BC9B)",

        "--comment-bg": "#fafafa",
        "--comment-title": "#333",
        "--comment-text": "#555",
        "--comment-border": "#ddd",

        "--button-bg": "#667eea",
        "--button-hover": "#5a67d8",
        "--button-text": "#fff",

        "--input-bg": "#fff",
        "--input-border": "#ccc",
        "--input-focus": "#667eea"
    },

    sunsetPro: {
        "--bg1": "#ff9966",
        "--bg2": "#ff5e62",
        "--card-bg": "#fff7f3",
        "--card-border": "#ffb199",
        "--card-top-strip": "#ff5e62",

        "--ticket-title": "#662200",
        "--ticket-text": "#773300",
        "--ticket-id-bg": "linear-gradient(135deg, #ff9966, #ff5e62)",

        "--comment-bg": "#fff1e8",
        "--comment-title": "#552200",
        "--comment-text": "#663300",
        "--comment-border": "#ffb199",

        "--button-bg": "#ff5e62",
        "--button-hover": "#ff3b42",
        "--button-text": "#fff",

        "--input-bg": "#fff7f3",
        "--input-border": "#ffb199",
        "--input-focus": "#ff5e62"
    },

    aquaBreeze: {
        "--bg1": "#6dd5ed",
        "--bg2": "#2193b0",
        "--card-bg": "#f0fcff",
        "--card-border": "#a8e9f6",
        "--card-top-strip": "#2193b0",

        "--ticket-title": "#003a4d",
        "--ticket-text": "#004d66",
        "--ticket-id-bg": "linear-gradient(135deg, #6dd5ed, #2193b0)",

        "--comment-bg": "#e6f9ff",
        "--comment-title": "#003644",
        "--comment-text": "#005066",
        "--comment-border": "#a8e9f6",

        "--button-bg": "#2193b0",
        "--button-hover": "#1a6f85",
        "--button-text": "#fff",

        "--input-bg": "#f0fcff",
        "--input-border": "#a8e9f6",
        "--input-focus": "#2193b0"
    },

    emeraldMist: {
        "--bg1": "#00b894",
        "--bg2": "#55efc4",
        "--card-bg": "#f3fffa",
        "--card-border": "#a5f2d6",
        "--card-top-strip": "#00b894",

        "--ticket-title": "#004d3d",
        "--ticket-text": "#006652",
        "--ticket-id-bg": "linear-gradient(135deg, #00b894, #55efc4)",

        "--comment-bg": "#e8fff7",
        "--comment-title": "#004033",
        "--comment-text": "#00604d",
        "--comment-border": "#a5f2d6",

        "--button-bg": "#00b894",
        "--button-hover": "#009a7a",
        "--button-text": "#fff",

        "--input-bg": "#f3fffa",
        "--input-border": "#a5f2d6",
        "--input-focus": "#00b894"
    },

    royalLuxe: {
        "--bg1": "#654ea3",
        "--bg2": "#eaafc8",
        "--card-bg": "#faf5ff",
        "--card-border": "#d8b7ff",
        "--card-top-strip": "#654ea3",

        "--ticket-title": "#2f1b52",
        "--ticket-text": "#3c2466",
        "--ticket-id-bg": "linear-gradient(135deg, #654ea3, #eaafc8)",

        "--comment-bg": "#f4ebff",
        "--comment-title": "#2c1a4b",
        "--comment-text": "#3a2362",
        "--comment-border": "#d8b7ff",

        "--button-bg": "#654ea3",
        "--button-hover": "#513d85",
        "--button-text": "#fff",

        "--input-bg": "#faf5ff",
        "--input-border": "#d8b7ff",
        "--input-focus": "#654ea3"
    },

    cyberNight2: {
        "--bg1": "#0a0f24",
        "--bg2": "#111d4a",
        "--card-bg": "#141a33",
        "--card-border": "#1e2b55",
        "--card-top-strip": "#00eaff",

        "--ticket-title": "#d6eaff",
        "--ticket-text": "#b6c9ff",
        "--ticket-id-bg": "linear-gradient(135deg, #00eaff, #0088ff)",

        "--comment-bg": "#10162c",
        "--comment-title": "#c5d8ff",
        "--comment-text": "#a6bbff",
        "--comment-border": "#1e2b55",

        "--button-bg": "#0088ff",
        "--button-hover": "#006bd1",
        "--button-text": "#fff",

        "--input-bg": "#0d142b",
        "--input-border": "#1e2b55",
        "--input-focus": "#00eaff"
    }
};

function applyTheme(name) {
    const theme = themes[name];
    Object.entries(theme).forEach(([key, value]) => {
        document.documentElement.style.setProperty(key, value);
    });
}
applyTheme ("cyberNight2");