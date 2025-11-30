import React, { useEffect, useState } from "react";
import {
  fetchAppointmentsForPatient,
  updateAppointment,
  cancelAppointment,
} from "../api/appointmentApi";

export default function AppointmentList({ patientId }) {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [editingId, setEditingId] = useState(null);
  const [editStart, setEditStart] = useState("");
  const [editEnd, setEditEnd] = useState("");
  const [editNotes, setEditNotes] = useState("");

  async function load() {
    try {
      setLoading(true);
      setError("");
      // NOW: this returns an ARRAY, not a Page
      const data = await fetchAppointmentsForPatient(patientId);
      setAppointments(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      setError("Failed to load appointments");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!patientId) return;
    load();

    // optional: listen for global refresh events if you want to trigger reload from other components
    function handleRefresh() {
      load();
    }
    window.addEventListener("appointments:refresh", handleRefresh);
    return () => window.removeEventListener("appointments:refresh", handleRefresh);
  }, [patientId]);

  function startEdit(a) {
    setEditingId(a.id);
    // assuming ISO strings like 2025-12-05T11:00:00
    setEditStart(a.startTime?.slice(0, 16) || "");
    setEditEnd(a.endTime?.slice(0, 16) || "");
    setEditNotes(a.notes || "");
  }

  function cancelEdit() {
    setEditingId(null);
    setEditStart("");
    setEditEnd("");
    setEditNotes("");
  }

  async function saveEdit(id) {
    try {
      await updateAppointment(id, {
        startTime: editStart,
        endTime: editEnd,
        notes: editNotes,
        // keep status as SCHEDULED when editing, or adjust if you support other transitions
        status: "SCHEDULED",
      });
      cancelEdit();
      await load();
    } catch (err) {
      console.error(err);
      setError("Failed to update appointment");
    }
  }

  async function handleCancel(id) {
    if (!window.confirm("Cancel this appointment?")) return;
    try {
      await cancelAppointment(id);
      await load();
    } catch (err) {
      console.error(err);
      setError("Failed to cancel appointment");
    }
  }

  if (loading) {
    return (
      <div className="card">
        <h2>My appointments</h2>
        <p>Loading appointments…</p>
      </div>
    );
  }

  return (
    <div className="card">
      <h2>My appointments</h2>
      {error && <div className="error">{error}</div>}

      {appointments.length === 0 ? (
        <p className="muted">No appointments yet. Use the form above to create one.</p>
      ) : (
        <div className="table-wrapper">
          <table className="table">
            <thead>
              <tr>
                <th>Doctor</th>
                <th>Start</th>
                <th>End</th>
                <th>Status</th>
                <th>Notes</th>
                <th style={{ width: "180px" }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {appointments.map((a) => (
                <tr key={a.id}>
                  <td>{a.doctorId}</td>
                  <td>
                    {editingId === a.id ? (
                      <input
                        type="datetime-local"
                        value={editStart}
                        onChange={(e) => setEditStart(e.target.value)}
                      />
                    ) : (
                      a.startTime
                    )}
                  </td>
                  <td>
                    {editingId === a.id ? (
                      <input
                        type="datetime-local"
                        value={editEnd}
                        onChange={(e) => setEditEnd(e.target.value)}
                      />
                    ) : (
                      a.endTime
                    )}
                  </td>
                  <td>{a.status}</td>
                  <td>
                    {editingId === a.id ? (
                      <input
                        type="text"
                        value={editNotes}
                        onChange={(e) => setEditNotes(e.target.value)}
                      />
                    ) : (
                      a.notes
                    )}
                  </td>
                  <td>
                    {editingId === a.id ? (
                      <>
                        <button className="small" onClick={() => saveEdit(a.id)}>
                          Save
                        </button>
                        <button className="small ghost" onClick={cancelEdit}>
                          Cancel
                        </button>
                      </>
                    ) : (
                      <>
                        <button className="small" onClick={() => startEdit(a)}>
                          Edit
                        </button>
                        <button
                          className="small danger"
                          onClick={() => handleCancel(a.id)}
                        >
                          Cancel
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}