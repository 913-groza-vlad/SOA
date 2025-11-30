import React, { useEffect, useState } from "react";
import {
  fetchAppointmentsForDoctor,
  cancelAppointment,
} from "../api/appointmentApi";

export default function DoctorAppointmentList({ doctorId }) {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function load() {
    try {
      setLoading(true);
      setError("");
      const data = await fetchAppointmentsForDoctor(doctorId);
      setAppointments(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      setError("Failed to load appointments");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!doctorId) return;
    load();
  }, [doctorId]);

  async function handleCancel(id) {
    if (!window.confirm("Cancel this appointment?")) return;
    try {
      await cancelAppointment(id);
      await load();
    } catch (err) {
      console.error(err);
      setError(err.message || "Failed to cancel appointment");
    }
  }

  if (loading) {
    return (
      <div className="card">
        <h2>My appointments</h2>
        <p>Loading…</p>
      </div>
    );
  }

  return (
    <div className="card">
      <h2>My appointments</h2>
      {error && <div className="error">{error}</div>}

      {appointments.length === 0 ? (
        <p className="muted">No appointments yet.</p>
      ) : (
        <div className="table-wrapper">
          <table className="table">
            <thead>
              <tr>
                <th>Patient</th>
                <th>Start</th>
                <th>End</th>
                <th>Status</th>
                <th>Notes</th>
                <th style={{ width: "120px" }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {appointments.map((a) => (
                <tr key={a.id}>
                  <td>{a.patientId}</td>
                  <td>{a.startTime}</td>
                  <td>{a.endTime}</td>
                  <td>{a.status}</td>
                  <td>{a.notes}</td>
                  <td>
                    <button
                      className="small danger"
                      onClick={() => handleCancel(a.id)}
                      disabled={a.status === "CANCELLED"}
                    >
                      Cancel
                    </button>
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
