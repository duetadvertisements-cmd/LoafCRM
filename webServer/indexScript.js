async function signUp () {
    const username = document.getElementById ("signInPageUsernameInput").value;
    const email = document.getElementById ("signInPageEmailInput").value;
    const password = document.getElementById ("signInPagePasswordInput").value;

    const formData = `username=${encodeURIComponent(username)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`;

    try {
        const serverResponse = await fetch("http://localhost:8080/signup", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        });

        const responseText = await serverResponse.text();

        console.log("Server response:");
        console.log(responseText);

        if (responseText == "SUCCESS") {
            alert ("Success");

            document.getElementById("signInPageUsernameInput").value = "";
            document.getElementById("signInPageEmailInput").value = "";
            document.getElementById("signInPagePasswordInput").value = "";
        }

        else {
            alert ("Failed");

            document.getElementById("signInPageUsernameInput").value = "";
            document.getElementById("signInPageEmailInput").value = "";
            document.getElementById("signInPagePasswordInput").value = "";
        }

    } catch (error) {
        console.error ("YOU FORGOT TO TURN THE SERVER ON");
        console.log ("\n");
        console.error("Request failed:", error);
    }
}

async function login() {
    const username = document.getElementById("loginPageUsernameInput").value;
    const password = document.getElementById("loginPagePasswordInput").value;

    const formData = `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`;

    try {
        const serverResponse = await fetch("http://localhost:8080/login", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        });

        const responseText = await serverResponse.text();

        console.log("Server response:", responseText);

        // ✅ Handle responses
        if (responseText === "SUCCESS") {
            alert("Login successful!");

            // optional: redirect
            sessionStorage.setItem("username", username);
            sessionStorage.setItem ("role", await (getRole (sessionStorage.getItem ("username"))));
            const role = sessionStorage.getItem ("role");
            if (role == "ADMIN") {
                window.location.href = "Admindashboard.html";
            }
            if (role == "WORKER") {
                window.location.href = "Workerdashboard.html";
            }
            if (role == "CLIENT") {
                window.location.href = "dashboard.html";
            }
            else {
                console.log ("An error occured, couldn't redirect line 78")
            }
            

        } else if (responseText === "WRONG_PASSWORD") {
            alert("Wrong password");

        } else if (responseText === "USER_NOT_FOUND") {
            alert("User not found");

        } else {
            alert("Something went wrong");
        }

    } catch (error) {
        console.error("Server not running or request failed", error);
        alert("Cannot connect to server");
    }
}

function showLogin() {
    document.getElementById("signUpPage").classList.remove("active");
    document.getElementById("loginPage").classList.add("active");
}

function showSignUp() {
    document.getElementById("loginPage").classList.remove("active");
    document.getElementById("signUpPage").classList.add("active");
}

async function getRole(username) {

    const formData = `username=${encodeURIComponent(username)}`;

    try {
        const res = await fetch("http://localhost:8080/getRole", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData
        });

        const role = await res.text();

        console.log("User role:", role);

        return role;

    } catch (error) {
        console.error("Error getting role:", error);
        return null;
    }
}