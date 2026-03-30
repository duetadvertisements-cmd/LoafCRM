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

        console.log("Server response:");
        console.log(responseText);

        if (responseText == "SUCCESS") {
            alert ("Success");
        }

        else {
            alert ("Failed");
        }

        history.go ();

    } catch (error) {
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
    }
}