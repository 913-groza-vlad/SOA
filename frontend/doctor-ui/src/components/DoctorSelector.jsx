import React, { useEffect, useState } from "react";
import { fetchDoctors } from "../api/doctorApi";
import { getUsernameFromToken } from "../utils/jwt";

const DOCTOR_ID_KEY = "doctorId";
const TOKEN_KEY = "authToken";

export default function DoctorSelector({ onSelected }) {
  const [doctors, setDoctors] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY);
    const username = getUsernameFromToken(token);

    async function load() {
      try {
        setLoading(true);
        const page = await fetchDoctors(0, 100);
        const content = page.content || page;
        setDoctors(content);

        // naive auto-match: if doctor.email == username
        const match = content.find(
          (d) => d.email && d.email.toLowerCase() === String(username || "").toLowerCase()
        );
        if (match) setSelectedId(match.id);
      } catch (err) {
        console.error(err);
        setError("Failed to load doctors");
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  function handleConfirm() {
    if (!selectedId) {
      setError("Please select your doctor profile");
      return;
    }
    localStorage.setItem(DOCTOR_ID_KEY, String(selectedId));
    onSelected(selectedId);
  }

  if (loading) return <div className="card">Loading doctors…</div>;

  return (
    <div className="card">
      <h2>Select your doctor profile</h2>
      <p className="muted">Choose which Doctor record corresponds to your user.</p>

      {error && <div className="error">{error}</div>}

      <div className="table-wrapper">
        <table className="table">
          <thead>
            <tr>
              <th></th>
              <th>Full name</th>
              <th>Email</th>
              <th>Specialty</th>
            </tr>
          </thead>
          <tbody>
            {doctors.map((d) => (
              <tr key={d.id}>
                <td>
                  <input
                    type="radio"
                    name="doctor"
                    checked={selectedId === d.id}
                    onChange={() => setSelectedId(d.id)}
                  />
                </td>
                <td>{d.firstName} {d.lastName}</td>
                <td>{d.email}</td>
                <td>{d.specialty}</td>
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
