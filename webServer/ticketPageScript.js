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

        div.innerHTML = `
            <h4>${c.commentTitle}</h4>
            <p>${c.comment}</p>
            <small>By ${c.username} • ${c.madeOn}</small>
        `;

        container.appendChild(div);
    });
}