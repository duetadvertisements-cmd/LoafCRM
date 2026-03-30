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
    window.location.href = 'Dashboards/dashboard.html';
}

setMainTicket();

async function addComment () {
    const title = prompt ();
    const data = prompt ();
    const creator = sessionStorage.getItem ("username");
    const ticket = JSON.parse(sessionStorage.getItem("currentTicket")).id;

    const comment = `title=${encodeURIComponent(title)}&data=${encodeURIComponent(data)}&creator=${encodeURIComponent(creator)}&ticket=${encodeURIComponent (ticket)}`;    

    try {
        const serverResponse = await fetch("http://localhost:8080/addComment", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: comment
        });

        const responseText = await serverResponse.text();

        console.log ("------------------------------------------------------");
        console.log("Server response to addComment ():");
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
        console.error ("-------------------------ERROR TO ADDCOMMENT ()-----------------------------");
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.error ("\n");
        console.error("Request failed:", error);
        console.error ("------------------------------------------------------");
    }
}

async function getComments () {
    const ticketID = JSON.parse(sessionStorage.getItem("currentTicket")).id;
    const ticket = `ticketID=${encodeURIComponent(ticketID)}`;

    try {
        const serverResponse = await fetch("http://localhost:8080/getComments", {
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