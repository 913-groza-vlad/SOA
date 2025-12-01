import React, { useEffect, useState, useRef } from "react";
import {
  getAllEvents,
  getAppointmentEvents,
  getNotificationEvents,
} from "./api/adminEventsApi";
import EventTable from "./components/EventTable";
import EventDetailsModal from "./components/EventDetailsModal";
import Pagination from "./components/Pagination";
import { decodeToken, getRoleFromToken, getUsernameFromToken } from "./utils/jwt";
import { createAdminEventsClient } from "./socket/AdminEventsSocket";
import "./App.css";

const TOKEN_KEY = "authToken";

export default function App() {
  const [events, setEvents] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filter, setFilter] = useState("all");
  const [newEventCount, setNewEventCount] = useState(0);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [username, setUsername] = useState("");
  const [role, setRole] = useState("");
  const [authError, setAuthError] = useState("");
  const [authChecked, setAuthChecked] = useState(false);
  const stompClientRef = useRef(null);
  const pageRef = useRef(page);
  const newEventsRef = useRef([]);

  // Auth check
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
          finalUsername = getUsernameFromToken(token);
          if (r !== "ADMIN") {
            finalAuthError = `This UI requires ADMIN role. You are logged in as ${r}.`;
          }
        }
      }

      if (!mounted) return;
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

  async function loadEvents(page, filter) {
    try {
      let result;
      switch (filter) {
        case "appointments":
          result = await getAppointmentEvents(page);
          break;
        case "notifications":
          result = await getNotificationEvents(page);
          break;
        default:
          result = await getAllEvents(page);
      }

      setEvents(result.content || []);
      setTotalPages(result.totalPages || 1);
    } catch (err) {
      console.error(err);
    }
  }

  // Initial load (wait until auth checked)
  useEffect(() => {
    if (authError || !authChecked) return;
    const t = setTimeout(() => loadEvents(page, filter), 0);
    return () => clearTimeout(t);
  }, [page, filter, authError, authChecked]);

  // keep a ref of the current page so websocket handler can decide where to apply incoming events
  useEffect(() => {
    pageRef.current = page;
    // if user navigated to the first page and we have buffered events, show them
    if (page === 0 && newEventsRef.current.length > 0) {
      setEvents((prev) => {
        const merged = [...newEventsRef.current, ...prev];
        // limit overall list size
        return merged.slice(0, 50);
      });
      newEventsRef.current = [];
      setNewEventCount(0);
    }
  }, [page]);

  // If filter changes, drop any buffered events because they may not match the new filter
  useEffect(() => {
    newEventsRef.current = [];
    setTimeout(() => setNewEventCount(0), 0);
  }, [filter]);

  // WebSocket connection
  useEffect(() => {
    if (authError || !authChecked) return;

    const client = createAdminEventsClient((event) => {
      // If filter doesn’t match, ignore
      if (filter === "appointments" && event.source !== "KAFKA_APPOINTMENT") {
        return;
      }
      if (filter === "notifications" && event.source !== "RABBIT_NOTIFICATION") {
        return;
      }

      // If user is viewing the first page, show live update immediately
      if (pageRef.current === 0) {
        setEvents((prev) => [event, ...prev].slice(0, 50));
      } else {
        // buffer events while user is on another page
        newEventsRef.current.unshift(event);
        setNewEventCount(newEventsRef.current.length);
      }
    });

    stompClientRef.current = client;
    if (client && typeof client.activate === 'function') client.activate();

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
  }, [authError, filter, authChecked]);

  if (authError) {
    return (
      <div className="page">
        <div className="card">
          <h2>Access denied</h2>
          <p>{authError}</p>
          <p>
            Go back to <a href="/login/">Login</a>.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <header className="topbar">
        <div><strong>MedApp – Admin Panel</strong></div>
        <div className="topbar-right">
          <span className="muted">
            Logged in as <strong>{username}</strong> ({role})
          </span>
          <button
            className="small ghost"
            onClick={() => {
              localStorage.removeItem(TOKEN_KEY);
              window.location.href = "/login/";
            }}
          >
            Logout
          </button>
        </div>
      </header>

      <main className="content">
        <div className="card">
          <h2>Events</h2>

          <div className="filters">
            <select
              value={filter}
              onChange={(e) => {
                setPage(0);
                setFilter(e.target.value);
                // reload full page from backend so paging makes sense
                loadEvents(0, e.target.value);
              }}
            >
              <option value="all">All Events</option>
              <option value="appointments">Appointment Events (Kafka)</option>
              <option value="notifications">Notification Events (RabbitMQ)</option>
            </select>
          </div>

          {newEventCount > 0 && (
            <div style={{ marginBottom: '0.75rem' }}>
              <button
                className="small"
                onClick={() => {
                  // bring buffered events into view
                  setEvents((prev) => {
                    const merged = [...newEventsRef.current, ...prev];
                    newEventsRef.current = [];
                    setNewEventCount(0);
                    return merged.slice(0, 50);
                  });
                  setPage(0);
                }}
              >
                Show {newEventCount} new event{newEventCount > 1 ? 's' : ''}
              </button>
            </div>
          )}

          <EventTable events={events} onView={setSelectedEvent} />
          <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </div>
      </main>

      <EventDetailsModal
        event={selectedEvent}
        onClose={() => setSelectedEvent(null)}
      />
    </div>
  );
}