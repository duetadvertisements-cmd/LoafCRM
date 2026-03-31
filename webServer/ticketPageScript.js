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
        const response = await fetch("http://192.168.1.34:8080/addComment", {
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
        const serverResponse = await fetch("http://192.168.1.34:8080/getComments", {
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
            const fullUrl = `http://192.168.1.34:8080/${c.filepath}`;
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