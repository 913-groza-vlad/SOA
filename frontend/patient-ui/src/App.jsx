import React, { useEffect, useState } from "react";
import PatientSelector from "./components/PatientSelector";
import AppointmentForm from "./components/AppointmentForm";
import AppointmentList from "./components/AppointmentList";
import { decodeToken, getRoleFromToken } from "./utils/jwt";

const TOKEN_KEY = "authToken";
const PATIENT_ID_KEY = "patientId";

export default function App() {
  const [patientId, setPatientId] = useState(
    () => localStorage.getItem(PATIENT_ID_KEY)
  );
  const [username, setUsername] = useState("");
  const [role, setRole] = useState("");
  const [authError, setAuthError] = useState("");
  const [authChecked, setAuthChecked] = useState(false);

  useEffect(() => {
    let mounted = true;

    (function runAuthCheck() {
      const token = localStorage.getItem(TOKEN_KEY);
      let finalAuthError = "";
      let finalRole = "";
      let finalUsername = "";

      if (!token) {
        finalAuthError = "No token found. Please log in first.";
      } else {
        const decoded = decodeToken(token);
        if (!decoded) {
          finalAuthError = "Invalid token. Please log in again.";
        } else {
          const r = getRoleFromToken(token);
          finalRole = r || "";
          finalUsername = decoded.sub || decoded.username || "";
          if (r && r !== "PATIENT") {
            finalAuthError = `This UI is for PATIENT role. You are logged in as ${r}.`;
          }
        }
      }

      if (!mounted) return;
      // schedule updates to avoid synchronous setState inside effect body
      setTimeout(() => {
        if (!mounted) return;
        setRole(finalRole);
        setUsername(finalUsername);
        setAuthError(finalAuthError);
        setAuthChecked(true);
      }, 0);
    })();

    return () => {
      mounted = false;
    };
  }, []);

  function handlePatientSelected(id) {
    setPatientId(id);
  }

  if (!authChecked) {
    return null; // or a small loading indicator
  }

  if (authError) {
    return (
      <div className="page">
        <div className="card">
          <h2>Access issue</h2>
          <p>{authError}</p>
          <p>
            Go back to <a href="/login/">login</a> and sign in with a patient account.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <header className="topbar">
        <div>
          <strong>MedApp – Patient Portal</strong>
        </div>
        <div className="topbar-right">
          <span className="muted">
            Logged in as <strong>{username}</strong> ({role})
          </span>
          <button
            className="small ghost"
            onClick={() => {
              localStorage.removeItem(TOKEN_KEY);
              localStorage.removeItem(PATIENT_ID_KEY);
              window.location.href = "/login/";
            }}
          >
            Logout
          </button>
        </div>
      </header>

      <main className="content">
        {!patientId ? (
          <PatientSelector onSelected={handlePatientSelected} />
        ) : (
          <>
            <AppointmentForm
              patientId={Number(patientId)}
              onCreated={() => {
                // we’ll let AppointmentList reload itself via a "refresh" trick if needed
                window.dispatchEvent(new Event("appointments:refresh"));
              }}
            />
            <AppointmentList patientId={Number(patientId)} />
          </>
        )}
      </main>
    </div>
  );
}