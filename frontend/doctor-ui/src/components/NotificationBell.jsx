import React, { useEffect, useState, useRef } from "react";
import {
  fetchUnreadCount,
  fetchNotifications,
  markNotificationRead,
} from "../api/notificationApi";
import { createNotificationClient } from "../socket/NotificationSocket";

export default function NotificationBell({ userId }) {
  const [unread, setUnread] = useState(0);
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [client, setClient] = useState(null);
  const [error, setError] = useState("");
  const mountedRef = useRef(true);

  async function loadUnread() {
    try {
      const count = await fetchUnreadCount();
      if (mountedRef.current) setUnread(typeof count === "number" ? count : Number(count || 0));
    } catch (err) {
      console.error(err);
      if (mountedRef.current) setError("Failed to load unread count");
    }
  }

  async function loadNotifications() {
    try {
      const page = await fetchNotifications(0, 20);
      const content = page.content || page;
      if (mountedRef.current) setNotifications(content);
    } catch (err) {
      console.error(err);
      if (mountedRef.current) setError("Failed to load notifications");
    }
  }

  useEffect(() => {
    if (!userId) return;

    const mountedRefLocal = mountedRef;

    const t = setTimeout(() => {
      if (!mountedRefLocal.current) return;
      loadUnread();
      loadNotifications();
    }, 0);

    const c = createNotificationClient(userId, (msg) => {
      if (!mountedRefLocal.current) return;
      setNotifications((prev) => [msg, ...prev]);
      setUnread((prev) => prev + 1);
    });
    setClient(c);

    return () => {
      mountedRef.current = false;
      clearTimeout(t);
      if (c) c.deactivate();
    };
  }, [userId]);

  async function handleMarkRead(id) {
    try {
      await markNotificationRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
      setUnread((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error(err);
      setError("Failed to mark as read");
    }
  }

  return (
    <div className="notif-container">
      <button className="notif-button" onClick={() => setOpen((o) => !o)}>
        🔔
        {unread > 0 && <span className="notif-badge">{unread}</span>}
      </button>

      {open && (
        <div className="notif-dropdown">
          <h4>Notifications</h4>
          {error && <div className="error">{error}</div>}
          {notifications.length === 0 ? (
            <p className="muted">No notifications</p>
          ) : (
            <ul>
              {notifications.map((n) => (
                <li key={n.id} className={n.read ? "read" : "unread"}>
                  <div className="notif-title">{n.title}</div>
                  <div className="notif-message">{n.message}</div>
                  <div className="notif-meta">
                    <span>{n.createdAt}</span>
                    {!n.read && (
                      <button
                        className="small"
                        onClick={() => handleMarkRead(n.id)}
                      >
                        Mark read
                      </button>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
