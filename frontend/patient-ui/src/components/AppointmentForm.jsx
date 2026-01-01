import React, { useEffect, useState } from "react";
import { fetchDoctors } from "../api/doctorApi";
import { createAppointment } from "../api/appointmentApi";

export default function AppointmentForm({ patientId, onCreated }) {
  const [doctors, setDoctors] = useState([]);
  const [doctorId, setDoctorId] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");
  const [notes, setNotes] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    async function loadDoctors() {
      try {
        const page = await fetchDoctors(0, 100);
        const content = page.content || page;
        setDoctors(content);
      } catch (err) {
        console.error(err);
        setError("Failed to load doctors");
      }
    }
    loadDoctors();
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!doctorId || !startTime || !endTime) {
      setError("Please select doctor and time range");
      return;
    }

    try {
      setLoading(true);
      const payload = {
        patientId,
        doctorId: Number(doctorId),
        startTime,
        endTime,
        notes,
      };
      const createdId = await createAppointment(payload);
      setSuccess("Appointment created successfully");
      setNotes("");
      onCreated && onCreated(createdId);
    } catch (err) {
      console.error(err);
      setError("Failed to create appointment");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="card">
      <h2>Book a new appointment</h2>
      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}

      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          Doctor
          <select value={doctorId} onChange={(e) => setDoctorId(e.target.value)} required>
            <option value="">Select doctor…</option>
            {doctors.map((d) => (
              <option key={d.id} value={d.id}>
                Dr. {d.firstName} {d.lastName} ({d.specialization})
              </option>
            ))}
          </select>
        </label>

        <label>
          Start time
          <input
            type="datetime-local"
            value={startTime}
            onChange={(e) => setStartTime(e.target.value)}
            required
          />
        </label>

        <label>
          End time
          <input
            type="datetime-local"
            value={endTime}
            onChange={(e) => setEndTime(e.target.value)}
            required
          />
        </label>

        <label className="full-width">
          Notes
          <textarea
            rows={3}
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Optional notes for the doctor"
          />
        </label>

        <div className="full-width">
          <button type="submit" className="primary" disabled={loading}>
            {loading ? "Saving…" : "Create appointment"}
          </button>
        </div>
      </form>
    </div>
  );
}