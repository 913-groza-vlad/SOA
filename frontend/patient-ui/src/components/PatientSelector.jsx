import React, { useEffect, useState } from "react";
import { fetchPatients } from "../api/patientApi";
import { getUsernameFromToken } from "../utils/jwt";

const PATIENT_ID_KEY = "patientId";
const TOKEN_KEY = "authToken";

export default function PatientSelector({ onSelected }) {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedId, setSelectedId] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY);
    const username = getUsernameFromToken(token);

    async function load() {
      try {
        setLoading(true);
        const page = await fetchPatients(0, 100); // load first page
        const content = page.content || page; // depending on how Page is serialized
        setPatients(content);

        // naive auto-preselect: if a patient email equals username
        const match = content.find(
          (p) => p.email && p.email.toLowerCase() === String(username || "").toLowerCase()
        );
        if (match) {
          setSelectedId(match.id);
        }
      } catch (err) {
        console.error(err);
        setError("Failed to load patients");
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  function handleConfirm() {
    if (!selectedId) {
      setError("Please select a patient profile");
      return;
    }
    localStorage.setItem(PATIENT_ID_KEY, String(selectedId));
    onSelected(selectedId);
  }

  if (loading) return <div className="centered">Loading patients…</div>;

  return (
    <div className="card">
      <h2>Select your patient profile</h2>
      <p className="muted">
        We need to associate your user with one of the Patient records. Choose the right one below.
      </p>

      {error && <div className="error">{error}</div>}

      <div className="table-wrapper">
        <table className="table">
          <thead>
            <tr>
              <th></th>
              <th>Full name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Date of birth</th>
            </tr>
          </thead>
          <tbody>
            {patients.map((p) => (
              <tr key={p.id}>
                <td>
                  <input
                    type="radio"
                    name="patient"
                    checked={selectedId === p.id}
                    onChange={() => setSelectedId(p.id)}
                  />
                </td>
                <td>{p.firstName} {p.lastName}</td>
                <td>{p.email}</td>
                <td>{p.phone}</td>
                <td>{p.dateOfBirth}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <button className="primary" onClick={handleConfirm}>
        Continue
      </button>
    </div>
  );
}