import React from "react";

export default function EventDetailsModal({ event, onClose }) {
  if (!event) return null;

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <h2>Event #{event.id}</h2>
        <p>
          <strong>Source:</strong> {event.source}
        </p>
        <p>
          <strong>Type:</strong> {event.type}
        </p>
        <p>
          <strong>Key:</strong> {event.key || "-"}
        </p>

        <h3>Payload</h3>
        <pre className="json-viewer">
{JSON.stringify(JSON.parse(event.payload), null, 2)}
        </pre>

        <button className="primary" onClick={onClose}>
          Close
        </button>
      </div>
    </div>
  );
}
