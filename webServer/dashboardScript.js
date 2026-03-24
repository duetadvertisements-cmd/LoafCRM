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

sessionStorage.setItem ("role", getRole (sessionStorage.getItem ("username")));

function setDashboardAccordingToRole () {
    //UNIMPLEMENTED FEATURE
}