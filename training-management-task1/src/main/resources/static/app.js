const officerSelect = document.getElementById("officer");
const departmentSelect = document.getElementById("department");
const trainingSelect = document.getElementById("training");
const form = document.getElementById("nominationForm");
const message = document.getElementById("message");
const table = document.getElementById("nominationTable");
const officerDetails = document.getElementById("officerDetails");
const trainingRules = document.getElementById("trainingRules");
const eligibilityPreview = document.getElementById("eligibilityPreview");
let officers = [];
let trainings = [];
let selectedRules = [];

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
        const [loadedOfficers, departments, loadedTrainings] = await Promise.all([
            getJson("/api/officers"),
            getJson("/api/departments"),
            getJson("/api/trainings")
        ]);
        officers = loadedOfficers;
        trainings = loadedTrainings;

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
        await refreshEligibilityPanel();
    } catch (e) {
        setMessage(e.message, "error");
    }
}

function ruleLabel(rule) {
    return {
        ALLOWED_DEPARTMENT: "Department",
        ALLOWED_DESIGNATION: "Designation",
        MIN_YEARS_OF_SERVICE: "Minimum years of service",
        NO_RECENT_PARTICIPATION_MONTHS: "No participation within"
    }[rule.ruleType] || rule.ruleType;
}

function renderOfficerDetails() {
    const officer = officers.find(item => item.id === Number(officerSelect.value));
    if (!officer) {
        officerDetails.textContent = "Select an officer to view their profile.";
        officerDetails.className = "details empty";
        return;
    }

    officerDetails.className = "details";
    officerDetails.innerHTML = `<strong>${officer.name}</strong><span>${officer.department.name}</span>
        <span>${officer.designation || "Designation not set"}</span>
        <span>${officer.yearsOfService ?? "Not set"} years of service</span>`;
}

function renderTrainingRules() {
    if (!trainingSelect.value) {
        trainingRules.textContent = "Select a training programme to view its rules.";
        trainingRules.className = "rules empty";
        return;
    }

    trainingRules.className = "rules";
    if (selectedRules.length === 0) {
        trainingRules.innerHTML = "<strong>No additional eligibility rules</strong><span>All officers may apply.</span>";
        return;
    }

    trainingRules.innerHTML = `<strong>Eligibility rules</strong>${selectedRules.map(rule =>
        `<span><b>${ruleLabel(rule)}:</b> ${rule.ruleValue}${rule.ruleType === "NO_RECENT_PARTICIPATION_MONTHS" ? " months" : ""}</span>`).join("")}`;
}

function renderEligibilityPreview() {
    const officer = officers.find(item => item.id === Number(officerSelect.value));
    if (!officer || !trainingSelect.value) {
        eligibilityPreview.textContent = "Select an officer and training programme to check eligibility.";
        eligibilityPreview.className = "eligibility-preview empty";
        return;
    }

    const departmentRules = selectedRules.filter(rule => rule.ruleType === "ALLOWED_DEPARTMENT");
    const designationRules = selectedRules.filter(rule => rule.ruleType === "ALLOWED_DESIGNATION");
    const minimumService = selectedRules.find(rule => rule.ruleType === "MIN_YEARS_OF_SERVICE");
    const eligible = (!departmentRules.length || departmentRules.some(rule =>
        rule.ruleValue.toLowerCase() === officer.department.name.toLowerCase()))
        && (!designationRules.length || designationRules.some(rule =>
            rule.ruleValue.toLowerCase() === (officer.designation || "").toLowerCase()))
        && (!minimumService || (officer.yearsOfService ?? -1) >= Number(minimumService.ruleValue));

    eligibilityPreview.textContent = eligible
        ? "Eligible to nominate for this training."
        : "Not eligible for this training based on the current rules.";
    eligibilityPreview.className = `eligibility-preview ${eligible ? "eligible" : "ineligible"}`;
}

async function refreshEligibilityPanel() {
    renderOfficerDetails();
    if (!trainingSelect.value) {
        selectedRules = [];
        renderTrainingRules();
        renderEligibilityPreview();
        return;
    }

    try {
        selectedRules = await getJson(`/api/eligibility-rules/training/${trainingSelect.value}`);
        renderTrainingRules();
        renderEligibilityPreview();
    } catch (e) {
        selectedRules = [];
        trainingRules.textContent = "Unable to load eligibility rules.";
        trainingRules.className = "rules error-text";
    }
}

officerSelect.addEventListener("change", refreshEligibilityPanel);
trainingSelect.addEventListener("change", refreshEligibilityPanel);

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
