let currentEmail = null;
let lastProcessed = "";

async function getEmail() {
    try {
        const res = await fetch("http://localhost:8080/getEmail");
        const email = await res.json();

        const unique = email.subject + email.from;

        // prevent duplicates
        if (unique === lastProcessed) return;

        lastProcessed = unique;
        currentEmail = email;

        console.log(email);

        document.getElementById("subject").innerText = email.subject;
        document.getElementById("body").innerText = email.body;
        document.getElementById("from").innerText = email.from;

    } catch (err) {
        console.error(err);
    }
}

async function createTicketFromEmail(email) {
    const formData = new URLSearchParams();

    formData.append("title", email.subject);
    formData.append("data", email.body);
    formData.append("creator", "EMAIL_SYSTEM");
    formData.append("toEmail", email.from);
    formData.append("madeBy", "EMAIL");

    await fetch("http://localhost:8080/addTicket", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: formData.toString()
    });
}

setInterval(getEmail, 10000); // every 10 seconds