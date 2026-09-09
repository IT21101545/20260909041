const officerSelect = document.getElementById("officer");
const departmentSelect = document.getElementById("department");
const trainingSelect = document.getElementById("training");
const form = document.getElementById("nominationForm");
const message = document.getElementById("message");
const table = document.getElementById("nominationTable");

async function getJson(url) {
    const response = await fetch(url);
    if (!response.ok) throw new Error("Unable to load data.");
    return response.json();
}

function setMessage(text, type) {
    message.textContent = text;
    message.className = "message " + type;
}

async function loadFormData() {
    try {
        const [officers, departments, trainings] = await Promise.all([
            getJson("/api/officers"),
            getJson("/api/departments"),
            getJson("/api/trainings")
        ]);

        officerSelect.innerHTML = '<option value="">Select officer</option>';
        officers.forEach(o => {
            officerSelect.innerHTML += `<option value="${o.id}">${o.name} — ${o.email}</option>`;
        });

        departmentSelect.innerHTML = '<option value="">Select department</option>';
        departments.forEach(d => {
            departmentSelect.innerHTML += `<option value="${d.id}">${d.name}</option>`;
        });

        trainingSelect.innerHTML = '<option value="">Select training</option>';
        trainings.forEach(t => {
            trainingSelect.innerHTML +=
                `<option value="${t.id}">${t.title} — ${t.date} — ${t.venue}</option>`;
        });
    } catch (e) {
        setMessage(e.message, "error");
    }
}

async function loadNominations() {
    try {
        const nominations = await getJson("/api/nominations");
        table.innerHTML = "";

        if (nominations.length === 0) {
            table.innerHTML = '<tr><td colspan="5">No nominations yet.</td></tr>';
            return;
        }

        nominations.forEach(n => {
            const statusClass = "status-" + n.status.toLowerCase();
            const canCancel = n.status === "CONFIRMED" || n.status === "WAITLISTED";
            table.innerHTML += `
                <tr>
                    <td>${n.officer.name}</td>
                    <td>${n.department.name}</td>
                    <td>${n.training.title}</td>
                    <td>${n.nominatedAt.replace("T", " ").substring(0, 16)}</td>
                    <td><span class="badge ${statusClass}">${n.status}</span></td>
                    <td>${canCancel
                        ? `<button class="secondary" onclick="cancelNomination(${n.id})">Cancel</button>`
                        : ""}</td>
                </tr>`;
        });
    } catch (e) {
        setMessage(e.message, "error");
    }
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage("", "");

    const payload = {
        officerId: Number(officerSelect.value),
        departmentId: Number(departmentSelect.value),
        trainingId: Number(trainingSelect.value)
    };

    try {
        const response = await fetch("/api/nominations", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "Nomination failed.");
        }

        setMessage("Nomination created successfully.", "success");
        form.reset();
        await loadNominations();
    } catch (e) {
        setMessage(e.message, "error");
    }
});

async function cancelNomination(id) {
    try {
        const response = await fetch(`/api/nominations/${id}/cancel`, { method: "POST" });
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "Cancel failed.");
        }

        setMessage("Nomination cancelled. Waiting list re-checked.", "success");
        await loadNominations();
    } catch (e) {
        setMessage(e.message, "error");
    }
}

loadFormData();
loadNominations();
