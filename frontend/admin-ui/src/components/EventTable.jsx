import React from "react";

export default function EventTable({ events, onView }) {
  return (
    <div className="table-wrapper">
      <table className="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Source</th>
            <th>Type</th>
            <th>Key</th>
            <th>Created At</th>
            <th style={{ width: "120px" }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {events.length === 0 && (
            <tr>
              <td colSpan="6" style={{ textAlign: "center" }}>
                No events
              </td>
            </tr>
          )}
          {events.map((e) => (
            <tr key={e.id}>
              <td>{e.id}</td>
              <td>{e.source}</td>
              <td>{e.type}</td>
              <td>{e.key || "-"}</td>
              <td>{e.createdAt}</td>
              <td>
                <button className="small" onClick={() => onView(e)}>
                  View
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
