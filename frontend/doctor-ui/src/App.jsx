import React, { useEffect, useState } from "react";
import DoctorSelector from "./components/DoctorSelector";
import DoctorAppointmentList from "./components/DoctorAppointmentList";
import NotificationBell from "./components/NotificationBell";
import { decodeToken, getRoleFromToken, getUserIdFromToken, getUsernameFromToken } from "./utils/jwt";
import { fetchDoctors } from "./api/doctorApi";

const TOKEN_KEY = "authToken";
const DOCTOR_ID_KEY = "doctorId";

export default function App() {
  const [doctorId, setDoctorId] = useState(
    () => localStorage.getItem(DOCTOR_ID_KEY)
  );
  const [authError, setAuthError] = useState("");
  const [username, setUsername] = useState("");
  const [role, setRole] = useState("");
  const [userId, setUserId] = useState(null);
  const [authChecked, setAuthChecked] = useState(false);

  useEffect(() => {
    let mounted = true;

    (function runAuthCheck() {
      const token = localStorage.getItem(TOKEN_KEY);
      let finalAuthError = "";
      let finalRole = "";
      let finalUsername = "";
      let finalUserId = null;

      if (!token) {
        finalAuthError = "No token found. Please log in first.";
      } else {
        const decoded = decodeToken(token);
        if (!decoded) {
          finalAuthError = "Invalid token. Please log in again.";
        } else {
          const r = getRoleFromToken(token);
          finalRole = r || "";
          finalUsername = getUsernameFromToken(token) || "";
          finalUserId = getUserIdFromToken(token);
          if (r !== "DOCTOR") {
            finalAuthError = `This UI is for DOCTOR role. You are logged in as ${r || "UNKNOWN"}.`;
          }
        }
      }

      if (!mounted) return;
      setTimeout(() => {
        if (!mounted) return;
        setRole(finalRole);
        setUsername(finalUsername);
        setUserId(finalUserId);
        setAuthError(finalAuthError);
        setAuthChecked(true);
      }, 0);
    })();

    return () => {
      mounted = false;
    };
  }, []);

  function handleDoctorSelected(id) {
    setDoctorId(id);
  }

  // If authenticated and we don't have a doctor selected, try to auto-resolve
  useEffect(() => {
    let mounted = true;
    if (!authChecked || !userId || doctorId) return;

    (async function findMyDoctor() {
      try {
        const page = await fetchDoctors(0, 200);
        if (!mounted) return;
        const content = page.content || page || [];
        const match = content.find((d) => d.userId != null && Number(d.userId) === Number(userId));
        if (match) {
          localStorage.setItem(DOCTOR_ID_KEY, String(match.id));
          setDoctorId(String(match.id));
        }
      } catch (err) {
        // ignore — user can still select manually
        console.error("Failed to auto-resolve doctor:", err);
      }
    })();

    return () => {
      mounted = false;
    };
  }, [authChecked, userId, doctorId]);

  if (!authChecked) return null;

  if (authError) {
    return (
      <div className="page">
        <div className="card">
          <h2>Access issue</h2>
          <p>{authError}</p>
          <p>
            Go back to <a href="/login/">login</a> and sign in with a doctor account.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <header className="topbar">
        <div>
          <strong>MedApp – Doctor Portal</strong>
        </div>
        <div className="topbar-right">
          <span className="muted">
            Logged in as <strong>{username}</strong> ({role})
          </span>
          {userId && <NotificationBell userId={userId} />}
          <button
            className="small ghost"
            onClick={() => {
              localStorage.removeItem(TOKEN_KEY);
              localStorage.removeItem(DOCTOR_ID_KEY);
              window.location.href = "/login/";
            }}
          >
            Logout
          </button>
        </div>
      </header>

      <main className="content">
        {!doctorId ? (
          <DoctorSelector onSelected={handleDoctorSelected} />
        ) : (
          <DoctorAppointmentList doctorId={Number(doctorId)} />
        )}
      </main>
    </div>
  );
}